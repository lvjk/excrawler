package six.com.crawler.work;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.common.DateFormats;
import six.com.crawler.constants.JobConTextConstants;
import six.com.crawler.entity.HttpProxyType;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.entity.Site;
import six.com.crawler.http.HttpProxyPool;
import six.com.crawler.utils.MD5Utils;
import six.com.crawler.utils.ThreadUtils;
import six.com.crawler.work.downer.Downer;
import six.com.crawler.work.downer.DownerManager;
import six.com.crawler.work.downer.DownerType;
import six.com.crawler.work.exception.DownerException;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.extract.CssSelectExtracter;
import six.com.crawler.work.store.StoreAbstarct;

/**
 * @author six
 * @date 2016年1月15日 下午6:45:26 爬虫抽象层
 * 
 *       当爬虫队列数据为null时 那么就会设置状态为finished
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
	private List<String> primaryKeys;;

	private HttpProxyPool httpProxyPool;

	@Override
	protected final void initWorker(JobSnapshot jobSnapshot) {
		// 1.初始化 站点code
		String siteCode = getJob().getParam(JobConTextConstants.SITE_CODE);
		if (StringUtils.isBlank(siteCode)) {
			throw new NullPointerException("please set siteCode");
		}
		site = getManager().getSiteDao().query(siteCode);
		// 2.初始化工作对了
		String queueName = getJob().getQueueName();
		if (StringUtils.isBlank(queueName)) {
			throw new NullPointerException("please set queue's name");
		}
		workQueue = new RedisWorkQueue(getManager().getRedisManager(), queueName);
		// 4.初始化下载器
		String downerType = getJob().getParam(JobConTextConstants.DOWNER_TYPE);
		downer = DownerManager.getInstance().buildDowner(DownerType.valueOf(Integer.valueOf(downerType)), this);
		String httpProxyTypeStr = getJob().getParam(JobConTextConstants.HTTP_PROXY_TYPE);
		if (StringUtils.isBlank(httpProxyTypeStr)) {
			httpProxyTypeStr = "0";
		}
		HttpProxyType httpProxyType = HttpProxyType.valueOf(Integer.valueOf(httpProxyTypeStr));

		String httpProxyRestTimeStr = getJob().getParam(JobConTextConstants.HTTP_PROXY_REST_TIME);
		httpProxyRestTimeStr = httpProxyRestTimeStr == null ? "0" : httpProxyRestTimeStr;
		int httpProxyRestTime = 0;
		try {
			httpProxyRestTime = Integer.valueOf(httpProxyRestTimeStr);
		} catch (Exception e) {
			LOG.error("job[" + getJob().getName() + "] param[" + httpProxyRestTime + "] is invalid:"
					+ httpProxyRestTimeStr, e);
		}
		httpProxyPool =new HttpProxyPool(getManager().getRedisManager(), siteCode, httpProxyType, httpProxyRestTime);
		downer.setHttpProxy(httpProxyPool.getHttpProxy());
		// 5.初始化内容抽取
		List<ExtractItem> extractItems = getManager().getExtractItemDao().query(getJob().getName());
		if (null != extractItems && !extractItems.isEmpty()) {
			primaryKeys = new ArrayList<>();
			outResultKey = new ArrayList<>();
			for (ExtractItem extractItem : extractItems) {
				if (extractItem.getOutputType() == 1) {
					outResultKey.add(extractItem.getOutputKey());
				}
				if (extractItem.getPrimary() == 1 && !primaryKeys.contains(extractItem.getOutputKey())) {
					primaryKeys.add(extractItem.getOutputKey());
				}
			}
			if (!outResultKey.isEmpty() && primaryKeys.isEmpty()) {
				throw new RuntimeException("there is a primary's key at least");
			}
			outResultKey.add(0, Constants.DEFAULT_RESULT_ID);
			outResultKey.add(Constants.DEFAULT_RESULT_COLLECTION_DATE);
			outResultKey.add(Constants.DEFAULT_RESULT_ORIGIN_URL);
			extracter = new CssSelectExtracter(this, extractItems);
		}
		String resultStoreClass = getJob().getParam(JobConTextConstants.RESULT_STORE_CLASS);
		if (StringUtils.isNotBlank(resultStoreClass) && !"null".equalsIgnoreCase(resultStoreClass)) {
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
		insideInit();
	}

	@Override
	protected void insideWork() throws Exception {
		long startTime = System.currentTimeMillis();
		doingPage = workQueue.pull();
		long endTime = System.currentTimeMillis();
		LOG.debug("workQueue pull time:" + (endTime - startTime));
		if (null != doingPage) {
			try {
				LOG.info("processor page:" + doingPage.getOriginalUrl());
				// 1.设置下载器代理
				downer.setHttpProxy(httpProxyPool.getHttpProxy());
				startTime = System.currentTimeMillis();
				// 2. 下载数据
				downer.down(doingPage);
				endTime = System.currentTimeMillis();
				LOG.debug("downer down time:" + (endTime - startTime));
				startTime = System.currentTimeMillis();
				// 3. 抽取前操作
				beforeExtract(doingPage);
				ResultContext resultContext = null;
				// 4.抽取结果
				if (null != extracter) {
					resultContext = extracter.extract(doingPage);
				} else {
					resultContext = new ResultContext();
				}
				// 5.抽取后操作
				afterExtract(doingPage, resultContext);
				// 6.组装数据和设置默认字段
				assembleExtractResult(resultContext);
				endTime = System.currentTimeMillis();
				LOG.debug("extracter extract time:" + (endTime - startTime));
				startTime = System.currentTimeMillis();
				if (null != store) {
					// 7.存储数据
					int storeCount = store.store(resultContext);
					getWorkerSnapshot().setTotalResultCount(getWorkerSnapshot().getTotalResultCount() + storeCount);
				}
				endTime = System.currentTimeMillis();
				LOG.debug("store time:" + (endTime - startTime));
				// 8.记录操作数据
				workQueue.finish(doingPage);// 完成page处理
				// 9.完成操作
				onComplete(doingPage, resultContext);
				LOG.info("finished processor page:" + doingPage.getOriginalUrl());
			} catch (Exception e) {
				LOG.error("process page err:" +doingPage.getOriginalUrl());
				throw e;
			}
		} else {
			// 没有处理数据时 设置 state == WorkerLifecycleState.FINISHED
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.FINISHED);
		}
	}

	/**
	 * 对抽取出来的结果进行组装
	 * 
	 * @param resultContext
	 * @return
	 */
	private void assembleExtractResult(ResultContext resultContext) {
		if (null != primaryKeys && !primaryKeys.isEmpty()) {
			String primaryKey = primaryKeys.get(0);
			List<String> mainResultList = resultContext.getExtractResult(primaryKey);
			if (null != mainResultList) {
				int size = mainResultList.size();
				String nowTime = DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1);
				for (int i = 0; i < size; i++) {
					Map<String, String> dataMap = new HashMap<>();
					List<String> primaryKeysValue = new ArrayList<>();
					for (String resultKey : outResultKey) {
						if (!Constants.DEFAULT_RESULT_KEY_SET.contains(resultKey)) {
							List<String> tempResultList = resultContext.getExtractResult(resultKey);
							String result = "";
							if (i < tempResultList.size()) {
								result = tempResultList.get(i);
							} else {
								if (primaryKeys.contains((resultKey))) {
									throw new RuntimeException("main key[" + resultKey + "] don't have result");
								}
							}
							if (primaryKeys.contains(resultKey)) {
								primaryKeysValue.add(result);
							}
							dataMap.put(resultKey, result);
						}
					}
					String id = getResultID(primaryKeysValue);
					dataMap.put(Constants.DEFAULT_RESULT_ID, id);
					dataMap.put(Constants.DEFAULT_RESULT_COLLECTION_DATE, nowTime);
					dataMap.put(Constants.DEFAULT_RESULT_ORIGIN_URL, doingPage.getFinalUrl());
					resultContext.addoutResult(dataMap);
				}
			}
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
	protected abstract void onComplete(Page doingPage, ResultContext resultContext);

	/**
	 * 内部异常处理，如果成功处理返回true 否则返回false;
	 * 
	 * @param e
	 * @param doingPage
	 * @return
	 */
	protected abstract boolean insideOnError(Exception e, Page doingPage);

	protected void onError(Exception e) {
		if (null != doingPage) {
			if (e instanceof DownerException) {
				long restTime = 1000 * 5;
				LOG.info("perhaps server is too busy,it's time for having a rest(" + restTime + ")");
				ThreadUtils.sleep(restTime);
			}
			Exception insideException = null;
			boolean insideExceptionResult = false;
			try {
				insideExceptionResult = insideOnError(e, doingPage);
			} catch (Exception e1) {
				insideException = e1;
				LOG.error("insideOnError err page:" + doingPage.getFinalUrl(), e1);
			}
			// 判断内部处理是否可处理,如果不可处理那么这里默认处理
			if (!insideExceptionResult) {
				String msg = null;
				if (null == insideException
						&& doingPage.getRetryProcess() < Constants.WOKER_PROCESS_PAGE_MAX_RETRY_COUNT) {
					doingPage.setRetryProcess(doingPage.getRetryProcess() + 1);
					workQueue.retryPush(doingPage);
					msg = "retry processor[" + doingPage.getRetryProcess() + "] page:" + doingPage.getFinalUrl();
				} else {
					workQueue.pushErr(doingPage);
					workQueue.finish(doingPage);
					msg = "retry process count[" + doingPage.getRetryProcess() + "]>="
							+ Constants.WOKER_PROCESS_PAGE_MAX_RETRY_COUNT + " and push to err queue:"
							+ doingPage.getFinalUrl();
				}
				LOG.error(msg, e);
			}
		}
	}

	public static String getResultID(List<String> keyValues) {
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
		if (null != httpProxyPool) {
			httpProxyPool.destroy();
		}
	}
}
