package six.com.crawler.work.extract;

import java.util.List;

import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.impl.CssCommonSelectExtracter;
import six.com.crawler.work.extract.impl.CssTableForManyExtracter;
import six.com.crawler.work.extract.impl.CssTableForOneExtracter;
import six.com.crawler.work.extract.impl.JsonExtracter;
import six.com.crawler.work.extract.impl.RegularExtracter;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午10:58:09
 * 
 *       抽取器生成工厂
 */
public class ExtracterFactory {

	/**
	 * 生成一个抽取器
	 * @param worker
	 * @param extractItems
	 * @param extracterType
	 * @return
	 */
	public static Extracter newExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems,
			ExtracterType extracterType) {
		Extracter extracter = null;
		if (ExtracterType.CssCommonSelect == extracterType) {
			extracter = new CssCommonSelectExtracter(worker, extractItems);
		} else if (ExtracterType.CssTableForOne == extracterType) {
			extracter = new CssTableForOneExtracter(worker, extractItems);
		} else if (ExtracterType.CssTableForMany == extracterType) {
			extracter = new CssTableForManyExtracter(worker, extractItems);
		} else if (ExtracterType.Regular == extracterType) {
			extracter = new RegularExtracter(worker, extractItems);
		} else if (ExtracterType.Json == extracterType) {
			extracter = new JsonExtracter(worker, extractItems);
		} else {
			extracter = new CssCommonSelectExtracter(worker, extractItems);
		}
		return extracter;
	}
}
