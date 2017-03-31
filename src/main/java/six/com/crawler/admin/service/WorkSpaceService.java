package six.com.crawler.admin.service;

import java.util.List;
import java.util.Map;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.entity.DoneInfo;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.WorkSpaceInfo;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月8日 下午3:25:59
 */
public interface WorkSpaceService {


	public WorkSpaceInfo getWorkSpaceInfo(String workSpaceName);
	
	public ResponseMsg<List<WorkSpaceInfo>> getWorkSpaces();
	
	public Map<String,Object> getWorkSpaceDoingData(String workSpaceName,String cursor);
	
	public List<Page> getWorkSpaceErrData(String workSpaceName, String cursor);

	public List<DoneInfo> getQueueDones();

	public ResponseMsg<String> clearDoing(String workSpaceName);
	
	public ResponseMsg<String> clearErr(String workSpaceName);
	
	public ResponseMsg<String> clearDone(String workSpaceName);

	public String againDoErrQueue(String workSpaceName);
}
