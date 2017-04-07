package six.com.crawler.work;

import java.util.List;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.constants.JobConTextConstants;
import six.com.crawler.entity.HttpProxyType;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.entity.Site;
import six.com.crawler.http.HttpProxyPool;
import six.com.crawler.utils.ThreadUtils;
import six.com.crawler.work.downer.Downer;
import six.com.crawler.work.downer.DownerManager;
import six.com.crawler.work.downer.DownerType;
import six.com.crawler.work.downer.exception.DownerException;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.extract.ExtracterFactory;
import six.com.crawler.work.extract.ExtracterType;
import six.com.crawler.work.space.WorkSpace;
import six.com.crawler.work.store.Store;
import six.com.crawler.work.store.StoreFactory;
import six.com.crawler.work.store.StoreType;

/**
 * @author six
 * @date 2016年1月15日 下午6:45:26 爬虫抽象层
 * 
 *       当爬虫队列数据为null时 那么就会设置状态为finished
 */
public abstract class AbstractCrawlWorker extends AbstractWorker {

	final static Logger log = LoggerFactory.getLogger(AbstractCrawlWorker.class);

	// 上次处理数据时间
	protected int findElementTimeout = Constants.FIND_ELEMENT_TIMEOUT;
	// 默认HTTP PROXY最小休息时间 5 秒
	private int httpProxyMinResttime = Constants.DEFAULT_MIN_HTTPPROXY_RESTTIME;
	// 站点
	private Site site;
	// 爬虫任务工作空间
	protected WorkSpace<Page> workQueue;
	// 下载器
	private Downer downer;
	// http代理池
	private HttpProxyPool httpProxyPool;
	// 解析处理程序
	private Extracter extracter;
	// 爬虫正在处理的page
	private Page doingPage;
	// 数据对外输出存儲处理程序
	private Store store;

	@Override
	protected final void initWorker(JobSnapshot jobSnapshot) {
		// 初始化 站点code
		String siteCode = getJob().getParam(JobConTextConstants.SITE_CODE);
		if (StringUtils.isBlank(siteCode)) {
			throw new NullPointerException("please set siteCode");
		}
		site = getManager().getSiteDao().query(siteCode);
		if (null == site) {
			throw new NullPointerException("did not get site[" + siteCode + "]");
		}

		String workSpace = getJob().getWorkSpaceName();
		if (StringUtils.isBlank(workSpace)) {
			throw new NullPointerException("please set workSpace's name");
		}
		// 初始化 工作队列
		workQueue = getManager().getWorkSpaceManager().newWorkSpace(workSpace, Page.class);

		// 初始化下载器
		int downerTypeInt = getJob().getParamInt(JobConTextConstants.DOWNER_TYPE, 1);
		DownerType downerType = DownerType.valueOf(downerTypeInt);
		downer = DownerManager.getInstance().buildDowner(downerType, this);

		int httpProxyTypeInt = getJob().getParamInt(JobConTextConstants.HTTP_PROXY_TYPE, 0);
		HttpProxyType httpProxyType = HttpProxyType.valueOf(httpProxyTypeInt);

		// 初始化http 代理
		int httpProxyRestTime = getJob().getParamInt(JobConTextConstants.HTTP_PROXY_REST_TIME, httpProxyMinResttime);
		httpProxyPool = new HttpProxyPool(getManager().getRedisManager(), siteCode, httpProxyType, httpProxyRestTime);
		downer.setHttpProxy(httpProxyPool.getHttpProxy());
		// 初始化内容抽取
		List<ExtractItem> extractItems = getManager().getExtractItemDao().query(getJob().getName());
		extracter = ExtracterFactory.newExtracter(this, extractItems,
				ExtracterType.valueOf(getJob().getParamInt(JobConTextConstants.EXTRACTER_TYPE, 0)));
		// 初始化数据存储
		int storeTypeInt = 0;
		// 兼容之前设置的store class模式
		String resultStoreClass = getJob().getParam(JobConTextConstants.RESULT_STORE_CLASS);
		if (StringUtils.equals("six.com.crawler.work.store.DataBaseStore", resultStoreClass)) {
			storeTypeInt = 1;
		} else {
			storeTypeInt = getJob().getParamInt(JobConTextConstants.RESULT_STORE_TYPE, 0);
		}
		this.store = StoreFactory.newStore(this, StoreType.valueOf(storeTypeInt));
		insideInit();
	}

