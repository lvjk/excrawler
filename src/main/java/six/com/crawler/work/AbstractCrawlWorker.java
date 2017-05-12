package six.com.crawler.work;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;

import six.com.crawler.entity.HttpProxyType;
import six.com.crawler.entity.JobParamKeys;
import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.entity.Site;
import six.com.crawler.http.HttpMethod;
import six.com.crawler.http.HttpProxyPool;
import six.com.crawler.node.lock.DistributedLock;
import six.com.crawler.schedule.consts.DownloadContants;
import six.com.crawler.utils.ThreadUtils;
import six.com.crawler.work.downer.Downer;
import six.com.crawler.work.downer.DownerHelper;
import six.com.crawler.work.downer.DownerManager;
import six.com.crawler.work.downer.DownerType;
import six.com.crawler.work.downer.exception.DownerException;
import six.com.crawler.work.downer.exception.HttpFiveZeroTwoException;
import six.com.crawler.work.downer.exception.RawDataNotFoundException;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.Extracter;
import six.com.crawler.work.extract.ExtracterFactory;
import six.com.crawler.work.extract.ExtracterType;
import six.com.crawler.work.store.Store;
import six.com.crawler.work.store.StoreFactory;
import six.com.crawler.work.store.StoreType;

/**
 * @author six
 * @date 2016年1月15日 下午6:45:26 爬虫抽象层
 * 
 *       当爬虫队列数据为null时 那么就会设置状态为finished
 */
public abstract class AbstractCrawlWorker extends AbstractWorker<Page> {

	final static Logger log = LoggerFactory.getLogger(AbstractCrawlWorker.class);

	// 上次处理数据时间
	protected int findElementTimeout = Constants.FIND_ELEMENT_TIMEOUT;
	// 站点
	private Site site;
	// 下载器
	private Downer downer;
	// http代理池
	private HttpProxyPool httpProxyPool;
	// 抽取项
	private List<ExtractItem> extractItems;
	// 解析处理程序
	private Extracter extracter;
	// 数据对外输出存儲处理程序
	private Store store;

	protected DownerHelper helper = null;

	private ExecutorService cachedThreadPool = Executors.newCachedThreadPool();

	public AbstractCrawlWorker() {
		super(Page.class);
	}

	@Override
	protected final void initWorker(JobSnapshot jobSnapshot) {
		// 初始化 站点code
		String siteCode = getJob().getParam(JobParamKeys.SITE_CODE);
		if (StringUtils.isBlank(siteCode)) {
			throw new NullPointerException("please set siteCode");
		}
		site = getManager().getSiteDao().query(siteCode);
		if (null == site) {
			throw new NullPointerException("did not get site[" + siteCode + "]");
		}

		initDownerHelper(siteCode, jobSnapshot);

		// 初始化下载器
		int downerTypeInt = getJob().getParamInt(JobParamKeys.DOWNER_TYPE, 1);
		DownerType downerType = DownerType.valueOf(downerTypeInt);
		downer = DownerManager.getInstance().buildDowner(downerType, this);

		int httpProxyTypeInt = getJob().getParamInt(JobParamKeys.HTTP_PROXY_TYPE, 0);
		HttpProxyType httpProxyType = HttpProxyType.valueOf(httpProxyTypeInt);

		String siteHttpProxyPoolLockPath = HttpProxyPool.REDIS_HTTP_PROXY_POOL + "_" + siteCode;
		DistributedLock distributedLock = getManager().getNodeManager().getWriteLock(siteHttpProxyPoolLockPath);
		httpProxyPool = new HttpProxyPool(getManager().getRedisManager(), distributedLock, siteCode, httpProxyType,
				site.getVisitFrequency());
		downer.setHttpProxy(httpProxyPool.getHttpProxy());

		// 初始化内容抽取
		extractItems = getManager().getExtractItemDao().query(getJob().getName());
		extracter = ExtracterFactory.newExtracter(this, extractItems,
				ExtracterType.valueOf(getJob().getParamInt(JobParamKeys.EXTRACTER_TYPE, 0)));
		// 初始化数据存储
		int storeTypeInt = 0;
		// 兼容之前设置的store class模式
		String resultStoreClass = getJob().getParam(JobParamKeys.RESULT_STORE_CLASS);
		if (StringUtils.equals("six.com.crawler.work.store.DataBaseStore", resultStoreClass)) {
			storeTypeInt = 1;
		} else {
			storeTypeInt = getJob().getParamInt(JobParamKeys.RESULT_STORE_TYPE, 0);
		}
		this.store = StoreFactory.newStore(this, StoreType.valueOf(storeTypeInt));

		useRawData(siteCode);
		insideInit();
	}

