package six.com.crawler.entity;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月1日 上午11:31:53
 */
public class SiteProfile extends Profile {

	private Site site;

	private List<ExtractPath> extractPaths;

	public Site getSite() {
		return site;
	}

	public void setSite(Site site) {
		this.site = site;
	}

	public List<ExtractPath> getExtractPaths() {
		return extractPaths;
	}

	public void setExtractPaths(List<ExtractPath> extractPaths) {
		this.extractPaths = extractPaths;
	}

	public static String buildXml(SiteProfile siteProfile) {
		String xml = "";
		if (null != siteProfile) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			DocumentBuilder docBuilder;
			try {
				docBuilder = dbf.newDocumentBuilder();
				Document doc = docBuilder.newDocument();
				doc.setXmlStandalone(true);
				Element root = doc.createElement("siteProfile");
				Element siteElement = doc.createElement("site");
				Site site = siteProfile.getSite();
				siteElement.setAttribute("code", getValue(site.getCode()));
				siteElement.setAttribute("visitFrequency", String.valueOf(site.getVisitFrequency()));
				
				if (StringUtils.isNotBlank(site.getMainUrl())) {
					Element mainUrlElement = doc.createElement("mainUrl");
					mainUrlElement.setTextContent(site.getMainUrl());
					siteElement.appendChild(mainUrlElement);
				}
								
				if (StringUtils.isNotBlank(site.getDescribe())) {
					Element describeElement = doc.createElement("describe");
					describeElement.setTextContent(site.getDescribe());
					siteElement.appendChild(describeElement);
				}
				
				root.appendChild(siteElement);
				if (null != siteProfile.getExtractPaths()) {
					for (ExtractPath extractPath : siteProfile.getExtractPaths()) {
						Element extractPathElement = doc.createElement("extractPath");
						extractPathElement.setAttribute("name", getValue(extractPath.getName()));
						extractPathElement.setAttribute("siteCode",
								getValue(String.valueOf(extractPath.getSiteCode())));
						extractPathElement.setAttribute("ranking", String.valueOf(extractPath.getRanking()));
						extractPathElement.setAttribute("extractAttName", getValue(extractPath.getExtractAttName()));
						extractPathElement.setAttribute("appendHead", getValue(extractPath.getAppendHead()));
						extractPathElement.setAttribute("appendEnd", getValue(extractPath.getAppendEnd()));
						extractPathElement.setAttribute("compareAttName", getValue(extractPath.getCompareAttName()));
						extractPathElement.setAttribute("containKeyWord", getValue(extractPath.getContainKeyWord()));
						if (StringUtils.isNotBlank(extractPath.getPath())) {
							Element pathElement = doc.createElement("path");
							pathElement.setTextContent(extractPath.getPath());
							extractPathElement.appendChild(pathElement);
						}
						
						
						if (StringUtils.isNotBlank(extractPath.getFilterPath())) {
							Element filterPathElement = doc.createElement("filterPath");
							filterPathElement.setTextContent(extractPath.getFilterPath());
							extractPathElement.appendChild(filterPathElement);
						}
						
						if (StringUtils.isNotBlank(extractPath.getSubstringStart())) {
							Element substringStartElement = doc.createElement("substringStart");
							substringStartElement.setTextContent(extractPath.getSubstringStart());
							extractPathElement.appendChild(substringStartElement);
						}
						
						
						if (StringUtils.isNotBlank(extractPath.getSubstringEnd())) {
							Element substringEndElement = doc.createElement("substringEnd");
							substringEndElement.setTextContent(extractPath.getSubstringEnd());
							extractPathElement.appendChild(substringEndElement);
						}
						
						
						if (StringUtils.isNotBlank(extractPath.getReplaceWord())) {
							Element replaceWordElement = doc.createElement("replaceWord");
							replaceWordElement.setTextContent(extractPath.getReplaceWord());
							extractPathElement.appendChild(replaceWordElement);
						}
						
						
						if (StringUtils.isNotBlank(extractPath.getReplaceValue())) {
							Element replaceValueElement = doc.createElement("replaceValue");
							replaceValueElement.setTextContent(extractPath.getReplaceValue());
							extractPathElement.appendChild(replaceValueElement);
						}
						
						
						if (StringUtils.isNotBlank(extractPath.getDescribe())) {
							Element pathDescribeElement = doc.createElement("describe");
							pathDescribeElement.setTextContent(extractPath.getDescribe());
							extractPathElement.appendChild(pathDescribeElement);
						}
						
						
						root.appendChild(extractPathElement);
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

	public static SiteProfile buildSiteProfile(String siteProfileXml) {
		SiteProfile siteProfile = null;
		if (StringUtils.isNotBlank(siteProfileXml)) {
			DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
			try (ByteArrayInputStream input = new ByteArrayInputStream(siteProfileXml.getBytes());) {
				DocumentBuilder docBuilder = dbf.newDocumentBuilder();
				Document doc = docBuilder.parse(input);
				NodeList siteNodeList = doc.getElementsByTagName("site");
				siteProfile = new SiteProfile();
				Site site = new Site();
				for (int i = 0; i < siteNodeList.getLength(); i++) {
					Node siteNode = siteNodeList.item(i);
					site.setCode(getValueStr(siteNode.getAttributes().getNamedItem("code")));
					
					site.setVisitFrequency(getValueInt(siteNode.getAttributes().getNamedItem("visitFrequency")));
					
					NodeList siteChildNodes = siteNode.getChildNodes();
					for (int j = 0; j < siteChildNodes.getLength(); j++) {
						Node siteChildNode = siteChildNodes.item(j);
						String tagName = siteChildNode.getNodeName();
						if ("mainUrl".equals(tagName)) {
							site.setMainUrl(getValue(siteChildNode.getTextContent()));
						} else if ("describe".equals(tagName)) {
							site.setDescribe(getValue(siteChildNode.getTextContent()));
						}
					}
				}
				siteProfile.setSite(site);
				List<ExtractPath> extractPathList = new ArrayList<>();
				NodeList extractPathNodeList = doc.getElementsByTagName("extractPath");
				for (int i = 0; i < extractPathNodeList.getLength(); i++) {
					Node extractPathNode = extractPathNodeList.item(i);
					ExtractPath extractPath = new ExtractPath();
					extractPath.setName(getValueStr(extractPathNode.getAttributes().getNamedItem("name")));
					extractPath.setSiteCode((getValueStr(extractPathNode.getAttributes().getNamedItem("siteCode"))));
					extractPath.setRanking(getValueInt(extractPathNode.getAttributes().getNamedItem("ranking")));
					extractPath.setExtractAttName(
							getValueStr(extractPathNode.getAttributes().getNamedItem("extractAttName")));
					extractPath.setAppendHead(getValueStr(extractPathNode.getAttributes().getNamedItem("appendHead")));
					extractPath.setAppendEnd(getValueStr(extractPathNode.getAttributes().getNamedItem("appendEnd")));
					extractPath.setCompareAttName(
							getValueStr(extractPathNode.getAttributes().getNamedItem("compareAttName")));
					extractPath.setContainKeyWord(
							getValueStr(extractPathNode.getAttributes().getNamedItem("containKeyWord")));
					NodeList extractPathChildNodes = extractPathNode.getChildNodes();
					for (int j = 0; j < extractPathChildNodes.getLength(); j++) {
						Node extractPathChildNode = extractPathChildNodes.item(j);
						String tagName = extractPathChildNode.getNodeName();
						if ("path".equals(tagName)) {
							extractPath.setPath(getValue(extractPathChildNode.getTextContent()));
						}else if ("filterPath".equals(tagName)) {
							extractPath.setFilterPath(getValue(extractPathChildNode.getTextContent()));
						} else if ("substringStart".equals(tagName)) {
							extractPath.setSubstringStart(getValue(extractPathChildNode.getTextContent()));
						} else if ("substringEnd".equals(tagName)) {
							extractPath.setSubstringEnd(getValue(extractPathChildNode.getTextContent()));
						} else if ("replaceWord".equals(tagName)) {
							extractPath.setReplaceWord(getValue(extractPathChildNode.getTextContent()));
						} else if ("replaceValue".equals(tagName)) {
							extractPath.setReplaceValue(getValue(extractPathChildNode.getTextContent()));
						} else if ("describe".equals(tagName)) {
							extractPath.setDescribe(getValue(extractPathChildNode.getTextContent()));
						}
					}
					extractPathList.add(extractPath);
				}
				siteProfile.setExtractPaths(extractPathList);
			} catch (Exception e) {
				throw new RuntimeException("buildJobProfile err", e);
			}
		}
		return siteProfile;
	}
}
