package six.com.crawler.admin.service.impl;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import six.com.crawler.admin.service.ExtractPathService;
import six.com.crawler.admin.service.SiteService;
import six.com.crawler.dao.SiteDao;
import six.com.crawler.entity.Site;
import six.com.crawler.entity.SiteProfile;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年9月9日 下午1:15:09
 */
@Service
public class SiteServiceImpl implements SiteService {

	final static Logger LOG = LoggerFactory.getLogger(SiteServiceImpl.class);

	@Autowired
	private SiteDao siteDao;

	@Autowired
	private ExtractPathService extracterService;

	public List<Site> querySites(int pageIndex, int pageSize) {
		List<Site> sites = siteDao.querySites(pageIndex, pageSize);
		return sites;
	}

	@Override
	public Site querySite(String siteCode) {
		Site result = siteDao.query(siteCode);
		return result;
	}

	public void save(Site site) {
		siteDao.save(site);
	}

	public void del(String siteCode) {
		siteDao.del(siteCode);
	}

	@Override
	public List<ExtractPath> queryExtractPathBySiteCode(String siteCode) {
		return null;
	}

	@Override
	public List<ExtractPath> queryExtractPathByName(String name) {
		return null;
	}

	@Override
	public ResponseEntity<InputStreamResource> download(String param) {
		SiteProfile profile = new SiteProfile();
		Site site = querySite(param);
		if (null != site) {
			profile.setSite(site);
			List<ExtractPath> extractPaths = extracterService.query(site.getCode());
			profile.setExtractPaths(extractPaths);
		}
		String xml = "";
		try {
			xml = SiteProfile.buildXml(profile);
		} catch (Exception e) {
			LOG.error("downloadProfile err:" + param, e);
		}
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
		headers.setContentDispositionFormData("attachment", param + "_site.xml");
		byte[] bytes = xml.getBytes();
		InputStream inputStream = new ByteArrayInputStream(bytes);
		return ResponseEntity.ok().headers(headers).contentLength(bytes.length)
				.contentType(MediaType.parseMediaType("application/octet-stream"))
				.body(new InputStreamResource(inputStream));
	}

	@Override
	public String upload(MultipartFile siteProfile) {
		String msg = null;
		if (null != siteProfile && !siteProfile.isEmpty()) {
			try {
				byte[] buffer = siteProfile.getBytes();
				String siteProfileXml = new String(buffer);
				SiteProfile profile = SiteProfile.buildSiteProfile(siteProfileXml);
				if (null != profile && null != profile.getSite()) {
					del(profile.getSite().getCode());
					save(profile.getSite());
					extracterService.delExtractPathBySiteCide(profile.getSite().getCode());
					if (null != profile.getExtractPaths()&&!profile.getExtractPaths().isEmpty()) {
						extracterService.saveExtractPath(profile.getExtractPaths());
					}

					msg = "uploadJobProfile[" + siteProfile.getName() + "] succeed";
				}
			} catch (Exception e) {
				msg = "uploadJobProfile[" + siteProfile.getName() + "] err";
				LOG.error(msg, e);
			}
		} else {
			msg = "uploadJobProfile is empty";
		}
		return msg;
	}

	public SiteDao getSiteDao() {
		return siteDao;
	}

	public void setSiteDao(SiteDao siteDao) {
		this.siteDao = siteDao;
	}

	public ExtractPathService getExtracterService() {
		return extracterService;
	}

	public void setExtracterService(ExtractPathService extracterService) {
		this.extracterService = extracterService;
	}

}
