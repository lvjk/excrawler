package six.com.crawler.work;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.DateFormats;
import six.com.crawler.common.constants.JobConTextConstants;
import six.com.crawler.common.entity.HttpProxy;
import six.com.crawler.common.entity.HttpProxyType;
import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.common.entity.Site;
import six.com.crawler.common.utils.MD5Utils;
import six.com.crawler.schedule.AbstractSchedulerManager;
import six.com.crawler.work.downer.Downer;
import six.com.crawler.work.downer.DownerManager;
import six.com.crawler.work.downer.DownerType;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.store.StoreAbstarct;

/**
 * @author six
 * @date 2016年1月15日 下午6:45:26 爬虫抽象层
 */
public abstract class AbstractCrawlWorker extends AbstractWorker {

	final static Logger LOG = LoggerFactory.getLogger(AbstractCrawlWorker.class);
	// 上次处理数据时间
	protected int findElementTimeout = 1000;
	private Site site; // 站点
	private Downer downer;// 下载器
	// 解析处理程序
	private Extracter extracter;
	private Page doingPage;
	protected WorkQueue workQueue; // 队列
	// 存儲处理程序
	private StoreAbstarct store;
	// 处理的结果key
	List<String> outResultKey;
	// 主要的结果key
	private String mainResultKey;
	// 主要的结果key
	private Set<String> mainResultKeys;

	public AbstractCrawlWorker(String name, AbstractSchedulerManager manager, Job job, Site site, WorkQueue workQueue) {
		super(name, manager, job);
		this.workQueue = workQueue;
		this.site = site;
	}

	@SuppressWarnings("unchecked")
	@Override
	protected final void initWorker() {
		String downerType = getJob().getParameter(JobConTextConstants.DOWNER_TYPE, String.class);
		downer = DownerManager.getInstance().buildDowner(DownerType.valueOf(Integer.valueOf(downerType)), this);
		String mainResultKey = getJob().getParameter(JobConTextConstants.MAIN_RESULT_KEY, String.class);
		if (StringUtils.isNotBlank(mainResultKey)) {
			String[] mainResultKeys = mainResultKey.split(";");
			this.mainResultKey = mainResultKeys[0];
			this.mainResultKeys = new HashSet<>(mainResultKeys.length);
			for (String key : mainResultKeys) {
				this.mainResultKeys.add(key);
			}
		}
		List<ExtractItem> extractItems = getManager().getJobService().queryPaserItem(getJob().getName());
		extracter = new Extracter(this, extractItems);
		String resultStoreClass = getJob().getResultStoreClass();
		if (StringUtils.isNotBlank(resultStoreClass) && !"null".equalsIgnoreCase(resultStoreClass)) {
			for (ExtractItem paserPath : extractItems) {
				if (paserPath.getOutput() == 1) {
					outResultKey.add(paserPath.getResultKey());
				}
			}
			outResultKey.add(Constants.COLLECTION_DATE_FIELD);
			outResultKey.add(Constants.ORIGIN_URL);
			Class<?> storeClz = null;
			try {
				storeClz = Class.forName(resultStoreClass);
			} catch (ClassNotFoundException e) {
				LOG.error("ClassNotFoundException  err:" + resultStoreClass, e);
			}
			Constructor<?> storeConstructor = null;
			if (null != storeClz) {
				try {
					storeConstructor = storeClz.getConstructor(AbstractWorker.class, List.class);
				} catch (NoSuchMethodException e) {
					LOG.error("NoSuchMethodException getConstructor err:" + storeClz, e);
				} catch (SecurityException e) {
					LOG.error("SecurityException err" + storeClz, e);
				}
			}

			if (null != storeConstructor) {
				try {
					this.store = (StoreAbstarct) storeConstructor.newInstance(this, outResultKey);
				} catch (InstantiationException e) {
					LOG.error("InstantiationException  err:" + resultStoreClass, e);
				} catch (IllegalAccessException e) {
					LOG.error("IllegalAccessException  err:" + storeClz.getName(), e);
				} catch (IllegalArgumentException e) {
					LOG.error("IllegalArgumentException  err:" + storeClz.getName(), e);
				} catch (InvocationTargetException e) {
					LOG.error("InvocationTargetException  err:" + storeClz.getName(), e);
				}
			}
		}
		List<String> seedPageMd5s = getJob().getParameter(JobConTextConstants.SEED_PAGE, List.class);
		if (null != seedPageMd5s && seedPageMd5s.size() > 0) {
			List<Page> seedPages = getManager().getPageService().query(getSite().getCode(), seedPageMd5s);
			for (Page seedPage : seedPages) {
				getWorkQueue().push(seedPage);
			}
		}
		insideInit();
	}

