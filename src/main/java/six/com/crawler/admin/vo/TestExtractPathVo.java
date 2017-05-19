package six.com.crawler.admin.vo;

import six.com.crawler.work.extract.ExtractPath;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月9日 下午2:39:03 
*/
public class TestExtractPathVo extends ExtractPath{

	/**
	 * 
	 */
	private static final long serialVersionUID = 2664280926182012740L;
	
	private String testUrl;
	private String testHtml;
	
	public String getTestHtml() {
		return testHtml;
	}
	public void setTestHtml(String testHtml) {
		this.testHtml = testHtml;
	}
	public String getTestUrl() {
		return testUrl;
	}
	public void setTestUrl(String testUrl) {
		this.testUrl = testUrl;
	}
}
