package six.com.crawler.work.fetch;

import java.util.ArrayList;
import java.util.List;

import six.com.crawler.entity.ResultContext;
import six.com.crawler.work.extract.ExtractItem;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月5日 上午9:19:20
 */
public class UrlFetch implements Fetch {

	private List<ExtractItem> extractItems=new ArrayList<>();

	public UrlFetch(List<ExtractItem> extractItems) {
		for (ExtractItem extractItem : extractItems) {
			if (extractItem.getOutputType() == 3 && extractItem.getType() == 2) {
				this.extractItems.add(extractItem);
			}
		}
	}

	@Override
	public void fetch(ResultContext resultContext) {

	}

}
