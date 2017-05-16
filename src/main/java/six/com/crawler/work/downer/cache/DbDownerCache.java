package six.com.crawler.work.downer.cache;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.druid.support.json.JSONUtils;

import six.com.crawler.entity.Page;
import six.com.crawler.http.HttpMethod;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月15日 下午6:12:12
 */
public class DbDownerCache extends AbstractDownerCache {

	public DbDownerCache(String siteCode) {
		super(siteCode);
	}

	protected final static Logger log = LoggerFactory.getLogger(DbDownerCache.class);
	
	@Override
	protected void doWirte(Page page) {
//		// 保存源数据
//		String savePath = helper.getRawDataPath();
//		String fileName = page.getFinalUrl();
//		if (page.getMethod() == HttpMethod.POST) {// 当请求为post时，将参数拼装到文件名中
//			if (null != page.getParameters()) {
//				fileName = fileName + "_";
//				for (String key : doingPage.getParameters().keySet()) {
//					fileName = "&" + key + "=" + doingPage.getParameters().get(key);
//				}
//			}
//		}
//		fileName = convertFileName(fileName);
//		if (createDir(savePath)) {
//			// 保存数据
//			String dataPath = savePath + "/data/" + fileName;
//			if (null != doingPage.getPageSrc()) {
//				saveToFile(dataPath, doingPage.getPageSrc());
//			}
//
//			// 保存meta
//			String metaPath = savePath + "/meta/" + fileName;
//			String metaJsonStr = JSONUtils.toJSONString(doingPage.getMetaMap());
//			if (!metaJsonStr.isEmpty() && !"{}".equals(metaJsonStr)) {
//				saveToFile(metaPath, metaJsonStr);
//			}
//		}
	
	}

	@Override
	protected void doRead(Page page) {
		// TODO Auto-generated method stub

	}

}
