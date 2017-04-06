package six.com.crawler.work.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;

import six.com.crawler.common.DateFormats;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.AutoCharsetDetectorUtils;
import six.com.crawler.utils.ObjectCheckUtils;
import six.com.crawler.utils.TelPhoneUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.exception.ExtractEmptyResultException;
import six.com.crawler.work.extract.exception.ExtractUnknownException;
import six.com.crawler.work.extract.exception.NotFindExtractPathException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 下午9:19:54
 * 
 *       抽取器抽象类：
 * 
 *       <p>
 *       1.普通的css query 抽取
 *       </p>
 *       <p>
 *       2.普通的正则 抽取
 *       </p>
 *       <p>
 *       3.html 单条数据表格抽取
 *       </p>
 *       <p>
 *       4.html 多条数据表格抽取
 *       </p>
 *       <p>
 *       5.json 数据抽取
 *       </p>
 * 
 */
public abstract class AbstractExtracter implements Extracter {

	private AbstractCrawlWorker worker;
	private List<ExtractItem> extractItems;
	private Map<String, List<ExtractPath>> extractPathMap = new HashMap<>();
	private static Set<Character> charSet = new HashSet<>();
	private volatile int primaryResultSize = -1;
	private static final String EMPTY_VALUE = "";
	private AtomicInteger extracterIndex = new AtomicInteger(0);
	private String workerName;
	private String jobSnapshotId;

	static {
		charSet.add(' ');
		charSet.add('\n');
		charSet.add('\t');
	}

