package six.com.crawler.common.entity;

import java.util.List;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午4:05:36
 */
@XmlRootElement(name = "jobProfile")
public class JobProfile {

	private Site site;

	private List<ExtractPath> extractPaths;

	private List<ExtractItem> extractItems;

	private List<Job> jobs;

	public Site getSite() {
		return site;
	}

	@XmlElement(name = "site")
	public void setSite(Site site) {
		this.site = site;
	}

	@XmlElement(name = "extractPath")
	public List<ExtractPath> getExtractPaths() {
		return extractPaths;
	}

	public void setExtractPaths(List<ExtractPath> extractPaths) {
		this.extractPaths = extractPaths;
	}

	@XmlElement(name = "extractItem")
	public List<ExtractItem> getExtractItems() {
		return extractItems;
	}

	public void setExtractItems(List<ExtractItem> extractItems) {
		this.extractItems = extractItems;
	}

	@XmlElement(name = "job")
	public List<Job> getJobs() {
		return jobs;
	}

	public void setJobs(List<Job> jobs) {
		this.jobs = jobs;
	}
}
