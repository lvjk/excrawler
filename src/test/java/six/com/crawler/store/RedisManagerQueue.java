package six.com.crawler.store;

import java.util.ArrayList;
import java.util.List;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.entity.Page;
import six.com.crawler.work.RedisWorkQueue;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月6日 下午3:28:44
 */
public class RedisManagerQueue extends BaseTest {

	@Test
	public void test() {
//		List<String> queueNames = new ArrayList<String>();
//		queueNames.add("cq315house_house_state");
//		queueNames.add("cq315house_house_info");
//		queueNames.add("cq315house_presell_2");
//		queueNames.add("qichacha");
//		queueNames.add("tmsf_house_info");
//		queueNames.add("tmsf_house_url");
//		queueNames.add("tmsf_project_info");
//
//		for (String queueName : queueNames) {
//			RedisWorkQueue workQueue1 = new RedisWorkQueue(redisManager, queueName);
//			Page page = null;
//			List<Page> list = new ArrayList<Page>();
//			while ((page = workQueue1.pull()) != null) {
//				page.setNeedDown(1);
//				list.add(page);
//			}
//			for (Page newPage : list) {
//				workQueue1.push(newPage);
//			}
//		}
	}

}
