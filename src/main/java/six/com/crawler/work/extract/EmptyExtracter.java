package six.com.crawler.work.extract;

import java.util.List;

import six.com.crawler.entity.Page;
import six.com.crawler.work.AbstractCrawlWorker;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午11:14:35 
*/
public class EmptyExtracter extends AbstractExtracter{

	public EmptyExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		super(worker, extractItems);
	}

	@Override
	protected List<String> doExtract(Page page, ExtractItem extractItem, ExtractPath path) {
		return null;
	}

}
