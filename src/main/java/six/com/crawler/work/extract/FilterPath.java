package six.com.crawler.work.extract;

import java.util.List;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

/**
 * @author six
 * @date 2016年8月23日 下午4:09:29
 */
public class FilterPath {

	protected String tagName;
	protected String attName;
	protected String attValue;
	protected List<FilterPath> elements;

	public static FilterPath EmptyFilterElement=new FilterPath(){
		public Element filter(Element element) {
			return element;
		}
		public boolean isFilter(Element element) {
			return false;
		}
	};
	public Element filter(Element element) {
		for (FilterPath felement : elements) {
			if (null == felement.attName) {
				if (element.tagName().equals(felement.tagName)) {
					return null;
				}
			} else {
				String eleValue = element.attr(felement.attName);
				if (element.tagName().equals(felement.tagName) && felement.attValue.equals(eleValue)) {
					return null;
				}
			}
		}
		Elements list = element.children();
		Element newElement = null;
		for (int i = 0; i < list.size(); i++) {
			newElement = list.get(i);
			if (null == filter(newElement)) {
				newElement.remove();
			}
		}
		return element;

	}

	public boolean isFilter(Element element) {
		for (FilterPath felement : elements) {
			if (null == felement.attName) {
				if (element.tagName().equals(felement.tagName)) {
					return true;
				}
			} else {
				String eleValue = element.attr(felement.attName);
				if (element.tagName().equals(felement.tagName) && felement.attValue.equals(eleValue)) {
					return true;
				}
			}
		}
		Elements list = element.children();
		Element newElement = null;
		for (int i = 0; i < list.size(); i++) {
			newElement = list.get(i);
			if (null == filter(newElement)) {
				newElement.remove();
			}
		}
		return false;

	}
}
