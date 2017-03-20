package six.com.crawler.entity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.w3c.dom.Node;

import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月24日 下午4:05:36
 */
public class JobProfile extends Profile {

	private Job job;

	private List<ExtractItem> extractItems;

	public List<ExtractItem> getExtractItems() {
		return extractItems;
	}

	public void setExtractItems(List<ExtractItem> extractItems) {
		this.extractItems = extractItems;
	}

	public Job getJob() {
		return job;
	}

	public void setJob(Job job) {
		this.job = job;
	}

	public static String buildXml(JobProfile jobProfile) {
		String xml = "";
		if (null != jobProfile) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			try {
				docBuilder = dbf.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				doc.setXmlStandalone(true);
				Element root = doc.createElement("jobProfile");
				Element jobElement = doc.createElement("job");
				Job job = jobProfile.getJob();
				jobElement.setAttribute("name", getValue(job.getName()));
				jobElement.setAttribute("designatedNodeName", getValue(job.getDesignatedNodeName()));
				jobElement.setAttribute("level", getValue(String.valueOf(job.getLevel())));
				jobElement.setAttribute("workFrequency", getValue(String.valueOf(job.getWorkFrequency())));
				jobElement.setAttribute("isScheduled", getValue(String.valueOf(job.getIsScheduled())));
				jobElement.setAttribute("needNodes", getValue(String.valueOf(job.getNeedNodes())));
				jobElement.setAttribute("user", getValue(job.getUser()));
				
				if (StringUtils.isNoneBlank(job.getQueueName())) {
					Element queueNameElement = doc.createElement("queueName");
					queueNameElement.setTextContent(job.getQueueName());
					jobElement.appendChild(queueNameElement);
				}

				if (StringUtils.isNoneBlank(job.getCronTrigger())) {
					Element cronTriggerElement = doc.createElement("cronTrigger");
					cronTriggerElement.setTextContent(job.getCronTrigger());
					jobElement.appendChild(cronTriggerElement);
				}

				if (StringUtils.isNoneBlank(job.getWorkerClass())) {
					Element workerClassElement = doc.createElement("workerClass");
					workerClassElement.setTextContent(job.getWorkerClass());
					jobElement.appendChild(workerClassElement);
				}
				if (StringUtils.isNoneBlank(job.getDescribe())) {
					Element jobDescribeElement = doc.createElement("describe");
					jobDescribeElement.setTextContent(job.getDescribe());
					jobElement.appendChild(jobDescribeElement);
				}
				if (null != job.getParamList()) {
					for (JobParam jobParam : job.getParamList()) {
						Element jobParamElement = doc.createElement("param");
						jobParamElement.setAttribute("name", getValue(jobParam.getName()));
						jobParamElement.setTextContent(getValue(jobParam.getValue()));
						jobElement.appendChild(jobParamElement);
					}
				}
				root.appendChild(jobElement);
				if (null != jobProfile.getExtractItems()) {
					for (ExtractItem extractItem : jobProfile.getExtractItems()) {
						Element extractItemElement = doc.createElement("extractItem");
						extractItemElement.setAttribute("jobName", getValue(extractItem.getJobName()));
						extractItemElement.setAttribute("serialNub",
								getValue(String.valueOf(extractItem.getSerialNub())));
						extractItemElement.setAttribute("pathName", getValue(extractItem.getPathName()));
						extractItemElement.setAttribute("primary", getValue(String.valueOf(extractItem.getPrimary())));
						extractItemElement.setAttribute("type", getValue(String.valueOf(extractItem.getType())));
						extractItemElement.setAttribute("outputType",
								getValue(String.valueOf(extractItem.getOutputType())));
						extractItemElement.setAttribute("outputKey", getValue(extractItem.getOutputKey()));
						extractItemElement.setAttribute("mustHaveResult",
								getValue(String.valueOf(extractItem.getMustHaveResult())));
						if (StringUtils.isNoneBlank(extractItem.getDescribe())) {
							Element describeElement = doc.createElement("describe");
							describeElement.setTextContent(extractItem.getDescribe());
							extractItemElement.appendChild(describeElement);
						}
						root.appendChild(extractItemElement);
					}
				}
				doc.appendChild(root);
				xml = toXml(doc);
			} catch (Exception e) {
				throw new RuntimeException("buildXml err", e);
			}
		}
		return xml;
	}

	/**
	 * 根据 jobProfileXml build JobProfile 对象
	 * 
	 * @param jobProfileXml
	 * @return
	 */
	public static JobProfile buildJobProfile(String jobProfileXml) {
		JobProfile jobProfile = null;
		if (StringUtils.isNotBlank(jobProfileXml)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try (ByteArrayInputStream input = new ByteArrayInputStream(jobProfileXml.getBytes());) {
				DocumentBuilder docBuilder = dbf.newDocumentBuilder();
				Document doc = docBuilder.parse(input);
				//1.获取job节点
				NodeList jobNodeList = doc.getElementsByTagName("job");
				List<JobParam> jobParams = new ArrayList<>();
				jobProfile = new JobProfile();
				Job job = new Job();
				for (int i = 0; i < jobNodeList.getLength(); i++) {
					Node jobNode = jobNodeList.item(i);
					job.setName(getValueStr(jobNode.getAttributes().getNamedItem("name")));
					job.setDesignatedNodeName(getValueStr(jobNode.getAttributes().getNamedItem("designatedNodeName")));
					job.setLevel(getValueInt(jobNode.getAttributes().getNamedItem("level")));
					job.setWorkFrequency(getValueInt(jobNode.getAttributes().getNamedItem("workFrequency")));
					job.setIsScheduled(getValueInt(jobNode.getAttributes().getNamedItem("isScheduled")));
					job.setNeedNodes(getValueInt(jobNode.getAttributes().getNamedItem("needNodes")));
					job.setUser(getValueStr(jobNode.getAttributes().getNamedItem("user")));
					NodeList jobChildNodes = jobNode.getChildNodes();
					for (int j = 0; j < jobChildNodes.getLength(); j++) {
						Node jobChildNode = jobChildNodes.item(j);
						String tagName = jobChildNode.getNodeName();
						if ("queueName".equals(tagName)) {
							job.setQueueName(getValue(jobChildNode.getTextContent()));
						} else if ("cronTrigger".equals(tagName)) {
							job.setCronTrigger(getValue(jobChildNode.getTextContent()));
						} else if ("workerClass".equals(tagName)) {
							job.setWorkerClass(getValue(jobChildNode.getTextContent()));
						} else if ("describe".equals(tagName)) {
							job.setDescribe(getValue(jobChildNode.getTextContent()));
						} else if ("param".equals(tagName)) {
							JobParam jobParam = new JobParam();
							jobParam.setJobName(job.getName());
							jobParam.setName(getValueStr(jobChildNode.getAttributes().getNamedItem("name")));
							jobParam.setValue(getValue(jobChildNode.getTextContent()));
							jobParams.add(jobParam);
						}
					}
					job.setParamList(jobParams);
				}
				List<ExtractItem> extractItemList = new ArrayList<>();
				NodeList extractItemNodeList = doc.getElementsByTagName("extractItem");
				for (int i = 0; i < extractItemNodeList.getLength(); i++) {
					Node extractItemNode = extractItemNodeList.item(i);
					ExtractItem extractItem = new ExtractItem();
					extractItem.setJobName(getValueStr(extractItemNode.getAttributes().getNamedItem("jobName")));
					extractItem.setSerialNub((getValueInt(extractItemNode.getAttributes().getNamedItem("serialNub"))));
					extractItem.setPathName(getValueStr(extractItemNode.getAttributes().getNamedItem("pathName")));
					extractItem.setPrimary(getValueInt(extractItemNode.getAttributes().getNamedItem("primary")));
					extractItem.setType(getValueInt(extractItemNode.getAttributes().getNamedItem("type")));
					extractItem.setOutputType(getValueInt(extractItemNode.getAttributes().getNamedItem("outputType")));
					extractItem.setOutputKey(getValueStr(extractItemNode.getAttributes().getNamedItem("outputKey")));
					extractItem.setMustHaveResult(
							getValueInt(extractItemNode.getAttributes().getNamedItem("mustHaveResult")));
					NodeList extractItemChildNodes = extractItemNode.getChildNodes();
					for (int j = 0; j < extractItemChildNodes.getLength(); j++) {
						Node extractItemChildNode = extractItemChildNodes.item(j);
						String tagName = extractItemChildNode.getNodeName();
						if ("describe".equals(tagName)) {
							extractItem.setDescribe(getValue(extractItemChildNode.getTextContent()));
						}
					}
					extractItemList.add(extractItem);
				}
				jobProfile.setJob(job);
				jobProfile.setExtractItems(extractItemList);
			} catch (Exception e) {
				throw new RuntimeException("buildJobProfile err", e);
			}
		}
		return jobProfile;
	}
}
