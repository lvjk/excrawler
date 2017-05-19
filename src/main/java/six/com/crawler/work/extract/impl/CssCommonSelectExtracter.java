package six.com.crawler.work.extract.impl;

import java.util.List;

import six.com.crawler.entity.Page;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.AbstractExtracter;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.ExtractPath;

/**
 * @author six
 * @date 2016年8月18日 上午10:29:02
 */
public class CssCommonSelectExtracter extends AbstractExtracter {

	public CssCommonSelectExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		super(worker, extractItems);
	}

	@Override
	protected List<String> doExtract(Page page, ExtractItem extractItem,ExtractPath path) {
		List<String> extractResult = JsoupUtils.extract(page.getDoc(), path);
		return extractResult;
	}
}
