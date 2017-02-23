package six.com.crawler.common.entity;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import six.com.crawler.common.constants.JobConTextConstants;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月19日 下午8:35:28 类说明 爬虫job
 */
public class Job implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 781122651512874550L;
	private String name;// job 名字
	private int level;// 任务级别
	private String siteCode;// 站点code
	private String queueName;// 队列 名字
	private HttpProxyType httpProxyType;// http代理类型
	private int loadImages;// 是否加载图片 2不加载
	private String hostNode;// job所属哪个节点名字
	private int needNodes;// 执行此任务需要的节点数
	private String user = "admin";// 任务 所属用户
	private long everyProcessDelayTime = 1000;// 每次处理时间的阈值 默认1000毫秒
	private String describe;// 任务描述
	private int isScheduled;// 0不调度 1调度 是否开启调度
	private String workerClass;// worker class
	private String resultStoreClass;
	private String cronTrigger;// CronTrigger 时间
	private String fixedTableName;//数据表名
	private int isSnapshotTable;//是否启用镜像表
	private Map<String, Object> map = new HashMap<>();

	public String getCronTrigger() {
		return cronTrigger;
	}

	public void setCronTrigger(String cronTrigger) {
		this.cronTrigger = cronTrigger;
	}

	public long getEveryProcessDelayTime() {
		return everyProcessDelayTime;
	}

	public void setEveryProcessDelayTime(long everyProcessDelayTime) {
		this.everyProcessDelayTime = everyProcessDelayTime;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public int getNeedNodes() {
		return needNodes;
	}

	public void setNeedNodes(int needNodes) {
		this.needNodes = needNodes;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}

	public HttpProxyType getHttpProxyType() {
		return httpProxyType;
	}

	public void setHttpProxyType(int httpProxyType) {
		this.httpProxyType = HttpProxyType.getHttpProxyType(httpProxyType);
	}

	public String getHostNode() {
		return hostNode;
	}

	public void setHostNode(String hostNode) {
		this.hostNode = hostNode;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}

	public String getQueueName() {
		if (null == queueName) {
			queueName = name;
		}
		return queueName;
	}

	public void setQueueName(String queueName) {
		this.queueName = queueName;
	}

	public int getIsScheduled() {
		return isScheduled;
	}

	public void setIsScheduled(int isScheduled) {
		this.isScheduled = isScheduled;
	}

	public String getWorkerClass() {
		return workerClass;
	}

	public void setWorkerClass(String workerClass) {
		this.workerClass = workerClass;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}
	
	public int getLoadImages() {
		return loadImages;
	}

	public void setLoadImages(int loadImages) {
		this.loadImages = loadImages;
	}

	public String getResultStoreClass() {
		return resultStoreClass;
	}

	public void setResultStoreClass(String resultStoreClass) {
		this.resultStoreClass = resultStoreClass;
	}

	@SuppressWarnings("unchecked")
	public void init(List<JobParameter> list) {
		for (JobParameter jobParameter : list) {
			if (JobConTextConstants.SITE_CODE.equals(jobParameter.getAttName())) {
				map.put(JobConTextConstants.SITE_CODE, jobParameter.getAttValue());
			} else if (JobConTextConstants.SEED_PAGE.equals(jobParameter.getAttName())) {
				Object ob = map.get(JobConTextConstants.SEED_PAGE);
				List<String> seedPageMd5s = null;
				if (null == ob) {
					seedPageMd5s = new ArrayList<>();
					map.put(JobConTextConstants.SEED_PAGE, seedPageMd5s);
				} else {
					seedPageMd5s = (List<String>) ob;
				}
				seedPageMd5s.add(jobParameter.getAttValue());
			} else if (JobConTextConstants.RESULT_STORE_CLASS.equals(jobParameter.getAttName())) {
				map.put(JobConTextConstants.RESULT_STORE_CLASS, jobParameter.getAttValue());
			} else {
				map.put(jobParameter.getAttName(), jobParameter.getAttValue());
			}

		}
	}

	public <T> T getParameter(String key, Class<T> claz) {
		Object ob = map.get(key);
		T result = null;
		if (null != ob) {
			result = claz.cast(ob);
		}
		return result;
	}

	public void setParameter(String key, Object value) {
		map.put(key, value);
	}
	
	public String getFixedTableName() {
		return fixedTableName;
	}

	public void setFixedTableName(String fixedTableName) {
		this.fixedTableName = fixedTableName;
	}

	public int getIsSnapshotTable() {
		return isSnapshotTable;
	}

	public void setIsSnapshotTable(int isSnapshotTable) {
		this.isSnapshotTable = isSnapshotTable;
	}
}
