package six.com.crawler.common;

import java.io.File;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import six.com.crawler.common.entity.JobProfile;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午3:46:35
 */
public class JobConfigTest {

	public static void main(String[] args) {
		try {
			File file = new File("F:/jobTemplate/jobConfigTemplate.xml");
			JAXBContext jaxbC = JAXBContext.newInstance(JobProfile.class);
			Unmarshaller us = jaxbC.createUnmarshaller();
			JobProfile jobProfile = (JobProfile) us.unmarshal(file);
			System.out.println("extractPath size:"+jobProfile.getExtractPaths().size());
			System.out.println("extractItem size:"+jobProfile.getExtractItems().size());
			System.out.println("Job size:"+jobProfile.getJobs().size());
		} catch (JAXBException e) {
			e.printStackTrace();
		}

	}

	public void jobToxml() {

	}

}
