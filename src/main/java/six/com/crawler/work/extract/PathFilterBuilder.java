package six.com.crawler.work.extract;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.StringUtils;

/**
 * @author six
 * @date 2016年8月25日 下午4:23:35
 */
public class PathFilterBuilder {

	public final static String EMPTY_FILTER_ELEMENT = "EMPTY";

	public static PathFilter buildPathFilter(String filterPath) {
		if (null == filterPath || StringUtils.isBlank(filterPath)
				|| EMPTY_FILTER_ELEMENT.equalsIgnoreCase(filterPath)) {
			return PathFilter.EmptyFilterElement;
		}
		List<PathFilter> elements = new ArrayList<>();
		String[] filterElements = filterPath.split("\\|");
		int start = -1;
		int end = -1;
		PathFilter element = null;
		String startHead = "[@";
		String endHead = "=";
		String endHead1 = "']";
		String endHead2 = "\"]";
		for (String str : filterElements) {
			element = new PathFilter();
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
		PathFilter fe = new PathFilter();
		fe.elements = elements;
		return fe;

	}

}