	@Override
	protected void insideWork(Page doingPage) throws Exception {
		if (null != doingPage) {
			long downTime = 0;
			long extractTime = 0;
			long storeTime = 0;
			try {
				log.info("start to process page:" + doingPage.getOriginalUrl());
				// 设置下载器代理
				downer.setHttpProxy(httpProxyPool.getHttpProxy());
				// 暴露给实现类的
				beforeDown(doingPage);
				long startTime = System.currentTimeMillis();

				if (!helper.isDownloadState() || !helper.isUseRawData()) {// 当下载状态不为true的时候才需要下载
					// 下载数据
					downer.down(doingPage);

					if (helper.isSaveRawData()) {
						// 保存源数据
						saveRawData(doingPage, helper);
					}
				} else {
					String fileName = helper.getRawDataPath() + "/data/" + convertFileName(doingPage.getFinalUrl());
					String rawDataStr = readByFile(fileName);
					doingPage.setPageSrc(rawDataStr);
				}

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
				// 流程走到这步，可以确认数据已经被完全处理,那么ack 数据，最终删除数据
				getWorkSpace().ack(doingPage);
				// 添加数据被处理记录
				getWorkSpace().addDone(doingPage.getKey());

				log.info("finished processing,down time[" + downTime + "],extract time[" + extractTime + "],store time["
						+ storeTime + "]:" + doingPage.getOriginalUrl());
			} catch (Exception e) {
				throw new RuntimeException("process page err:" + doingPage.getOriginalUrl(), e);
			}
		}
	}

	/**
	 * 内部初始化
	 */
	protected abstract void insideInit();

	// private void outToWorkSpace(ResultContext resultContext, Page doingPage)
	// {
	// for(int i=0;null!=extractItems&&i<extractItems.size();i++){
	// ExtractItem extractItem=extractItems.get(i);
	// if(extractItem.getType()==ExtractItemType.URL.value()
	// &&extractItem.getOutputType()==3){
	// List<String>
	// urlList=resultContext.getExtractResult(extractItem.getOutputKey());
	// if(1==urlList.size()){
	// String newUrl=urlList.get(0);
	// }else if(urlList.size()>1){
	//
	// }
	// }
	// }
	// }

	/**
	 * doingPage下载前 可以进行相关操作
	 * 
	 * @param doingPage
	 */
	protected abstract void beforeDown(Page doingPage);

