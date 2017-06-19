package six.com.crawler.work;

import java.io.Serializable;
import java.util.Random;

import six.com.crawler.entity.JobSnapshot;
import six.com.crawler.utils.MD5Utils;
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
	
	public MonitorData(){}
	
	public MonitorData(JobSnapshot job){
		Random rondom= new Random();
		key=job.getWorkSpaceName()+"-"+job.getName()+"-"+MD5Utils.MD5(job.getName()+rondom.nextInt(100000));
	}
	
	private Index index;

	@Override
	public void setIndex(Index index) {
		this.index=index;
	}

	@Override
	public Index getIndex() {
		return this.index;
	}

	@Override
	public String getKey() {
		return key;
	}
	
}