	@Override
	protected void insideWork() throws Exception {
		doingPage = workQueue.pull();
		long downTime = 0;
		long extractTime = 0;
		long storeTime = 0;
		if (null != doingPage) {
			try {
				log.info("start to process page:" + doingPage.getOriginalUrl());
				// 暴露给实现类的
				beforeDown(doingPage);
				// 设置下载器代理
				downer.setHttpProxy(httpProxyPool.getHttpProxy());

				long startTime = System.currentTimeMillis();
				// 下载数据
				downer.down(doingPage);
				// 暴露给实现类的抽取前操作
				beforeExtract(doingPage);
				downTime = System.currentTimeMillis() - startTime;

				startTime = System.currentTimeMillis();
				// 抽取结果
				ResultContext resultContext = extracter.extract(doingPage);
				// 暴露给实现类的抽取后操作
				afterExtract(doingPage, resultContext);
				extractTime = System.currentTimeMillis() - startTime;

				startTime = System.currentTimeMillis();
				// 存储数据
				int storeCount = store.store(resultContext);
				storeTime = System.currentTimeMillis() - startTime;
				getWorkerSnapshot().setTotalResultCount(getWorkerSnapshot().getTotalResultCount() + storeCount);
				// 暴露给实现类的完成操作
				onComplete(doingPage, resultContext);
				// 流程走到这步，可以确认数据已经被完全处理,那么ack 数据，最终删除数据备份
				workQueue.ack(doingPage);
				// 添加数据被处理记录
				workQueue.addDone(doingPage);
				log.info("finished processing,down time[" + downTime + "],extract time[" + extractTime + "],store time["
						+ storeTime + "]:" + doingPage.getOriginalUrl());
			} catch (Exception e) {
				throw new RuntimeException("process page err:" + doingPage.getOriginalUrl(), e);
			}
		} else {
			// 没有处理数据时 设置 state == WorkerLifecycleState.FINISHED
			compareAndSetState(WorkerLifecycleState.STARTED, WorkerLifecycleState.FINISHED);
		}
	}

	/**
	 * 内部初始化
	 */
	protected abstract void insideInit();

	/**
	 * doingPage下载前 可以进行相关操作
	 * 
	 * @param doingPage
	 */
	protected abstract void beforeDown(Page doingPage);

	/**
	 * doingPage抽取前相关操作，可在这里实现验证识别或者判断是否需要登录
	 * 
	 * @param doingPage
	 */
	protected abstract void beforeExtract(Page doingPage);

	/**
	 * doingPage抽取数据后进行相关操作
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
	protected boolean insideOnError(Exception e, Page doingPage) {
		return false;
	}

	protected final void onError(Exception e) {
		if (null != doingPage) {
			if (e instanceof DownerException) {
				long restTime = 1000 * 5;
				log.info("perhaps server is too busy,it's time for having a rest(" + restTime + ")");
				ThreadUtils.sleep(restTime);
			}
			Exception insideException = null;
			boolean insideExceptionResult = false;
			// 异常先丢给实现类自己处理
			try {
				insideExceptionResult = insideOnError(e, doingPage);
			} catch (Exception e1) {
				insideException = e1;
				log.error("insideOnError err page:" + doingPage.getFinalUrl(), e1);
			}
			// 判断内部处理是否可处理,如果不可处理那么这里默认处理
			if (insideExceptionResult) {
				workQueue.ack(doingPage);
			} else {
				String msg = null;
				if (null == insideException
						&& doingPage.getRetryProcess() < Constants.WOKER_PROCESS_PAGE_MAX_RETRY_COUNT) {
					doingPage.setRetryProcess(doingPage.getRetryProcess() + 1);
					workQueue.push(doingPage);
					msg = "retry processor[" + doingPage.getRetryProcess() + "] page:" + doingPage.getFinalUrl();
				} else {
					workQueue.addErr(doingPage);
					workQueue.ack(doingPage);
					msg = "retry process count[" + doingPage.getRetryProcess() + "]>="
							+ Constants.WOKER_PROCESS_PAGE_MAX_RETRY_COUNT + " and push to err queue:"
							+ doingPage.getFinalUrl();
				}
				log.error(msg, e);
			}
		}
	}

	public WorkSpace<Page> getWorkQueue() {
		return workQueue;
	}

	public Downer getDowner() {
		return downer;
	}

	public Extracter getExtracter() {
		return extracter;
	}

	public Store getStore() {
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
