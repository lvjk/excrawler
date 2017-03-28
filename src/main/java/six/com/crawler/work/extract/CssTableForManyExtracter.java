package six.com.crawler.work.extract;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;

import six.com.crawler.entity.Page;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.exception.InvalidPathExtracterException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午10:05:24
 * 
 *  表格解析 多条数据 抽取器
 *       因为一个 表格抽取会一次性把所有数据 抽取出来 所以第一处理path时缓存下来
 */
public class CssTableForManyExtracter extends AbstractExtracter {

	Map<String, Map<String, List<String>>> cacheMap = new HashMap<>();

	public CssTableForManyExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		super(worker, extractItems);
	}

	@Override
	protected List<String> doExtract(Page page, ExtractItem extractItem, ExtractPath tablePath) {
		// 从缓存中获取 表格数据抽取出来的map
		Map<String, List<String>> resultMap = cacheMap.computeIfAbsent(page.getPageKey(), mapKey -> {
			// 第一次初始化缓存map里的数据
			Element tableElement = JsoupUtils.select(page.getDoc(), tablePath.getPath()).first();
			if (null == tableElement) {
				throw new InvalidPathExtracterException("invalid table path:" + tablePath.getPath());
			}
			Map<String, List<String>> tableResults = JsoupUtils.paserTableForMany(tableElement,
					tablePath.getTableHeadPath(), tablePath.getTableDataPath());
			return tableResults;
		});
		List<String> resultList = resultMap.get(tablePath.getExtractAttName());
		return resultList;
	}
}