	/**
	 * 是否保存原始数据
	 * 
	 * @param doingPage
	 * @param helper
	 */
	protected void saveRawData(Page doingPage, DownerHelper helper) {
		if (helper.isDownloadState()) {// 如果已下载完成。则不下载
			return;
		}
		cachedThreadPool.execute(new Runnable() {// 异步保存文件
			@Override
			public void run() {
				// 是否保存下载数据
				if (doingPage.getNoNeedDown() != -1 && helper.isSaveRawData()) {
					// 保存源数据
					String savePath = helper.getRawDataPath();
					String fileName = doingPage.getFinalUrl();
					if (doingPage.getMethod() == HttpMethod.POST) {// 当请求为post时，将参数拼装到文件名中
						if (null != doingPage.getParameters()) {
							fileName = fileName + "_";
							for (String key : doingPage.getParameters().keySet()) {
								fileName = "&" + key + "=" + doingPage.getParameters().get(key);
							}
						}
					}
					fileName = convertFileName(fileName);
					if (createDir(savePath)) {
						// 保存数据
						String dataPath = savePath + "/data/" + fileName;
						if (null != doingPage.getPageSrc()) {
							saveToFile(dataPath, doingPage.getPageSrc());
						}

						// 保存meta
						String metaPath = savePath + "/meta/" + fileName;
						String metaJsonStr = JSONUtils.toJSONString(doingPage.getMetaMap());
						if (!metaJsonStr.isEmpty() && !"{}".equals(metaJsonStr)) {
							saveToFile(metaPath, metaJsonStr);
						}
					}
				}
			}
		});
	}

	/**
	 * 是否使用原始数据
	 * 
	 * @param siteCode
	 */
	@SuppressWarnings("unchecked")
	protected void useRawData(String siteCode) {
		if (helper.isUseRawData()) {
			boolean downloadState = true;// 页面上需要展示是否存在源数据
			if (downloadState == true) {
				String[] rawDataFiles = null;
				String rawDataDir = helper.getRawDataPath() + "/data";
				File rawDataFile = new File(rawDataDir);
				if (!rawDataFile.exists()) {
					throw new RawDataNotFoundException("Raw data not found exception!");
				}
				rawDataFiles = rawDataFile.list();
				for (String fileName : rawDataFiles) {
					File contextFile = new File(fileName);
					String url = reConvertFileName(contextFile.getName());
					Page data = new Page(siteCode, 1, url, url);

					// String
					// dataFileName=helper.getRawDataPath()+"/data/"+fileName;
					// String rawDataStr=readByFile(dataFileName);

					String metaFileName = helper.getRawDataPath() + "/meta/" + fileName;
					String metaDataStr = readByFile(metaFileName);
					if (null != metaDataStr && !metaDataStr.isEmpty() && metaDataStr.equals("{}")) {
						Map<String, List<String>> metaDataMap = (Map<String, List<String>>) JSONUtils
								.parse(metaDataStr);
						// 无法得到上一步的参数。无法保证唯一性。
						data.getMetaMap().putAll(metaDataMap);
					}
					// data.setPageSrc(rawDataStr);

					getWorkSpace().push(data);
				}
			}
		}
	}

	/**
	 * 初始化下载帮助类
	 * 
	 * @param siteCode
	 * @param jobSnapshot
	 * @return
	 */
	public DownerHelper initDownerHelper(String siteCode, JobSnapshot jobSnapshot) {
		boolean isSaveRawData = getJob().getParamInt(JobParamKeys.IS_SAVE_RAW_DATA,
				JobParamKeys.DEFAULT_IS_SAVE_RAW_DATA) == 1 ? true : false;
		helper = new DownerHelper();
		String rawdataBasePath = getConfigure().getConfig("rawdata.path", "/home/excrawler/rawdata");
		helper.setRawdataBasePath(rawdataBasePath);
		helper.setSaveRawData(isSaveRawData);
		helper.setSiteCode(siteCode);
		helper.setJobName(getJob().getName());

		jobSnapshot.setSaveRawData(isSaveRawData);

		boolean isUseRawData = getJob().getParamInt(JobParamKeys.IS_USE_RAW_DATA,
				JobParamKeys.DEFAULT_IS_USE_RAW_DATA) == 1 ? true : false;
		helper.setUseRawData(isUseRawData);

		JobSnapshot current = getManager().getJobSnapshotDao().queryCurrentJob(jobSnapshot.getName());
		if (current != null) {
			int lastDownloadState = current.getDownloadState();
			if (lastDownloadState == DownloadContants.DOWN_LOAD_FINISHED) {
				helper.setDownloadState(true);
			} else {
				helper.setDownloadState(false);
			}
		} else {
			helper.setDownloadState(false);
		}
		return helper;
	}

