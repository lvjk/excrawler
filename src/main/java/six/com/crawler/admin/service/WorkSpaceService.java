package six.com.crawler.admin.service;

import java.util.List;
import java.util.Map;

import six.com.crawler.admin.api.ResponseMsg;
import six.com.crawler.entity.Page;
import six.com.crawler.entity.WorkSpaceInfo;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月8日 下午3:25:59
 */
public interface WorkSpaceService {


	public WorkSpaceInfo getWorkSpaceInfo(String workSpaceName);
	
	/**
	 * 获取所有工作空间
	 * @return
	 */
	public ResponseMsg<List<WorkSpaceInfo>> getWorkSpaces();
	
	/**
	 * 获取workSpaceName 工作空间里的处理数据
	 * @param workSpaceName
	 * @param cursor
	 * @return Map 包含list(数据),当前查询游标(cursor)
	 */
	public ResponseMsg<Map<String,Object>> getWorkSpaceDoingData(String workSpaceName,String cursor);
	
	/**
	 * 获取workSpaceName 工作空间里的异常数据
	 * @param workSpaceName
	 * @param cursor
	 * @return Map 包含list(数据),当前查询游标(cursor)
	 */
	public ResponseMsg<Map<String,Object>> getWorkSpaceErrData(String workSpaceName, String cursor);

	/**
	 * 清空处理数据
	 * @param workSpaceName
	 * @return
	 */
	public ResponseMsg<String> clearDoing(String workSpaceName);
	
	/**
	 * 清空异常数据
	 * @param workSpaceName
	 * @return
	 */
	public ResponseMsg<String> clearErr(String workSpaceName);
	
	/**
	 * 清空完成数据
	 * @param workSpaceName
	 * @return
	 */
	public ResponseMsg<String> clearDone(String workSpaceName);

	/**
	 * 再次处理异常数据
	 * @param workSpaceName
	 * @return
	 */
	public ResponseMsg<String> againDoErrQueue(String workSpaceName);
	
	/**
	 * 往处理队列中添加数据
	 * @param workSpaceName
	 * @param page
	 * @return
	 */
	public ResponseMsg<String> AddDoing(String workSpaceName,Page page);
}
