package six.com.crawler.work;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import six.com.crawler.entity.JobParamKeys;
import six.com.crawler.entity.Page;
import six.com.crawler.utils.DbHelper;
import six.com.crawler.utils.JobTableUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月20日 下午2:24:42
 */
public class DataBaseMergeTableWorker extends DataBaseAbstractWorker {

	private String ID_KEY = "id";
	private String ISLIVE_KEY = "isLive";
	private String fixedTableName;
	private List<Long> tableNameSuffixs;
	private String selectSqlTemplate;
	private String inserSqlTemplate;
	private String updateSqlTemplate;
	private String delSqlTemplate;
	private List<String> insertFields;


	@Override
	protected void insideWork(Page doingPage) throws Exception {
		if (null != tableNameSuffixs && tableNameSuffixs.size() > 0) {
			Connection connection = null;
			try {
				Long tableNameSuffix = tableNameSuffixs.get(0);
				String SnapshotTableName = JobTableUtils.buildJobTableName(fixedTableName,
						String.valueOf(tableNameSuffix));
				connection = getConnection();
				// 1 固定表中的数据对比镜像表是否存在如果不存在那么设置islive=0
				doFixedTable(connection, SnapshotTableName);
				// 2.将 镜像表中的数据插入固定表中
				doSnapshotTable(connection, SnapshotTableName);
				// 3 删除镜像表
				delSnapshotTable(connection, SnapshotTableName);
				tableNameSuffixs.remove(0);
			} catch (Exception e) {
				throw new RuntimeException(e);
			} finally {
				DbHelper.close(connection);
			}
		} else {
			// 没有处理数据时 设置 state == WorkerLifecycleState.SUSPEND
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.STOPED);
		}
	}

	protected void doFixedTable(Connection connection, String snapshotTableName) throws Exception {
		String selectFixedTableSql = JobTableUtils.buildSelectSql(selectSqlTemplate, fixedTableName);
		String selectSnapshotTableSql = JobTableUtils.buildSelectSql(selectSqlTemplate, snapshotTableName);
		String updateFixedTableSql = JobTableUtils.buildSelectSql(updateSqlTemplate, snapshotTableName);
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		int startIndex = 0;
		int batchSize = this.batchSize;
		try {
			while (true) {
				// 查询固定表中的数据
				ps = connection.prepareStatement(selectFixedTableSql);
				DbHelper.setPreparedStatement(ps, Arrays.asList(startIndex, batchSize));
				resultSet = ps.executeQuery();
				String[] columns = DbHelper.getColumn(resultSet);
				// 1 查询最早临时表数据
				List<Map<String, Object>> result = DbHelper.paserResult(resultSet, columns);
				for (Map<String, Object> map : result) {
					String id = map.get(ID_KEY).toString();
					int islive = (int) map.get(ISLIVE_KEY);
					// 如果没查到，那么设置islive为0,如果查到那么设置islive为1
					if (null == query(connection, selectSnapshotTableSql, id)) {
						if (islive == 1) {
							setIsLive(connection, updateFixedTableSql, id, 0);
						}
					} else {
						if (islive != 1) {
							setIsLive(connection, updateFixedTableSql, id, 1);
						}
					}
				}
				// 判断查询出的数据是否==batchSize,如果不等于那么已经查询到最后了，程序跳出
				if (result.size() != batchSize) {
					break;
				} else {
					startIndex += batchSize;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private Map<String, Object> query(Connection connection, String selectSnapshotTableSql, String id) {
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		try {
			// 查询固定表中的数据
			ps = connection.prepareStatement(selectSnapshotTableSql);
			DbHelper.setPreparedStatement(ps, Arrays.asList(id));
			resultSet = ps.executeQuery();
			String[] columns = DbHelper.getColumn(resultSet);
			List<Map<String, Object>> result = DbHelper.paserResult(resultSet, columns);
			if (null != result && result.size() > 0) {
				return result.get(0);
			} else {
				return null;
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	private void setIsLive(Connection connection, String updateFixedTableSql, String id, int isLive) {
		PreparedStatement ps = null;
		try {
			// 查询固定表中的数据
			ps = connection.prepareStatement(updateFixedTableSql);
			DbHelper.setPreparedStatement(ps, Arrays.asList(id, isLive));
			ps.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	protected void doSnapshotTable(Connection connection, String snapshotTableName) throws Exception {
		String selectSnapshotSql = JobTableUtils.buildSelectSql(selectSqlTemplate, snapshotTableName);
		String inserFixedTableSql = JobTableUtils.buildDelSql(inserSqlTemplate, fixedTableName);
		PreparedStatement ps = null;
		ResultSet resultSet = null;
		int startIndex = 0;
		int batchSize = this.batchSize;
		try {
			while (true) {
				// 查询镜像表中的数据
				ps = connection.prepareStatement(selectSnapshotSql);
				DbHelper.setPreparedStatement(ps, Arrays.asList(startIndex, batchSize));
				resultSet = ps.executeQuery();
				String[] columns = DbHelper.getColumn(resultSet);
				// 1 查询最早临时表数据
				List<Map<String, Object>> result = DbHelper.paserResult(resultSet, columns);
				for (Map<String, Object> map : result) {
					List<Object> parameter = new ArrayList<>(insertFields.size());
					String id = map.get(ID_KEY).toString();
					for (int i = 0; i < insertFields.size(); i++) {
						String filed = insertFields.get(i);
						Object value = map.get(filed);
						parameter.add(value);
					}
					try {
						PreparedStatement insertPs = connection.prepareStatement(inserFixedTableSql);
						DbHelper.setPreparedStatement(insertPs, parameter);
						ps.executeUpdate();
					} catch (SQLException e) {
						if (!e.getMessage().contains("Duplicate entry")) {
							throw new RuntimeException("insert err", e);
						} else {
							LOG.info("duplicate entry:" + id, e);
						}
					}
				}
				// 判断查询出的数据是否==batchSize,如果不等于那么已经查询到最后了，程序跳出
				if (result.size() != batchSize) {
					break;
				} else {
					startIndex += batchSize;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	protected void delSnapshotTable(Connection connection, String snapshotTableName) throws Exception {
		String delSnapshotTableSql = JobTableUtils.buildDelSql(delSqlTemplate, snapshotTableName);
		try {
			PreparedStatement ps = connection.prepareStatement(delSnapshotTableSql);
			ps.executeUpdate();
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	@Override
	protected void handle(Connection connection, List<Map<String, Object>> datas) throws Exception {

	}

	@Override
	protected void insideInit() {
		fixedTableName = getJob().getParam(JobParamKeys.FIXED_TABLE_NAME);
		selectSqlTemplate = getJob().getParam(JobParamKeys.SELECT_SQL_TEMPLATE);
		inserSqlTemplate = getJob().getParam(JobParamKeys.INSERT_SQL_TEMPLATE);
		updateSqlTemplate = getJob().getParam(JobParamKeys.UPDATE_SQL_TEMPLATE);
		delSqlTemplate = getJob().getParam(JobParamKeys.DEL_SQL_TEMPLATE);
		selectSqlTemplate = "";
		Connection connection = null;
		List<String> queryTables = null;
		try {
			connection = getConnection();
			queryTables = DbHelper.queryTableNames(connection, fixedTableName);
		} catch (SQLException e) {
			throw new RuntimeException("query tableNames err", e);
		} finally {
			DbHelper.close(connection);
		}
		tableNameSuffixs = new ArrayList<>(queryTables.size());
		for (int i = 0; i < queryTables.size(); i++) {
			String tableName = queryTables.get(i);
			String tableNameSuffixStr = JobTableUtils.splitJobTableNameSuffix(fixedTableName, tableName);
			Long tableNameSuffix = Long.valueOf(tableNameSuffixStr);
			if (tableNameSuffixs.size() == 0) {
				tableNameSuffixs.add(tableNameSuffix);
			} else {
				if (tableNameSuffix < tableNameSuffixs.get(0)) {
					tableNameSuffixs.add(0, tableNameSuffix);
				}
			}
		}
	}
}
