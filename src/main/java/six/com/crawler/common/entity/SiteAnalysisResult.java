package six.com.crawler.common.entity;

import java.util.List;

import six.com.crawler.work.extract.ExtractPath;

/**
 *@author six    
 *@date 2016年6月7日 下午4:05:50 
 *站点分析结果 
*/
public class SiteAnalysisResult {
	
		private List<ExtractPath> paths;//站点的抓取规则

		public List<ExtractPath> getPaths() {
			return paths;
		}

		public void setPaths(List<ExtractPath> paths) {
			this.paths = paths;
		}
}
