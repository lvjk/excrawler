package six.com.crawler.api;

import java.util.Enumeration;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.multipart.MultipartFile;

import six.com.crawler.service.NodeManagerService;
import six.com.crawler.service.DownloadAndUploadService;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月13日 下午3:59:53
 */
public class BaseApi {

	private final static Logger LOG = LoggerFactory.getLogger(BaseApi.class);

	@Autowired
	private NodeManagerService clusterService;

	public NodeManagerService getClusterService() {
		return clusterService;
	}

	public void setClusterService(NodeManagerService clusterService) {
		this.clusterService = clusterService;
	}

	public static HttpServletRequest getRequest() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
	}

	public static HttpServletResponse getResponse() {
		return ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getResponse();
	}

	public <T> ResponseMsg<T> createResponseMsg() {
		ResponseMsg<T> responseMsg = new ResponseMsg<>(clusterService.getCurrentNode().getName());
		return responseMsg;
	}

	/**
	 * 文件下载
	 * 
	 * @param downloadAndUploadService
	 * @param param
	 *            文件下载参数
	 * @return
	 */
	public static ResponseEntity<InputStreamResource> downloadFile(DownloadAndUploadService downloadAndUploadService,
			String param) {
		return downloadAndUploadService.download(param);
	}

	/**
	 * 文件上传
	 * 
	 * @param downloadAndUploadService
	 * @param fileName
	 * @param input
	 * @return
	 */
	public static String uploadFile(DownloadAndUploadService downloadAndUploadService, MultipartFile multipartFile) {
		return downloadAndUploadService.upload(multipartFile);
	}

	public static String getClientIp() {
		HttpServletRequest request = getRequest();
		String ip = request.getHeader("X-Forwarded-For");
		Enumeration<String> heads = request.getHeaderNames();
		while (heads.hasMoreElements()) {
			String headName = heads.nextElement();
			String headValue = request.getHeader(headName);
			LOG.info(headName + ":" + headValue);
		}
		if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("Proxy-Client-IP");

			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("WL-Proxy-Client-IP");

			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_CLIENT_IP");

			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getHeader("HTTP_X_FORWARDED_FOR");

			}
			if (ip == null || ip.length() == 0 || "unknown".equalsIgnoreCase(ip)) {
				ip = request.getRemoteAddr();

			}
		} else if (ip.length() > 15) {
			String[] ips = ip.split(",");
			for (int index = 0; index < ips.length; index++) {
				String strIp = (String) ips[index];
				if (!("unknown".equalsIgnoreCase(strIp))) {
					ip = strIp;
					break;
				}
			}
		}
		return ip;
	}

}
