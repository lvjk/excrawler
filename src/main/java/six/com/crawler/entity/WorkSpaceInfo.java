package six.com.crawler.entity;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年12月19日 上午9:55:41 
*/
public class WorkSpaceInfo{

	/**
	 * 工作空间 名称
	 */
	private String workSpaceName;
	
	/**
	 * 处理数据数量
	 */
	private int doingSize;
	
	/**
	 * 异常数据数量
	 */
	private int errSize;
	
	/**
	 * 处理掉的数据数量
	 */
	private int doneSize;
	
	public String getWorkSpaceName() {
		return workSpaceName;
	}

	public void setWorkSpaceName(String workSpaceName) {
		this.workSpaceName = workSpaceName;
	}

	public int getDoingSize() {
		return doingSize;
	}

	public void setDoingSize(int doingSize) {
		this.doingSize = doingSize;
	}

	public int getErrSize() {
		return errSize;
	}

	public void setErrSize(int errSize) {
		this.errSize = errSize;
	}

	public int getDoneSize() {
		return doneSize;
	}

	public void setDoneSize(int doneSize) {
		this.doneSize = doneSize;
	}
	
}