	/**
	 * URL字符转换
	 * 
	 * @param fileName
	 * @return
	 */
	private String convertFileName(String fileName) {
		String result = fileName.replaceAll("/", "@").replaceAll(":", "~").replaceAll("\\?", "!");
		return result;
	}

	/**
	 * URL字符转换-反转
	 * 
	 * @param fileName
	 * @return
	 */
	public String reConvertFileName(String fileName) {
		String result = fileName.replaceAll("@", "/").replaceAll("~", ":").replaceAll("!", "\\?");
		return result;
	}

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

	protected final void onError(Exception e, Page doingPage) {
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
				getWorkSpace().ack(doingPage);
			} else {
				String msg = null;
				Integer retryProcess = getJob().getParamInt("worker_process_page_max_retry_count",
						Constants.WOKER_PROCESS_PAGE_MAX_RETRY_COUNT);
				if (null == insideException && doingPage.getRetryProcess() < retryProcess) {
					doingPage.setRetryProcess(doingPage.getRetryProcess() + 1);
					getWorkSpace().errRetryPush(doingPage);
					msg = "retry processor[" + doingPage.getRetryProcess() + "] page:" + doingPage.getFinalUrl();
				} else {
					if (e instanceof HttpFiveZeroTwoException && doingPage.getFztRetryProcess() < retryProcess) {// 当超过重试次数之后，如果是502异常，则重新写入队列
						doingPage.setFztRetryProcess(doingPage.getFztRetryProcess() + 1);
						getWorkSpace().errRetryPush(doingPage);
						msg = "HttpCode[502] retry processor[" + doingPage.getRetryProcess() + "] page:"
								+ doingPage.getFinalUrl();
					} else {
						getWorkSpace().addErr(doingPage);
						getWorkSpace().ack(doingPage);
						msg = "retry process count[" + doingPage.getRetryProcess() + "]>="
								+ Constants.WOKER_PROCESS_PAGE_MAX_RETRY_COUNT + " and push to err queue:"
								+ doingPage.getFinalUrl();
					}
				}
				log.error(msg, e);
			}
		}
	}

	/**
	 * 创建dir，包含data和meta
	 * 
	 * @param destDirName
	 * @return
	 */
	public static boolean createDir(String destDirName) {

		if (!destDirName.endsWith(File.separator)) {
			destDirName = destDirName + File.separator;
		}

		File dataDir = new File(destDirName + "/data");
		File metaDir = new File(destDirName + "/meta");
		if (dataDir.exists() && metaDir.exists()) {
			return true;
		}

		// 创建目录
		if (dataDir.mkdirs() && metaDir.mkdirs()) {
			log.info("create dir " + destDirName + "success!");
			return true;
		} else {
			log.error("create dir " + destDirName + "fail!");
			return false;
		}
	}

	public static void saveToFile(final String fileName, final String context) {
		File file = null;
		FileWriter writer = null;
		try {
			file = new File(fileName);
			writer = new FileWriter(file);

			writer.write(context);
			writer.flush();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
				writer.close();
			} catch (IOException e) {
				log.error(e.getMessage(), e);
			}
		}
	}

	public static String readByFile(String fileName) {
		String content = null;
		File f = new File(fileName);

		FileChannel channel = null;
		FileInputStream fs = null;
		try {
			if (!f.exists()) {
				return null;
			}
			fs = new FileInputStream(f);
			channel = fs.getChannel();
			ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
			while ((channel.read(byteBuffer)) > 0) {
			}
			content = new String(byteBuffer.array());
		} catch (IOException e) {
			e.printStackTrace();
		} finally {

			try {
				if (null != channel) {
					channel.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				if (null != fs) {
					fs.close();
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return content;
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
		if (null != store) {
			store.close();
		}
	}

}
