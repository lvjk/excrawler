package six.com.crawler.constants;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月12日 下午6:05:04
 */
public interface JobConTextConstants {

	// 站点 code key
	String SITE_CODE = "siteCode";
	
	// 下载器类型
	String DOWNER_TYPE = "downerType";
	//代理类型
	String HTTP_PROXY_TYPE = "httpProxyType";
	//代理休息时间
	String HTTP_PROXY_REST_TIME = "httpProxyRestTime";
	
	String BATCH_SIZE = "batchSize";

	// 结果存储key
	String RESULT_STORE_CLASS = "resultStoreClass";
	
	String IS_SNAPSHOT_TABLE="isSnapshotTable";
	
	String FIXED_TABLE_NAME="fixedTableName";

	// 主要的结果key
	String MAIN_RESULT_KEY = "mainResultKey";
	// 结果存储sql
	String DB_URL = "dbUrl";
	String DB_DRIVER_CLASS_NAME = "dbDriverClassName";
	// 结果存储sql
	String DB_USER = "dbUser";
	// 结果存储sql
	String DB_PASSWD = "dbPasswd";

	// 结果发送http url
	String SEND_HTTP_URL = "sendHttpUrl";

	String SEND_HTTP_METHOD = "sendHttpMethod";

	String FIND_ELEMENT_TIME_OUT = "findElementTimeout";

	String CREATE_TABLE_SQL_TEMPLATE = "createTableSqlTemplate";

	String SELECT_SQL_TEMPLATE = "selectSqlTemplate";

	String INSERT_SQL_TEMPLATE = "insertSqlTemplate";

	String UPDATE_SQL_TEMPLATE = "updateSqlTemplate";

	String DEL_SQL_TEMPLATE = "delSqlTemplate";
}
