package six.com.crawler.utils;

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

import six.com.crawler.work.extract.ExtractPath;
import six.com.crawler.work.extract.PathFilter;
import six.com.crawler.work.extract.PathFilterBuilder;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月31日 上午10:27:09
 */
public class JsoupUtils {

	private static Set<Character> charSet = new HashSet<>();

	static {
		charSet.add(' ');
		charSet.add('\n');
		charSet.add('\t');
	}

	public static Elements select(Element element, String cssQuery) {
		Elements result = null;
		if (null != element) {
			result = element.select(cssQuery);
		}
		return result;
	}

	
	public static Elements children(Element element, String childrenTag) {
		Elements findChildren=new Elements();
		if (null != element) {
			Elements allChildren=element.children();
			for(Element children:allChildren){
				if(StringUtils.equalsIgnoreCase(childrenTag, children.tagName())){
					findChildren.add(children);
				}
			}
		}
		return findChildren;
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
	public static List<String> extract(Document doc, ExtractPath path) {
		List<String> resultList = new ArrayList<String>();
		boolean isAdd = false;
		Elements htmlElements = doc.getAllElements();
		Elements findElements = htmlElements.select(path.getPath());
		String result = null;
		for (Element element : findElements) {
			isAdd = true;
			result = paserElement(path.getFilterPath(), path.getExtractAttName(), element);
			// 如果null==pathResultType.getContainKeyWord() 或者 包含 指定的关键word
			// 才add
			// 判断是否有比较 的attName
			if (StringUtils.isNotBlank(path.getCompareAttName())) {
				String compareAttValue = null;
				// 判断比较的attName是否跟抽取路径attName相等
				if (path.getExtractAttName().equalsIgnoreCase(path.getCompareAttName())) {
					compareAttValue = result;
				} else {
					compareAttValue = paserElement(StringUtils.EMPTY, path.getCompareAttName(), element);
				}
				// 如果 比较的值没有包含指定key的话那么不add
				if (!StringUtils.contains(compareAttValue, path.getContainKeyWord())) {
					isAdd = false;
				}
			}
			if (isAdd) {
				// 判断 是否有需要替换的字符
				if (StringUtils.isNotBlank(path.getReplaceWord())) {
					result = StringUtils.replace(result, path.getReplaceWord(),
							path.getReplaceValue() == null ? "" : path.getReplaceValue());
				}
				// 判断是否需要截取
				if (StringUtils.isNotBlank(path.getSubstringStart())
						|| StringUtils.isNotBlank(path.getSubstringEnd())) {
					// 如果getSubstringStart==blank,那么默认从头截取到 getSubstringEnd
					if (StringUtils.isBlank(path.getSubstringStart())) {
						result = StringUtils.substringBeforeLast(result, path.getSubstringEnd());

					} // 如果getSubstringEnd==blank,那么默认从getSubstringStart截取到尾
					else if (StringUtils.isBlank(path.getSubstringEnd())) {
						result = StringUtils.substringAfterLast(result, path.getSubstringStart());
					} else {
						result = StringUtils.substringBetween(result, path.getSubstringStart(), path.getSubstringEnd());
					}
				}
				resultList.add(result);
			}

		}
		return resultList;
	}

	private static String paserElement(String filterPath, String reslutAttName, Element element) {
		PathFilter pathFilter = PathFilterBuilder.buildPathFilter(filterPath);
		String result = null;
		if (pathFilter.isFilter(element)) {
			result = "";
		} else if ("text".equalsIgnoreCase(reslutAttName)) {
			StringBuilder text = new StringBuilder();
			appendText(pathFilter, element, text);
			result = text.toString();
		} else {
			result = element.attr(reslutAttName);
			result = StringUtils.trim(result);
		}
		// 替换特殊字符
		result = AutoCharsetDetectorUtils.instance().escape(result);
		return result;
	}

	/**
	 * <table>
	 * <tr>
	 * <td>key</td>
	 * <td>value</td>
	 * <td>key</td>
	 * <td>value</td>
	 * </tr>
	 * <tr>
	 * <td>key</td>
	 * <td>value</td>
	 * <td>key</td>
	 * <td>value</td>
	 * </tr>
	 * </table>
	 * 
	 * @param table
	 * @return
	 */
	@Deprecated
	public static List<TableResult> paserTable(Document table) {
		Elements trElements = table.getElementsByTag("tr");
		Element trElement = null;
		List<TableResult> result = new ArrayList<>();
		TableResult tableResult = null;
		for (int i = 0; i < trElements.size(); i++) {
			trElement = trElements.get(i);
			Elements tdElements = trElement.getElementsByTag("td");
			Element tdElement = null;
			for (int j = 0; j < tdElements.size();) {
				tableResult = new TableResult();
				tdElement = tdElements.get(j);
				String key = paserElement(PathFilter.EmptyFilterElement, "text", tdElement);
				key = StringUtils.remove(key, " ");
				if (StringUtils.isNotBlank(key)) {
					String value = "";
					int tempIndex = j + 1;
					if (tempIndex < tdElements.size()) {
						tdElement = tdElements.get(tempIndex);
						value = paserElement(PathFilter.EmptyFilterElement, "text", tdElement);
					}
					tableResult.setKey(key);
					tableResult.setValue(value);
					result.add(tableResult);
				}
				j += 2;
			}
		}
		return result;
	}

	/**
	 * 
	 * @param table
	 * @param headCssSelect
	 *            table>tbody>tr>td
	 * @param dataCssSelect
	 *            table>tbody>tr
	 * @return
	 */
	@Deprecated
	public static Map<String, List<String>> paserTable(Element table, String headCssSelect, String dataCssSelect) {
		Elements headElements = table.select(headCssSelect);
		Elements dataElements = table.select(dataCssSelect);
		String[] fields = new String[headElements.size()];
		for (int i = 0; i < headElements.size(); i++) {
			Element fieldElement = headElements.get(i);
			fields[i] = StringUtils.remove(fieldElement.text(), " ");
		}
		Map<String, List<String>> resultMap = new HashMap<>();
		for (int i = 0; i < dataElements.size(); i++) {
			Element dataTrElement = dataElements.get(i);
			Elements tdElements = dataTrElement.getElementsByTag("td");
			for (int j = 0; j < tdElements.size(); j++) {
				Element tdElement = tdElements.get(j);
				String result = tdElement.text();
				resultMap.computeIfAbsent(fields[j], mapKey -> new ArrayList<>()).add(result);
			}
		}
		return resultMap;
	}

	public static List<TableResult> paserTable(Element table) {
		Elements trElements = table.getElementsByTag("tr");
		Element trElement = null;
		List<TableResult> result = new ArrayList<>();
		TableResult tableResult = null;
		for (int i = 0; i < trElements.size(); i++) {
			trElement = trElements.get(i);
			Elements tdElements = trElement.getElementsByTag("td");
			Element tdElement = null;
			if (1 == tdElements.size()) {
				continue;
			} else {
				for (int j = 0; j < tdElements.size();) {
					tableResult = new TableResult();
					tdElement = tdElements.get(j);
					String key = paserElement(PathFilter.EmptyFilterElement, "text", tdElement);
					key = StringUtils.trim(key);
					if (StringUtils.isNotBlank(key)) {
						String value = "";
						int tempIndex = j + 1;
						if (tempIndex < tdElements.size()) {
							tdElement = tdElements.get(tempIndex);
							value = paserElement(PathFilter.EmptyFilterElement, "text", tdElement);
						}
						tableResult.setKey(key);
						tableResult.setValue(value);
						result.add(tableResult);
					}
					j += 2;
				}
			}
		}
		return result;
	}

	/**
	 * 表格单条数据抽取,例如
	 * <p>名称:小明    性别:男 </p>
	 * <p>年龄:20       爱好:体育 </p>
	 * @param table
	 * @return
	 */
	public static Map<String, String> paserTableForOne(Element table) {
		ObjectCheckUtils.checkNotNull(table, "table");
		Map<String, String> resultMap = new HashMap<>();
		Elements trElements = table.getElementsByTag("tr");
		Element trElement = null;
		for (int i = 0; i < trElements.size(); i++) {
			trElement = trElements.get(i);
			Elements tdElements = trElement.getElementsByTag("td");
			Element tdElement = null;
			if (tdElements.size()>1) {
				for (int j = 0; j < tdElements.size();) {
					tdElement = tdElements.get(j);
					String key = paserElement(PathFilter.EmptyFilterElement, "text", tdElement);
					key = StringUtils.trim(key);
					if (StringUtils.isNotBlank(key)) {
						String value = "";
						int tempIndex = j + 1;
						if (tempIndex < tdElements.size()) {
							tdElement = tdElements.get(tempIndex);
							value = paserElement(PathFilter.EmptyFilterElement, "text", tdElement);
						}
						resultMap.put(key, value);
					}
					j += 2;
				}
			
			} 
		}
		return resultMap;
	}

	/**
	 * 表格多条数据数据抽取,例如:
	 * <p>
	 * 名称 年龄 性别(headCssSelect:列名)
	 * </p>
	 * <p>
	 * 小明1 18 男
	 * </p>
	 * <p>
	 * 小明2 19 男
	 * </p>
	 * <p>
	 * 小明3 20 男
	 * </p>
	 * 
	 * @param table
	 * @param headCssSelect
	 *            列名
	 * @param dataCssSelect
	 *            表格数据td
	 * @return
	 */
	public static Map<String, List<String>> paserTableForMany(Element table, String headCssSelect,
			String dataCssSelect) {
		ObjectCheckUtils.checkNotNull(table, "table");
		StringCheckUtils.checkStrBlank(headCssSelect, "headCssSelect");
		StringCheckUtils.checkStrBlank(dataCssSelect, "dataCssSelect");
		Elements headElements = table.select(headCssSelect);
		if (null == headElements || headElements.isEmpty()) {
			throw new RuntimeException("did not find table's head:" + headCssSelect);
		}
		String[] fields = new String[headElements.size()];
		for (int i = 0; i < headElements.size(); i++) {
			Element fieldElement = headElements.get(i);
			fields[i] = StringUtils.remove(fieldElement.text(), " ");
		}
		Elements dataElements = table.select(dataCssSelect);
		Map<String, List<String>> resultMap = new HashMap<>();
		for (int i = 0; i < dataElements.size(); i++) {
			Element dataTrElement = dataElements.get(i);
			Elements tdElements = dataTrElement.getElementsByTag("td");
			for (int j = 0; j < tdElements.size(); j++) {
				Element tdElement = tdElements.get(j);
				String result = tdElement.text();
				resultMap.computeIfAbsent(fields[j], mapKey -> new ArrayList<>()).add(result);
			}
		}
		return resultMap;
	}

	public static String paserElement(PathFilter filterPath, String reslutAttName, Element element) {
		String result = "";
		if (filterPath.isFilter(element)) {
			return result;
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
		result = StringUtils.removeStart(result, "\n");
		return result;
	}

	/**
	 * 组装获取的页面字符
	 * 
	 * @param node
	 * @param newBf
	 * @param pathResultType
	 */
	private static void appendText(PathFilter filterPath, Node node, StringBuilder newBf) {
		if (node instanceof Element) {
			if (filterPath.isFilter((Element) node)) {
				newBf.append("");
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

	public static class TableResult {
		public String getKey() {
			return key;
		}

		public void setKey(String key) {
			this.key = key;
		}

		public String getValue() {
			return value;
		}

		public void setValue(String value) {
			this.value = value;
		}

		private String key;
		private String value;
	}
}