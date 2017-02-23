package six.com.crawler.work.extract;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.select.Elements;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.utils.TelPhoneUtils;
import six.com.crawler.common.utils.UrlUtils;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 下午9:19:54
 */
public abstract class AbstractExtracter implements Extracter {

	private AbstractCrawlWorker worker;
	private List<ExtractItem> extractItems;
	private Map<String, ExtractPath> paserPathMap;
	private int defaultEmptyExtractCountMax = 3;
	private static final String EmptyResult = "";
	private static Set<Character> charSet = new HashSet<>();
	static {
		charSet.add(' ');
		charSet.add('\n');
		charSet.add('\t');
	}

	public AbstractExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		this.worker = worker;
		this.extractItems = extractItems;
		paserPathMap = new HashMap<>();
	}

	protected List<String> extract(Page page, ExtractItem paserItem) {
		List<String> preResult = null;
		if (paserItem.getType() == ExtractItemType.META.value()) {
			preResult = page.getMeta(paserItem.getResultKey());
		} else {
			ExtractPath optimalPath = paserPathMap.get(paserItem.getPathName());
			// 排名 默认1是最优 >1> 2 ......
			if (null == optimalPath) {
				// 如果最优的path==null 那么获取评分最高的Path list>0 至少会获取一个 path
				optimalPath = worker.getManager().getPaserPathService().queryPath(page.getSiteCode(),
						paserItem.getPathName(), 1);
			}
			// 如果 optimalPath 为null 那么库里根本不存在path 所以不需要继续往下处理
			if (null != optimalPath) {
				ExtractPath nowPath = optimalPath;
				int ranking = 1;
				int tempEmptyExtractCount = 0;
				while (true) {
					try {
						// 抽取结果 result不可能为null 如果result没有结果 那么 result
						// 会用Collections.emptyList();
						preResult = extract(page, paserItem, nowPath);
						if (!preResult.isEmpty()) {
							// 如果抽取到结果那么 直接break;
							break;
						} else {
							// 临时空抽取计数增量
							tempEmptyExtractCount++;
							// 设置 path 空抽取次数
							nowPath.setEmptyExtractCount(nowPath.getEmptyExtractCount() + 1);
							// 将path 更新至库里
							worker.getManager().getPaserPathService().updatePaserPath(nowPath);
							// 如果 path 临时空抽取达到默认次数那么将重新更新 optimalPath 的赋值
							ranking++;// 排位增量
							// 从库里按照path 排名迭代获取路径
							nowPath = worker.getManager().getPaserPathService().queryPath(page.getSiteCode(),
									paserItem.getPathName(), ranking);
							if (tempEmptyExtractCount >= defaultEmptyExtractCountMax) {
								// 如果最优的path==null 那么获取评分最高的Path list>0
								// 至少会获取一个
								// path
								paserPathMap.put(paserItem.getPathName(), nowPath);
								tempEmptyExtractCount = 0;
							}
							if (null == nowPath) {
								break;
							}
						}
					} catch (Exception t) {
						throw new ExtractUnknownException(
								"PaserProcessorExtractUnknownException:" + paserItem.getPathName(), t);
					}
				}
			} else {
				throw new ExtractUnknownException("don't find paser path:" + paserItem.getPathName());
			}
		}
		// 查看这个path是否是一定要有结果
		// 如果==must 没有结果的话 那么将会抛抽取 结果空 异常
		if ((null == preResult || preResult.isEmpty()) && paserItem.getMustHaveResult() == MustHaveResult.MUST) {
			throw new ExtractEmptyResultException(
					"extract resultKey [" + paserItem.getResultKey() + "] value is empty");
		}
		if (null == preResult) {
			preResult = new ArrayList<>();
		}
		return preResult;
	}

	protected String paserElement(FilterPath filterPath, String reslutAttName, Element element) {
		String result = "";
		if (filterPath.isFilter(element)) {
			result = EmptyResult;
		} else if ("text".equalsIgnoreCase(reslutAttName)) {
			StringBuilder text = new StringBuilder();
			appendText(filterPath, element, text);
			result = text.toString();
		} else {
			result = element.attr(reslutAttName);
			result = StringUtils.trim(result);
		}
		// 替换空格 &nbsp;
		result = StringUtils.replace(result, "&nbsp;", " ");
		return result;
	}

	/**
	 * 抽取
	 * 
	 * @param page
	 *            当前处理的页面对象
	 * @param path
	 *            抽取结果path
	 * @return
	 */
	protected List<String> extract(Page page, ExtractItem paserItem, ExtractPath path) {
		List<String> resultList = new ArrayList<String>();
		boolean isAdd;
		Document doc = page.getDoc();
		Elements htmlElements = doc.getAllElements();
		Elements findElements = htmlElements.select(path.getSelector());
		String result = null;
		for (Element element : findElements) {
			isAdd = true;
			result = paserElement(path.getFilterPath(), path.getReslutAttName(), element);
			// 如果null==pathResultType.getContainKeyWord() 或者 包含 指定的关键word
			// 才add
			if (null != path.getCompareAttName() && path.getCompareAttName().trim().length() > 0) {
				String compareAttValue = null;
				if (path.getReslutAttName().equalsIgnoreCase(path.getCompareAttName())) {
					compareAttValue = result;
				} else {
					compareAttValue = paserElement(FilterPath.EmptyFilterElement, path.getCompareAttName(), element);
				}
				if (!compareAttValue.contains(path.getContainKeyWord())) {
					isAdd = false;
				}
			}
			if (isAdd) {
				// 替换包含的字词
				if (null != path.getReplaceWord()) {
					result = StringUtils.replace(result, path.getReplaceWord(),
							path.getReplaceValue() == null ? "" : path.getReplaceValue());
				}
				result = paserString(paserItem, page, result);
				resultList.add(result);
			}

		}
		return resultList;
	}

	/**
	 * 通过 selector 获取
	 * 
	 * @param doc
	 * @param selector
	 * @return
	 */
	protected Elements selectorElements(Element htmlElement, Selector selector) {
		Elements findElements = findElements(htmlElement, selector);
		while (findElements.size() > 0 && null != selector.getNextSelector()) {
			selector = selector.getNextSelector();
			findElements = findElements(findElements, selector);
		}
		return findElements;
	}

	/**
	 * 通过Index获取
	 * 
	 * @param elements
	 * @param temp
	 * @return
	 */
	protected Elements getIndex(Elements elements, Selector temp) {
		if (temp.getIndex() > elements.size()) {
			elements.clear();
		} else {
			if (temp.getIndex() > 0 && elements.size() > temp.getIndex() - 1) {
				Element findElemen = elements.get(temp.getIndex() - 1);
				elements.clear();
				elements.add(findElemen);
			}
		}
		return elements;
	}

	/**
	 * 校验text是否为有效文本
	 * 
	 * @param text
	 * @return
	 */
	private static boolean validText(String text) {
		for (int i = 0; i < text.length(); i++) {
			if (!charSet.contains(text.charAt(i))) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 组装获取的页面字符
	 * 
	 * @param node
	 * @param newBf
	 * @param pathResultType
	 */
	protected void appendText(FilterPath filterPath, Node node, StringBuilder newBf) {
		if (node instanceof Element) {
			if (filterPath.isFilter((Element) node)) {
				newBf.append(EmptyResult);
				return;
			}
		}

		if ("#text".equals(node.nodeName())) {
			String text = node.outerHtml();
			if (validText(text)) {
				newBf.append(text);
			}
		} else {
			String nodeName = null;
			for (Node childNode : node.childNodes()) {
				appendText(filterPath, childNode, newBf);
				nodeName = node.nodeName();
				if ("p".equals(nodeName)) {
					newBf.append("\n");
				} else if ("br".equals(nodeName)) {
					newBf.append("\n");
				} else if ("pre".equals(nodeName)) {
					newBf.append("\n");
				}
			}
		}

	}

	protected static Elements findElements(Element element, Selector temp) {
		Elements newElements = null;
		Elements findElements = element.select(temp.getCssQuery());
		if (temp.getIndex() <= 0) {
			newElements = findElements;
		} else {
			newElements = new Elements();
			if (temp.getIndex() <= findElements.size()) {
				Element newElement = findElements.get(temp.getIndex() - 1);
				newElements.add(newElement);
			}
		}
		return newElements;
	}

	protected static Elements findElements(Elements elements, Selector temp) {
		Elements newElements = new Elements();
		for (Element childElement : elements) {
			Elements tempElements = findElements(childElement, temp);
			newElements.addAll(tempElements);
		}
		return newElements;
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

	public AbstractCrawlWorker getAbstractWorker() {
		return worker;
	}

	public List<ExtractItem> getExtractItems() {
		return extractItems;
	}
}
