package six.com.crawler.common;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.entity.Page;
import six.com.crawler.http.HttpProxyPool;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月9日 下午5:00:26
 */
public class RedisManagerTest extends BaseTest {

	@Test
	public void test() {
		//HttpProxyPool
//		Set<String> keys=redisManager.keys(HttpProxyPool.REDIS_HTTP_PROXY_POOL+"_"+"*");
//		for(String key:keys){
//			redisManager.del(key);
//		}
//		String hkey = RedisWorkQueue.PRE_QUEUE_KEY + "cq315house_house_info";
//		int size=redisManager.hllen(hkey);
//		String cursorStr = "0";
//		int count=0;
//		do {
//			List<Page> list = new ArrayList<Page>();
//			cursorStr = redisManager.hscan(hkey,cursorStr, list, Page.class);
//			for (Page page : list) {
//				count++;
//				System.out.println("page key:" + page.getPageKey()+"|总数量:" + count+"|游标:" + cursorStr);
//			}
//			list.clear();
//		} while (!"0".equals(cursorStr));
//		System.out.println("实际总数:" + size+"|获取总数:" + count);
	}
}
