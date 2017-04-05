package six.com.crawler.work.fetch;

import six.com.crawler.entity.ResultContext;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月5日 上午9:14:43
 * 
 *       挑选url 至工作队列
 */
public interface Fetch {

	public  void fetch(ResultContext resultContext);
}
