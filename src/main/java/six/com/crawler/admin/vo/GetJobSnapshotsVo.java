package six.com.crawler.admin.vo;

import java.util.List;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年5月8日 下午7:09:28 
*/
public class GetJobSnapshotsVo {

	public List<String> getJobNames() {
		return jobNames;
	}
	public void setJobNames(List<String> jobNames) {
		this.jobNames = jobNames;
	}
	public List<String> getWorkSpaceNames() {
		return workSpaceNames;
	}
	public void setWorkSpaceNames(List<String> workSpaceNames) {
		this.workSpaceNames = workSpaceNames;
	}
	private List<String> jobNames;
	private List<String> workSpaceNames;
}
