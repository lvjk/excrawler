package six.com.crawler.work.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.utils.TelPhoneUtils;
import six.com.crawler.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 下午9:19:54
 */
public abstract class AbstractExtracter implements Extracter {

	private AbstractCrawlWorker worker;
	private List<ExtractItem> extractItems;
	Map<String, List<ExtractPath>> extractPathMap = new HashMap<>();
	private static Set<Character> charSet = new HashSet<>();
	static {
		charSet.add(' ');
		charSet.add('\n');
		charSet.add('\t');
	}

	public AbstractExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		this.worker = worker;
		this.extractItems = extractItems;
	}

	protected List<String> extract(Page page, ExtractItem extractItem) {
		List<String> extractResult = null;
		if (extractItem.getType() == ExtractItemType.META.value()) {
			extractResult = page.getMeta(extractItem.getOutputKey());
			if (null == extractResult) {
				extractResult = new ArrayList<>();
			}
		} else {
			extractResult = new ArrayList<>();
			ExtractPath optimalPath = null;
			int ranking = 0;
			List<ExtractPath> pathList = extractPathMap.get(extractItem.getPathName());
			if (null == pathList) {
				pathList = worker.getManager().getExtractPathDao().query(extractItem.getPathName(),
						worker.getSite().getCode());
				extractPathMap.put(extractItem.getPathName(), pathList);
			}
			if (ranking >= pathList.size()) {
				throw new RuntimeException("don't find path:" + extractItem.getPathName());
			}
			optimalPath = pathList.get(ranking);
			// 如果 optimalPath 为null 那么库里根本不存在path 所以不需要继续往下处理
			if (null != optimalPath) {
				ExtractPath nowPath = optimalPath;
				try {
					// 抽取结果 result不可能为null 如果result没有结果 那么 result
					// 会用Collections.emptyList();
					List<String> tempExtractList = JsoupUtils.extract(page.getDoc(), nowPath);
					for (String extractItemStr : tempExtractList) {
						String tempExtract = paserString(extractItem, page, extractItemStr);
						extractResult.add(tempExtract);
					}
				} catch (Exception t) {
					throw new ExtractUnknownException(
							"PaserProcessorExtractUnknownException:" + extractItem.getPathName(), t);
				}

			} else {
				throw new ExtractUnknownException("don't find paser path:" + extractItem.getPathName());
			}
		}
		// 查看这个path是否是一定要有结果
		// 如果==must 没有结果的话 那么将会抛抽取 结果空 异常
		if (extractResult.isEmpty() && extractItem.getMustHaveResult() == 1) {
			throw new ExtractEmptyResultException(
					"extract resultKey [" + extractItem.getOutputKey() + "] value is empty");
		}
		return extractResult;
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
		if (StringUtils.isNoneBlank(preText)) {
			if (ExtractItemType.STRING.value() == paserResult.getType()) {
				return StringUtils.trim(preText);
			} else if (ExtractItemType.URL.value() == paserResult.getType()) {
				String newUrl = null;
				if (!"#no".equals(preText)) {
					newUrl = UrlUtils.paserUrl(page.getBaseUrl(), page.getFinalUrl(), StringUtils.trim(preText));
				}
				return newUrl;
			} else if (ExtractItemType.PHONE.value() == paserResult.getType()) {
				String[] temp = preText.split(" ");
				String telPhone = "";
				for (String word : temp) {
					if (TelPhoneUtils.isTelPhone(word)) {
						telPhone = word.trim();
					}
				}
				return telPhone;
			} else {
				return StringUtils.trim(preText);
			}
		}
		return preText;
	}

	public AbstractCrawlWorker getAbstractWorker() {
		return worker;
	}

	public List<ExtractItem> getExtractItems() {
		return extractItems;
	}
}
