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
	private static final String EmptyResult = "";
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
		List<String> preResult = null;
		if (extractItem.getType() == ExtractItemType.META.value()) {
			preResult = page.getMeta(extractItem.getOutputKey());
		} else {
			ExtractPath optimalPath = null;
			int ranking = 0;
			List<ExtractPath> pathList = extractPathMap.get(extractItem.getPathName());
			if (null == pathList) {
				pathList = worker.getManager().getPaserPathService().query(extractItem.getPathName(),
						worker.getSite().getCode());
				extractPathMap.put(extractItem.getPathName(), pathList);
			}
			if(ranking>=pathList.size()){
				throw new RuntimeException("don't find path:"+extractItem.getPathName());
			}
			optimalPath = pathList.get(ranking);
			// 如果 optimalPath 为null 那么库里根本不存在path 所以不需要继续往下处理
			if (null != optimalPath) {
				ExtractPath nowPath = optimalPath;
				try {
					// 抽取结果 result不可能为null 如果result没有结果 那么 result
					// 会用Collections.emptyList();
					preResult = extract(page, extractItem, nowPath);
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
		if ((null == preResult || preResult.isEmpty()) && extractItem.getMustHaveResult() == MustHaveResult.MUST) {
			throw new ExtractEmptyResultException(
					"extract resultKey [" + extractItem.getOutputKey() + "] value is empty");
		}
		if (null == preResult) {
			preResult = new ArrayList<>();
		}
		return preResult;
	}

	protected String paserElement(String filterPath, String reslutAttName, Element element) {
		PathFilter pathFilter = PathFilterBuilder.buildPathFilter(filterPath);
		String result = "";
		if (pathFilter.isFilter(element)) {
			result = EmptyResult;
		} else if ("text".equalsIgnoreCase(reslutAttName)) {
			StringBuilder text = new StringBuilder();
			appendText(pathFilter, element, text);
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
		Elements findElements = htmlElements.select(path.getPath());
		String result = null;
		for (Element element : findElements) {
			isAdd = true;
			result = paserElement(path.getFilterPath(), path.getExtractAttName(), element);
			// 如果null==pathResultType.getContainKeyWord() 或者 包含 指定的关键word
			// 才add
			if (null != path.getCompareAttName() && path.getCompareAttName().trim().length() > 0) {
				String compareAttValue = null;
				if (path.getExtractAttName().equalsIgnoreCase(path.getCompareAttName())) {
					compareAttValue = result;
				} else {
					compareAttValue = paserElement(null, path.getCompareAttName(), element);
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
	protected void appendText(PathFilter filterPath, Node node, StringBuilder newBf) {
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
