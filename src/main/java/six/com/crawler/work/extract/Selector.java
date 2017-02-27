package six.com.crawler.work.extract;

import java.util.ArrayList;
import java.util.List;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月16日 下午8:33:28 类说明
 * html 元素 选择器
 */
public class Selector {

	private String reslutAttName;// 要获取的值的属性名字
	private String cssQuery;// jsoup cssQuery
	private int index;//第几个元素
	private Selector nextSelector;
	
	public Selector getNextSelector() {
		return nextSelector;
	}

	public void setNextSelector(Selector nextSelector) {
		this.nextSelector = nextSelector;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getCssQuery() {
		return cssQuery;
	}

	public void setCssQuery(String cssQuery) {
		this.cssQuery = cssQuery;
	}

	public String getReslutAttName() {
		return reslutAttName;
	}

	public void setReslutAttName(String reslutAttName) {
		this.reslutAttName = reslutAttName;
	}

	/**
	 * new Selector by path string
	 * @param path
	 * @return
	 */
	public static List<Selector> newSelector(String path) {
		String split = ">";
		String perhapsSplit = "\\|";
		String[] selectors = path.split(perhapsSplit);
		List<Selector> list = new ArrayList<>(selectors.length);
		for (String selectorStr : selectors) {
			String[] cssQuerys=selectorStr.split(split);
			Selector head =null;
			for (String cssQuery : cssQuerys) {
				Selector selector = new Selector();
				if(null==head){
					head=selector;
				}else{
					Selector temp=head;
					while(null!=temp.getNextSelector()){
						temp=temp.getNextSelector();
					}
					temp.setNextSelector(selector);
				}
				cssQuery=getIndex(selector, cssQuery);
				selector.setCssQuery(cssQuery);
			}
			list.add(head);
		}
		return list;
	}
	
	private static String getIndex(Selector selector,String cssQuery){
		int end=cssQuery.length()-1;
		int start=0;
		if(')'==cssQuery.charAt(cssQuery.length()-1)){
			for(int i=cssQuery.length()-1;i>=0;i--){
				if('('==cssQuery.charAt(i)){
					start=i;
					break;
				}
			}
			if(end!=start&&start<end){
				String temp=cssQuery.substring(start+1, end);
				int index=Integer.valueOf(temp);
				selector.setIndex(index);
				cssQuery=cssQuery.substring(0, start);
			}
		}
		return cssQuery;
	}
	
}
