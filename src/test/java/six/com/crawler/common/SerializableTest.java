package six.com.crawler.common;

import java.util.ArrayList;
import java.util.List;

import six.com.crawler.common.entity.Page;
import six.com.crawler.common.entity.PageType;
import six.com.crawler.common.utils.AutoKryoUtils;
import six.com.crawler.common.utils.JavaSerializeUtils;
import six.com.crawler.work.downer.DownerType;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年8月27日 下午1:52:43 类说明
 */
public class SerializableTest {

	public static void main(String[] args) throws Exception {
		testPerformance(1000);
		testPerformance(10000);
		testPerformance(100000);
	}

	private static void testPerformance(int count) throws Exception {
		List<Page> list = new ArrayList<>();
		String url = "http://roll.mil.news.sina.com.cn/col/zgjq/index.shtml";
		for (int i = 0; i < count; i++) {
			Page page = new Page("sina", 1, url + i, url + i);
			page.setDownerType(DownerType.HTTPCLIENT.value());// 设置此页面下载类型
			page.setType(PageType.LISTING.value());
			list.add(page);
		}
		byte[] dts = null;

		long start = System.currentTimeMillis();
		for (Page page : list) {
			dts = AutoKryoUtils.INSTANCE.serialize(page);
			AutoKryoUtils.INSTANCE.unSerialize(dts, Page.class);
		}
		long end = System.currentTimeMillis();
		long kryoConsumeTime = end - start;
		start = System.currentTimeMillis();
		for (Page page : list) {
			dts = JavaSerializeUtils.serialize(page);
			JavaSerializeUtils.unSerialize(dts, Page.class);
		}
		end = System.currentTimeMillis();
		long javaConsumeTime = end - start;
		System.out.println("java:kryo 序列化时间对比|" + javaConsumeTime + ":" + kryoConsumeTime + "|差距:"
				+ (javaConsumeTime - kryoConsumeTime));
	}
}
