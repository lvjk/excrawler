package six.com.crawler.work.downer;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 
 * 下载工具类
 * @author weijiyong@tospur.com
 *
 */
public class DownerHelper {
	
	SimpleDateFormat df = new SimpleDateFormat("yyyyMMdd");//设置日期格式
	
	private String rawdataBasePath;
	
	private boolean isSaveRawData;
	
	private String siteCode;
	
	private String jobName;
	
	private boolean isUseRawData;
	
	private boolean downloadState;
	
	/**
	 * 获得源数据存储路径
	 * @return
	 */
	public String getRawDataPath(){
		StringBuffer pathbf=new StringBuffer();
		pathbf.append(rawdataBasePath);
		pathbf.append("/");
		pathbf.append(siteCode);
		pathbf.append("/");
		pathbf.append(df.format(new Date()));
		pathbf.append("/");
		pathbf.append(jobName);
		return pathbf.toString();
	}
	
	public String getRawdataBasePath() {
		return rawdataBasePath;
	}

	public void setRawdataBasePath(String rawdataBasePath) {
		this.rawdataBasePath = rawdataBasePath;
	}

	public boolean isSaveRawData() {
		return isSaveRawData;
	}

	public void setSaveRawData(boolean isSaveRawData) {
		this.isSaveRawData = isSaveRawData;
	}

	public String getSiteCode() {
		return siteCode;
	}

	public void setSiteCode(String siteCode) {
		this.siteCode = siteCode;
	}

	public String getJobName() {
		return jobName;
	}

	public void setJobName(String jobName) {
		this.jobName = jobName;
	}

	public boolean isUseRawData() {
		return isUseRawData;
	}

	public void setUseRawData(boolean isUseRawData) {
		this.isUseRawData = isUseRawData;
	}

	public boolean isDownloadState() {
		return downloadState;
	}

	public void setDownloadState(boolean downloadState) {
		this.downloadState = downloadState;
	}
}
