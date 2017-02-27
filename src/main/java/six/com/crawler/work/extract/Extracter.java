package six.com.crawler.work.extract;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年2月23日 下午9:19:23
 */
public interface Extracter {

	public ResultContext extract(Page page);
}
