package six.com.crawler.work;

import java.io.Serializable;

import six.com.crawler.work.space.Index;
import six.com.crawler.work.space.WorkSpaceData;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年6月16日 上午11:55:04 
*/
public class MonitorData implements WorkSpaceData,Serializable{

	private static final long serialVersionUID = -6303001982409677119L;
	
	private String key;

	private Index index;

	@Override
	public void setIndex(Index index) {
		this.index=index;
	}

	@Override
	public Index getIndex() {
		return this.index;
	}

	public void setKey(String key) {
		this.key=key;
	}
	
	@Override
	public String getKey() {
		return key;
	}
	
}
