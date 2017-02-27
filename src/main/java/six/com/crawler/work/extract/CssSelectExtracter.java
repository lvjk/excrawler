package six.com.crawler.work.extract;

import java.util.List;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.ResultContext;
import six.com.crawler.work.AbstractCrawlWorker;

/**
 * @author six
 * @date 2016年8月18日 上午10:29:02
 */
public class CssSelectExtracter extends AbstractExtracter {

	public CssSelectExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		super(worker, extractItems);
	}

	public ResultContext extract(Page page) {
		ResultContext resultContext = new ResultContext();
		if (null != getExtractItems()) {
			for (int i = 0; i < getExtractItems().size(); i++) {
				ExtractItem doPaserItem = getExtractItems().get(i);
				List<String> doResults = extract(page, doPaserItem);
				resultContext.addExtractResult(doPaserItem.getOutputKey(), doResults);
			}
		}
		return resultContext;
	}
}
