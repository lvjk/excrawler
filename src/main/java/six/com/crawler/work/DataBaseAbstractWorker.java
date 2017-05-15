package six.com.crawler.work;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.pool.DruidDataSource;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月20日 下午1:32:48
 */
public abstract class DataBaseAbstractWorker extends AbstractWorker<Page> {

	public DataBaseAbstractWorker() {
		super(Page.class);
	}

	final static Logger LOG = LoggerFactory.getLogger(DataBaseAbstractWorker.class);
	private String dbDriverClassName;
	private String dbUrl;
	private String dbUser;
	private String dbPasswd;
	private DruidDataSource datasource;
	int startIndex = 0;
	int batchSize = 100;

	protected final void initWorker(JobSnapshot jobSnapshot) {
		dbUrl = getJob().getParam(CrawlerJobParamKeys.DB_URL);
		dbUser = getJob().getParam(CrawlerJobParamKeys.DB_USER);
		dbPasswd = getJob().getParam(CrawlerJobParamKeys.DB_PASSWD);
		dbDriverClassName = getJob().getParam(CrawlerJobParamKeys.DB_DRIVER_CLASS_NAME);
		datasource = new DruidDataSource();
		datasource.setUrl(dbUrl);
		datasource.setDriverClassName(dbDriverClassName);
		datasource.setUsername(dbUser);
		datasource.setPassword(dbPasswd);
		datasource.setMaxActive(1);
		insideInit();
	}

	protected int getBatchSize() {
		return batchSize;
	}

	protected void setBatchSize(int batchSize) {
		this.batchSize = batchSize;
	}

	protected int getStartIndex() {
		return startIndex;
	}

	protected void setStartIndex(int startIndex) {
		this.startIndex = startIndex;
	}

	protected abstract void handle(Connection connection, List<Map<String, Object>> datas) throws Exception;

	protected Connection getConnection() throws SQLException {
		final Connection connection = datasource.getConnection();
		return connection;
	}

	@Override
	protected void onError(Exception t,Page doingPage) {
	}

	protected abstract void insideInit();

	protected void insideDestroy() {
		if (null != datasource) {
			datasource.close();
		}
	}
}
