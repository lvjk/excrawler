package six.com.crawler.common;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

import six.com.crawler.common.entity.JobProfile;
import six.com.crawler.common.entity.SiteProfile;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午3:46:35
 */
public class ProfileTest {

	public static void main(String[] args) throws IOException {
		testJobProfile();
		System.out.println("------------------------------------------------------");
		testSiteProfile();
	}
	
	public static void testJobProfile() throws IOException{
		File readFile = new File("C:/Users/38134/Downloads/chongqi_cqgtfw_gov_presell_info_job.xml");
		File writefile = new File("F:/six/git/exCrawler/src/test/java/six/com/crawler/common/jobProfile.xml");
		String jobProfileXml = FileUtils.readFileToString(readFile);
		JobProfile jobProfile = JobProfile.buildJobProfile(jobProfileXml);
		jobProfileXml=JobProfile.buildXml(jobProfile);
		FileUtils.write(writefile, jobProfileXml);
		System.out.println(jobProfileXml);
	}
	
	public static void testSiteProfile() throws IOException{
		File readFile = new File("C:/Users/38134/Downloads/chongqi_cqgtfw_gov_site.xml");
		File writefile = new File("F:/six/git/exCrawler/src/test/java/six/com/crawler/common/siteProfile.xml");
		String siteProfileXml = FileUtils.readFileToString(readFile);
		SiteProfile siteProfile = SiteProfile.buildSiteProfile(siteProfileXml);
		siteProfileXml=SiteProfile.buildXml(siteProfile);
		FileUtils.write(writefile, siteProfileXml);
		System.out.println(siteProfileXml);
	}
}