	public AbstractExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		ObjectCheckUtils.checkNotNull(worker, "worker");
		ObjectCheckUtils.checkNotNull(extractItems, "extractItems");
		ExtractItem firstExtractItem = null;
		int primaryCount = 0;
		int index = -1;
		for (int i = 0; i < extractItems.size(); i++) {
			ExtractItem extractItem = extractItems.get(i);
			if (extractItem.getPrimary() == 1) {
				primaryCount++;
				firstExtractItem = extractItem;
				index = i;
			}
		}
		if (primaryCount > 1) {
			throw new RuntimeException("there are many primary=1");
		}
		if (-1 != index) {
			extractItems.remove(index);
			extractItems.add(0, firstExtractItem);
		}
		this.worker = worker;
		this.extractItems = extractItems;
		this.workerName = getAbstractWorker().getName();
		this.jobSnapshotId = getAbstractWorker().getJobSnapshot().getId();
	}

	protected abstract List<String> doExtract(Page doingPage, ExtractItem extractItem, ExtractPath path);

	public final ResultContext extract(Page doingPage) {
		ResultContext resultContext = new ResultContext();
		primaryResultSize = -1;
		for (ExtractItem doPaserItem : extractItems) {
			List<String> tempDoResults = null;
			if (doPaserItem.getType() == ExtractItemType.META.value()) {// 判断是否是元数据类型
				tempDoResults = doingPage.getMeta(doPaserItem.getOutputKey());
			} else {
				List<ExtractPath> pathList = extractPathMap.computeIfAbsent(doPaserItem.getPathName(), mapKey -> {// 获取所有path
					return getAbstractWorker().getManager().getExtractPathDao().query(doPaserItem.getPathName(),
							getAbstractWorker().getSite().getCode());
				});
				if (null == pathList || pathList.isEmpty()) {// 如果没有获取到path 抛异常
					throw new NotFindExtractPathException("don't find path:" + doPaserItem.getPathName());
				}
				int ranking = 0;// 默认使用排名第一的path 抽取
				try {
					while (true) {
						ExtractPath path = pathList.get(ranking++);
						tempDoResults = doExtract(doingPage, doPaserItem, path);
						if (null != tempDoResults && tempDoResults.size() > 0) {// 如果抽取到了结果那么对结果进行处理
							break;
						} else if (ranking >= pathList.size()) {// 如果迭代的ranking>=pathList.size()那么跳出循环
							break;
						}
					}
				} catch (Exception e) {// 捕获 未知异常
					throw new ExtractUnknownException("extractItem[" + doPaserItem.getOutputKey() + "] err", e);
				}
			}
			if ((null == tempDoResults || tempDoResults.isEmpty())// 主键必须有值,如果抽取结果为空，判断是否必须有值，如果是那么抛出异常
					&& (1 == doPaserItem.getPrimary() || 1 == doPaserItem.getMustHaveResult())) {
				throw new ExtractEmptyResultException(
						"extract resultKey [" + doPaserItem.getOutputKey() + "] value is empty");
			}
			if (1 == doPaserItem.getPrimary()) {// 记录主键结果数量
				if (-1 == primaryResultSize) {// primaryResultSize第一次直接赋值
					primaryResultSize = tempDoResults.size();
				}
				// else {
				// if (primaryResultSize != tempDoResults.size()) {//
				// 对比与上一次的primaryResultSize
				// // ，如果不相等那么抛出异常
				// throw new PrimaryExtractException("extract primaryKey[" +
				// doPaserItem.getOutputKey()
				// + "]'s result size[" + tempDoResults.size() + "] did not
				// match last primaryResultSize["
				// + primaryResultSize + "]");
				// }
				// }
			} else {
				if (null == tempDoResults || tempDoResults.isEmpty()) {// 如果非主键结果为空，那么默认给它赋值跟主键数量一样的
																		// 空值
					tempDoResults = null == tempDoResults ? new ArrayList<>(primaryResultSize) : tempDoResults;
					for (int i = 0; i < primaryResultSize; i++) {
						tempDoResults.add(EMPTY_VALUE);
					}
				} else if (tempDoResults.size() < primaryResultSize
						&& doPaserItem.getType() == ExtractItemType.META.value()) {
					int supplementCount = primaryResultSize - tempDoResults.size();
					String defaultResult = tempDoResults.get(0);
					for (int i = 0; i < supplementCount; i++) {
						tempDoResults.add(defaultResult);
					}
				} else if (tempDoResults.size() < primaryResultSize) {
					int supplementCount = primaryResultSize - tempDoResults.size();
					for (int i = 0; i < supplementCount; i++) {
						tempDoResults.add(EMPTY_VALUE);
					}
				}
			}
			List<String> doResults = new ArrayList<>(tempDoResults.size());
			for (String doResult : tempDoResults) {
				String tempExtract = paserString(doPaserItem, doingPage, doResult);
				doResults.add(tempExtract);
			}
			resultContext.addExtractResult(doPaserItem.getOutputKey(), doResults);
		}
		// 组装结果，并加上系统默认字段
		assembleExtractResult(resultContext, doingPage, primaryResultSize);
		return resultContext;
	}

	/**
	 * 对抽取出来的结果进行组装，并加上系统默认字段
	 * 
	 * @param resultContext
	 * @return
	 */
	private void assembleExtractResult(ResultContext resultContext, Page doingPage, int primaryResultSize) {
		if (primaryResultSize > 0) {
			String nowTime = DateFormatUtils.format(System.currentTimeMillis(), DateFormats.DATE_FORMAT_1);
			List<String> idList = new ArrayList<>(primaryResultSize);
			List<String> workerNameList = new ArrayList<>(primaryResultSize);
			List<String> collectionDateList = new ArrayList<>(primaryResultSize);
			List<String> originUrlList = new ArrayList<>(primaryResultSize);
			String id = null;
			for (int i = 0; i < primaryResultSize; i++) {
				Map<String, String> dataMap = new HashMap<>();
				for (ExtractItem extractItem : extractItems) {
					List<String> tempResultList = resultContext.getExtractResult(extractItem.getOutputKey());
					String result = tempResultList.get(i);
					dataMap.put(extractItem.getOutputKey(), result);
				}
				id = getId();
				dataMap.put(Extracter.DEFAULT_RESULT_ID, id);
				dataMap.put(Extracter.DEFAULT_RESULT_COLLECTION_DATE, nowTime);
				dataMap.put(Extracter.DEFAULT_RESULT_ORIGIN_URL, doingPage.getFinalUrl());

				idList.add(id);
				workerNameList.add(workerName);
				collectionDateList.add(nowTime);
				originUrlList.add(doingPage.getFinalUrl());
				resultContext.addoutResult(dataMap);
			}
			resultContext.addExtractResult(Extracter.DEFAULT_RESULT_ID, idList);
			resultContext.addExtractResult(Extracter.DEFAULT_RESULT_COLLECTION_DATE, collectionDateList);
			resultContext.addExtractResult(Extracter.DEFAULT_RESULT_ORIGIN_URL, originUrlList);
		}
	}

	private String getId() {
		String id = workerName + "_" + jobSnapshotId + "_" + extracterIndex.getAndIncrement();
		return id;
	}

	/**
	 * 对抽取出来的结果进行加工解析
	 * 
	 * @param page
	 *            当前处理的页面对象
	 * @param preResult
	 *            抽取出来的结果
	 */
	protected String paserString(ExtractItem paserResult, Page page, String preText) {
		String newStr=preText;
		if (StringUtils.isNoneBlank(preText)) {
			if (ExtractItemType.STRING.value() == paserResult.getType()) {
				newStr=StringUtils.trim(preText);
				newStr=AutoCharsetDetectorUtils.instance().escape(newStr);
			} else if (ExtractItemType.URL.value() == paserResult.getType()) {
				String newUrl = null;
				if (!"#no".equals(preText)) {
					newUrl = UrlUtils.paserUrl(page.getBaseUrl(), page.getFinalUrl(), StringUtils.trim(preText));
				}
				newStr=newUrl;
			} else if (ExtractItemType.PHONE.value() == paserResult.getType()) {
				String[] temp = preText.split(" ");
				String telPhone = "";
				for (String word : temp) {
					if (TelPhoneUtils.isTelPhone(word)) {
						telPhone = StringUtils.trim(word);
					}
				}
				newStr=telPhone;
			} else {
				newStr=StringUtils.trim(preText);
				newStr=AutoCharsetDetectorUtils.instance().escape(newStr);
			}
		}
		return newStr;
	}

	public AbstractCrawlWorker getAbstractWorker() {
		return worker;
	}

	public List<ExtractItem> getExtractItems() {
		return extractItems;
	}
}
