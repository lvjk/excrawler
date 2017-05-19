package six.com.crawler.work.extract.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.utils.JsoupUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.extract.AbstractExtracter;
import six.com.crawler.work.extract.ExtractItem;
import six.com.crawler.work.extract.ExtractPath;
import six.com.crawler.work.extract.exception.InvalidPathExtracterException;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午10:05:13
 * 
 *       表格解析 单条数据 抽取器
 *       因为一个 表格抽取会一次性把所有key value 抽取出来 所以第一处理path时缓存下来
 */
public class CssTableForOneExtracter extends AbstractExtracter {

	Map<String, Map<String, String>> cacheMap = new HashMap<>();

	public CssTableForOneExtracter(AbstractCrawlWorker worker, List<ExtractItem> extractItems) {
		super(worker, extractItems);
	}

	@Override
	protected List<String> doExtract(Page page, ExtractItem extractItem, ExtractPath tablePath) {
		//从缓存中获取 表格数据抽取出来的map
		Map<String, String> resultMap = cacheMap.computeIfAbsent(page.getPageKey(), mapKey -> {
			//第一次初始化缓存map里的数据
			Elements tableElements = JsoupUtils.select(page.getDoc(), tablePath.getPath());
			if (null == tableElements || tableElements.isEmpty()) {
				throw new InvalidPathExtracterException("invalid table path:" + tablePath.getPath());
			}
			Map<String, String> tableResults = new HashMap<>();
			Map<String, String> tempTableResults = null;
			for (Element tableElement : tableElements) {
				tempTableResults = JsoupUtils.paserTableForOne(tableElement);
				tableResults.putAll(tempTableResults);
			}
			return tableResults;
		});
		List<String> resultList = new ArrayList<>(1);
		String result = resultMap.get(tablePath.getExtractAttName());
		resultList.add(result);
		return resultList;
	}

}
