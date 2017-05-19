package six.com.crawler.work.extract.impl;

import java.util.List;

import six.com.crawler.entity.Page;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.AbstractExtracter;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.ExtractPath;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月27日 上午10:17:48 
*/
public class RegularExtracter extends AbstractExtracter{

	public RegularExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		super(worker, extractItems);
	}

	@Override
	protected List<String> doExtract(Page page, ExtractItem extractItem, ExtractPath path) {
		return null;
	}


}
