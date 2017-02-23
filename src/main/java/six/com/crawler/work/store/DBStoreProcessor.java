package six.com.crawler.work.store;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

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
 * @date 创建时间：2016年9月20日 上午11:58:20
 */
public class DBStoreProcessor extends StoreAbstarct implements AutoCloseable {

	final static Logger LOG = LoggerFactory.getLogger(DBStoreProcessor.class);
	private String insertSqlTemplate;
	private String insertSql;
	private String tableName;
	private String dbDriverClassName;
	private String dbUrl;
	private String dbUser;
	private String dbPasswd;
	int batchSize;
	private DruidDataSource datasource;

	public DBStoreProcessor(AbstractWorker worker, List<String> resultKeys) {
		super(worker, resultKeys);
		String everySendSizeStr = worker.getJob().getParameter(JobConTextConstants.BATCH_SIZE, String.class);
		if (null != everySendSizeStr) {
			batchSize = Integer.valueOf(everySendSizeStr);
		} else {
			batchSize = 20;
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
	}

	@Override
	protected int insideStore(ResultContext resultContext) throws StoreException {
		int storeCount = 0;
		List<Object> parameter = new ArrayList<>();
		List<String> list = null;
		if (!getResultIsList()) {
			for (String resultKey : resultKeys) {
				String value = "";
				list = resultContext.getResult(resultKey);
				if (null != list && !list.isEmpty()) {
					value = list.get(0);
				}
				parameter.add(value);
			}
			storeCount += doSql(parameter);

		} else {
			List<String> mainResultList = resultContext.getResult(getMainResultKey());
			List<String> resultList = null;
			int size = mainResultList.size();
			for (int i = 0; i < size; i++) {
				parameter.clear();
				for (String resultKey : resultKeys) {
					String value = "";
					resultList = resultContext.getResult(resultKey);
					String tempValue = resultList.get(i);
					if (null != tempValue) {
						value = tempValue;
					}
					parameter.add(value);
				}
				storeCount +=doSql(parameter);
			}
		}
		return storeCount;
	}

	private int doSql(List<Object> parameter) throws StoreException {
		int resultCount = 0;
		Connection connection = null;
		PreparedStatement ps = null;
		try {
			connection = datasource.getConnection();
			ps = connection.prepareStatement(insertSql);
			DbHelper.setPreparedStatement(ps, parameter);
			resultCount = ps.executeUpdate();
		} catch (SQLException e) {
			if (!e.getMessage().contains("Duplicate entry")) {
				throw new StoreException("execute sql err", e);
			} else {
				LOG.info("duplicate entry:" + parameter);
			}
		} finally {
			DbHelper.close(connection);
		}
		return resultCount;

	}

	@Override
	public void close() throws Exception {
		datasource.close();
	}

}
