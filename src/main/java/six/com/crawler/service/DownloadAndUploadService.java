package six.com.crawler.service;


import org.springframework.core.io.InputStreamResource;
import org.springframework.http.ResponseEntity;
import org.springframework.web.multipart.MultipartFile;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月1日 下午2:21:34
 */
public interface DownloadAndUploadService {

	/**
	 * 文件下载服务
	 * @param param
	 * @return
	 */
	public ResponseEntity<InputStreamResource> download(String param);
	
	/**
	 * 文件上传服务
	 * @param multipartFile
	 * @return
	 */
	public String upload(MultipartFile multipartFile);
}
