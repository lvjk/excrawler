package six.com.crawler.work.extract;

import java.util.ArrayList;
import java.util.List;

/**
 * @author six
 * @date 2016年8月25日 下午4:23:35
 */
public class FilterElementBuilder {

	public final static String EMPTY_FILTER_ELEMENT = "EMPTY";

	public static FilterPath buildFilterElement(String filterElement) {
		if (EMPTY_FILTER_ELEMENT.equalsIgnoreCase(filterElement)) {
			return FilterPath.EmptyFilterElement;
		}
		if (null != filterElement && filterElement.trim().length() > 0) {
			List<FilterPath> elements = new ArrayList<>();
			String[] filterElements = filterElement.split("\\|");
			int start = -1;
			int end = -1;
			FilterPath element = null;
			String startHead = "[@";
			String endHead = "=";
			String endHead1 = "']";
			String endHead2 = "\"]";
			for (String str : filterElements) {
				element = new FilterPath();
				if ((start = str.indexOf(startHead)) != -1) {
					element.tagName = str.substring(0, start);
					end = str.indexOf(endHead);
					start += startHead.length();
					element.attName = str.substring(start, end);
					start = end + endHead.length() + 1;
					if ((end = str.indexOf(endHead1)) == -1) {
						end = str.indexOf(endHead2);
					}
					element.attValue = str.substring(start, end);
				} else {
					element.tagName = str;
				}
				elements.add(element);
			}
			FilterPath fe = new FilterPath();
			fe.elements = elements;
			return fe;
		} else {
			return FilterPath.EmptyFilterElement;
		}
	}

}
