package six.com.crawler.service;

import java.util.List;

import six.com.crawler.entity.Page;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月9日 下午12:48:43 
*/
public interface PageService {

	public void save(List<Page> page);
	
	public List<Page> query(String sitecode,List<String> urlMd5s);
}
