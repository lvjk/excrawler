package six.com.crawler.common.utils;

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

import six.com.crawler.work.extract.PathFilter;

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
	
	/**
	 * <table>
	 * 	<tr><td>key</td><td>value</td><td>key</td><td>value</td>
	 * 	</tr>
	 * 	<tr><td>key</td><td>value</td><td>key</td><td>value</td>
	 * 	</tr>
	 * </table>
	 * @param table
	 * @return
	 */
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
	 * @param headCssSelect    table>tbody>tr>td 
	 * @param dataCssSelect    table>tbody>tr
	 * @return
	 */
	public static Map<String,List<String>> paserTable(Element table,String headCssSelect,String dataCssSelect) {
		Elements headElements=table.select(headCssSelect);
		Elements dataElements=table.select(dataCssSelect);
		String[] fields=new String[headElements.size()];
		for (int i = 0; i < headElements.size(); i++) {
			Element fieldElement=headElements.get(i);
			fields[i]=fieldElement.text();
		}
		Map<String,List<String>> resultMap=new HashMap<>();
		for (int i = 0; i < dataElements.size(); i++) {
			Element dataTrElement=dataElements.get(i);
			Elements tdElements = dataTrElement.getElementsByTag("td");
			for (int j = 0; j < tdElements.size();j++) {
				Element tdElement = tdElements.get(j);
				String result=tdElement.text();
				resultMap.computeIfAbsent(fields[j],mapKey -> new ArrayList<>()).add(result);
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
			if(1==tdElements.size()){
				continue;
			}else{
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