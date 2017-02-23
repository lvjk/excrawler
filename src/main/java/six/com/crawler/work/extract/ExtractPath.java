package six.com.crawler.work.extract;


/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年5月17日 下午7:48:04 类说明
 */
public class ExtractPath {

	private String sitecode;// 站点id
	private String selectors;
	private String reslutAttName;// 要获取的值的属性名字
	private String appendHead; // 在头部追加字符
	private String appendEnd;// 在尾部追加字符
	private FilterPath filterPath;
	private int depth;// 页面深度
	private int emptyExtractCount;// 抽取结果为空的计数
	private String compareAttName;//用来比较 的att name
	private String containKeyWord;// 包含的关键字
	private String replaceWord;// 要替换的词
	private String replaceValue;// 替换的值
	private String describe;// 描述

	public String getSitecode() {
		return sitecode;
	}

	public void setSitecode(String sitecode) {
		this.sitecode = sitecode;
	}

	public String getReslutAttName() {
		return reslutAttName;
	}

	public String getAppendHead() {
		return appendHead;
	}

	public void setAppendHead(String appendHead) {
		this.appendHead = appendHead;
	}

	public String getAppendEnd() {
		return appendEnd;
	}

	public void setAppendEnd(String appendEnd) {
		this.appendEnd = appendEnd;
	}

	public int getEmptyExtractCount() {
		return emptyExtractCount;
	}

	public void setEmptyExtractCount(int emptyExtractCount) {
		this.emptyExtractCount = emptyExtractCount;
	}

	public int getDepth() {
		return depth;
	}

	public void setDepth(int depth) {
		this.depth = depth;
	}

	public FilterPath getFilterPath() {
		return filterPath;
	}

	public void setFilterPath(String filterPath) {
		this.filterPath = FilterElementBuilder.buildFilterElement(filterPath);
	}

	public String getSelector() {
		return selectors;
	}

	public void setPath(String path) {
		this.selectors =path;
	}

	public void setReslutAttName(String reslutAttName) {
		this.reslutAttName = reslutAttName;
	}

	public String getContainKeyWord() {
		return containKeyWord;
	}

	public void setContainKeyWord(String containKeyWord) {
		this.containKeyWord = containKeyWord;
	}

	public String getReplaceWord() {
		return replaceWord;
	}

	public void setReplaceWord(String replaceWord) {
		this.replaceWord = replaceWord;
	}

	public String getReplaceValue() {
		return replaceValue;
	}

	public void setReplaceValue(String replaceValue) {
		this.replaceValue = replaceValue;
	}

	public String getDescribe() {
		return describe;
	}

	public void setDescribe(String describe) {
		this.describe = describe;
	}
	public String getCompareAttName() {
		return compareAttName;
	}

	public void setCompareAttName(String compareAttName) {
		this.compareAttName = compareAttName;
	}
}