	@Override
	protected void insideWork() throws Exception {
		doingPage = workQueue.pull();
		if (null != doingPage) {
			try {
				LOG.info("processor page:" + doingPage.getOriginalUrl());
				// 1.设置下载器代理
				setHttpProxyForDowner();
				// 2. 下载数据
				downer.down(doingPage);
				// 3. 抽取前操作
				beforeExtract(doingPage);
				// 4.抽取结果
				ResultContext resultContext = extracter.extract(doingPage);
				// 5.设置默认字段
				setDefaultResult(resultContext);
				// 6.抽取后操作
				afterExtract(doingPage, resultContext);
				if(null!=store){
					// 7.存储数据
					int storeCount = store.store(resultContext);
					getWorkerSnapshot().setTotalResultCount(getWorkerSnapshot().getTotalResultCount() + storeCount);
				}
				// 8.记录操作数据
				workQueue.finish(doingPage);// 完成page处理
				// 9.完成操作
				onComplete(doingPage);
				LOG.info("finished processor page:" + doingPage.getOriginalUrl());
			} catch (Exception e) {
				throw new RuntimeException("process page err:" + doingPage.getOriginalUrl(), e);
			}
		} else {
			// 没有处理数据时 设置 state == WorkerLifecycleState.SUSPEND
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.WAITED);
		}
	}

	/**
	 * 内部初始化
	 */
	protected abstract void insideInit();

	/**
	 * 下载前处理
	 * 
	 * @param doingPage
	 */
	protected abstract void beforeDown(Page doingPage);

	/**
	 * 抽取数据前
	 * 
	 * @param doingPage
	 */
	protected abstract void beforeExtract(Page doingPage);

	/**
	 * 抽取数据后
	 * 
	 * @param doingPage
	 * @param resultContext
	 */
	protected abstract void afterExtract(Page doingPage, ResultContext resultContext);

	/**
	 * 完成操作
	 * 
	 * @param doingPage
	 */
	protected abstract void onComplete(Page doingPage);

	/**
	 * 异常处理
	 * 
	 * @param e
	 * @param doingPage
	 */
	protected abstract void insideOnError(Exception e, Page doingPage);

	/**
	 * 设置爬虫默认字段
	 * 
	 * @param resultContext
	 */
	private void setDefaultResult(ResultContext resultContext) {
		List<String> mainResultList = resultContext.getResult(mainResultKey);
		if (null != mainResultList) {
			List<String> collectionDateList = new ArrayList<>();
			List<String> originUrlList = new ArrayList<>();
			for (int i = 0; i < mainResultList.size(); i++) {
				collectionDateList.add(DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1));
				originUrlList.add(doingPage.getFinalUrl());
			}
			resultContext.addResult(Constants.COLLECTION_DATE_FIELD, collectionDateList);
			resultContext.addResult(Constants.ORIGIN_URL, originUrlList);

			List<String> keyValues = new ArrayList<>();
			List<String> resultList = null;
			int size = mainResultList.size();
			List<String> ids = new ArrayList<>();
			for (int i = 0; i < size; i++) {
				keyValues.clear();
				for (String mainKey : mainResultKeys) {
					resultList = resultContext.getResult(mainKey);
					String value = resultList.get(i);
					keyValues.add(value);
				}
				String id = getResultID(keyValues);
				ids.add(id);
			}
			resultContext.addResult(Constants.RESULT_ID_KEY, ids);
		}
	}

	public void setHttpProxyForDowner() {
		HttpProxy httpProxy = null;
		if (null != downer) {
			if (getJob().getHttpProxyType() == HttpProxyType.ENABLE_ONE && null == downer.getHttpProxy()) {
				httpProxy = getManager().getHttpPorxyService().getHttpProxy(site.getCode());
				downer.setHttpProxy(httpProxy);
			} else if (getJob().getHttpProxyType() == HttpProxyType.ENABLE_MANY) {
				httpProxy = getManager().getHttpPorxyService().getHttpProxy(site.getCode());
				downer.setHttpProxy(httpProxy);
			} else {
				downer.setHttpProxy(httpProxy);
			}
		}
	}

	protected void onError(Exception e) {
		if (null != doingPage) {
			if (doingPage.getRetryProcess() < Constants.WOKER_PROCESS_PAGE_MAX_RETRY_COUNT) {
				doingPage.setRetryProcess(doingPage.getRetryProcess() + 1);
				workQueue.retryPush(doingPage);
			} else {
				workQueue.pushErr(doingPage);
			}
			LOG.error("processor err page[" + doingPage.getRetryProcess() + "] :" + doingPage.getFinalUrl(), e);
		}
		insideOnError(e, doingPage);
	}

	private String getResultID(List<String> keyValues) {
		StringBuilder newValue = new StringBuilder();
		for (String value : keyValues) {
			newValue.append(value);
		}
		String id = MD5Utils.MD5(newValue.toString());
		return id;
	}

	public WorkQueue getWorkQueue() {
		return workQueue;
	}

	public Downer getDowner() {
		return downer;
	}

	public Extracter getExtracter() {
		return extracter;
	}

	public StoreAbstarct getStore() {
		return this.store;
	}

	public long getFindElementTimeout() {
		return findElementTimeout;
	}

	public Site getSite() {
		return site;
	}

	protected void insideDestroy() {
		if (null != downer) {
			downer.close();
		}
	}
}
