package six.com.crawler.dao;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月16日 上午9:57:55
 * 
 *       数据库表名 定义常数接口
 */
public interface TableNames {

	/** 网站站点 数据库 表名 **/
	String SITE_TABLE_NAME = "ex_crawler_platform_site";

	/** 网站站点 页面 数据库 表名 **/
	String SITE_PAGE_TABLE_NAME = "ex_crawler_platform_site_page";

	/** 网站站点 页面 元素抽取路径 数据库 表名 **/
	String SITE_EXTRACT_PATH_TABLE_NAME = "ex_crawler_platform_extract_path";

	/** 任务 数据库 表名 **/
	String JOB_TABLE_NAME = "ex_crawler_platform_job";

	/** 任务参数 数据库 表名 **/
	String JOB_PARAM_TABLE_NAME = "ex_crawler_platform_job_param";

	/** 任务抽取项 数据库 表名 **/
	String JOB_EXTRACT_ITEM_TABLE_NAME = "ex_crawler_platform_extract_item";

	/** 任务执行快照 数据库 表名 **/
	String JOB_SNAPSHOT_TABLE_NAME = "ex_crawler_platform_job_snapshot";

	/** 任务worker执行快照 数据库 表名 **/
	String JOB_WORKER_SNAPSHOT_TABLE_NAME = "ex_crawler_platform_job_worker_snapshot";

	/** 任务worker执行异常信息 数据库 表名 **/
	String JOB_WORKER_SNAPSHOT_ERRMSG_TABLE_NAME = "ex_crawler_platform_job_worker_err";

	/** http代理 数据库 表名 **/
	String HTTP_PROXY_TABLE_NAME = "ex_crawler_platform_http_proxy";
}
