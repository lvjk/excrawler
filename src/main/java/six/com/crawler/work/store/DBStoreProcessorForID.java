package six.com.crawler.work.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

import six.com.crawler.common.constants.JobConTextConstants;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.utils.DbHelper;
import six.com.crawler.common.utils.JobTableUtils;
import six.com.crawler.work.AbstractWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年12月9日 上午11:15:34
 */
public class DBStoreProcessorForID extends StoreAbstarct implements AutoCloseable {

	final static String checkTableIsCreateSql = "select table_name  " + " from INFORMATION_SCHEMA.tables  "
			+ " where TABLE_NAME=?";
	final static Logger LOG = LoggerFactory.getLogger(DBStoreProcessorForID.class);
	private String insertSqlTemplate;
	private String insertSql;
	private String createTableSqlTemplate;
	private String tableName;
	private String dbDriverClassName;
	private String dbUrl;
	private String dbUser;
	private String dbPasswd;
	int batchSize = 1;
	private DruidDataSource datasource;

	public DBStoreProcessorForID(AbstractWorker worker, List<String> resultKeys) {
		super(worker, resultKeys);
		String everySendSizeStr = worker.getJob().getParameter(JobConTextConstants.BATCH_SIZE, String.class);
		if (null != everySendSizeStr) {
			try {
				batchSize = Integer.valueOf(everySendSizeStr);
			} catch (Exception e) {
				throw new RuntimeException("Integer.valueOf batchSize err:" + everySendSizeStr, e);
			}

		}
		dbUrl = worker.getJob().getParameter(JobConTextConstants.DB_URL, String.class);
		dbUser = worker.getJob().getParameter(JobConTextConstants.DB_USER, String.class);
		dbPasswd = worker.getJob().getParameter(JobConTextConstants.DB_PASSWD, String.class);
		dbDriverClassName = worker.getJob().getParameter(JobConTextConstants.DB_DRIVER_CLASS_NAME, String.class);
		datasource = new DruidDataSource();
		datasource.setUrl(dbUrl);
		datasource.setDriverClassName(dbDriverClassName);
		datasource.setUsername(dbUser);
		datasource.setPassword(dbPasswd);
		datasource.setMaxActive(1);
		tableName = worker.getJobSnapshot().getTableName();
		insertSqlTemplate = worker.getJob().getParameter(JobConTextConstants.INSERT_SQL_TEMPLATE, String.class);
		insertSql = JobTableUtils.buildInsertSql(insertSqlTemplate, tableName);
		createTableSqlTemplate = worker.getJob().getParameter(JobConTextConstants.CREATE_TABLE_SQL_TEMPLATE,
				String.class);
		initTable();
	}

	private void initTable() {
		if (StringUtils.isNotBlank(tableName) && StringUtils.isNotBlank(createTableSqlTemplate)) {
			// 检查table 是否
			if (!checkIsCreated(tableName)) {
				String createTableSql = JobTableUtils.buildCreateTableSql(createTableSqlTemplate, tableName);
				try {
					doSql(createTableSql, null);
				} catch (StoreException e) {
					throw new RuntimeException("create data temp table err", e);
				}
			}
		}
	}

	private boolean checkIsCreated(String table) {
		Connection connection = null;
		try {
			connection = datasource.getConnection();
			List<String> tables = DbHelper.queryTableNames(connection, table);
			if (tables.size() > 0) {
				return true;
			} else {
				return false;
			}
		} catch (SQLException e) {
			throw new RuntimeException("check[" + table + "] is created err:" + checkTableIsCreateSql, e);
		} finally {
			DbHelper.close(connection);
		}
	}

	@Override
	protected int insideStore(ResultContext resultContext) throws StoreException {
		int storeCount = 0;
		List<Object> parameter = new ArrayList<>();
		List<String> list = null;
		String value = "";
		List<String> keyValues = new ArrayList<>(getMainResultKeys().size());
		if (!getResultIsList()) {
			for (String resultKey : resultKeys) {
				list = resultContext.getResult(resultKey);
				if (null != list && !list.isEmpty()) {
					value = list.get(0);
				}
				parameter.add(value);
			}
			storeCount+=doSql(insertSql, parameter);
		} else {
			List<String> mainResultList = resultContext.getResult(getMainResultKey());
			List<String> resultList = null;
			int size = mainResultList.size();
			for (int i = 0; i < size; i++) {
				parameter.clear();
				keyValues.clear();
				for (String resultKey : resultKeys) {
					resultList = resultContext.getResult(resultKey);
					value = resultList.get(i);
					parameter.add(value);
				}
				storeCount+=doSql(insertSql, parameter);
			}
		}
		return storeCount;
	}

	private int doSql(String sql, List<Object> parameter) throws StoreException {
		int storeCount=0;
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = datasource.getConnection();
			ps = connection.prepareStatement(sql);
			DbHelper.setPreparedStatement(ps, parameter);
			storeCount=ps.executeUpdate();
		} catch (SQLException e) {
			if (!e.getMessage().contains("Duplicate entry")) {
				throw new StoreException("execute sql err:" + sql, e);
			} else {
				LOG.info("duplicate entry:" + parameter);
			}
		} finally {
			DbHelper.close(connection);
		}
		return storeCount;

	}

	@Override
	public void close() throws Exception {
		datasource.close();
	}

}
