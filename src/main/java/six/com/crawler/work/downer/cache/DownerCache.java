package six.com.crawler.work.downer.cache;

import six.com.crawler.entity.Page;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月15日 下午5:57:01
 */
public interface DownerCache {

	void write(Page page);

	void read(Page page);
	
	void close();
}
