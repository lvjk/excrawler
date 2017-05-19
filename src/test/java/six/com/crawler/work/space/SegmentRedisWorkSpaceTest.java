package six.com.crawler.work.space;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.entity.Page;
import six.com.crawler.node.lock.DistributedLock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月12日 下午5:20:42
 */
public class SegmentRedisWorkSpaceTest extends BaseTest {

	private static final String WORKSPACE_PRE = "workspace_";

	@Test
	public void test() {
//		String workSpaceName = "test_repari";
//		testPush(workSpaceName);
//		testPull(workSpaceName);
//		try {
//			Thread.sleep(100000000);
//		} catch (InterruptedException e) {
//			e.printStackTrace();
//		}
	}

	public void testPush(String workSpaceName) {
		DistributedLock distributedLock = clusterManager.getWriteLock(WORKSPACE_PRE + workSpaceName);
		SegmentRedisWorkSpace<Page> workSpace = new SegmentRedisWorkSpace<>(redisManager, distributedLock,
				workSpaceName, Page.class);
		String url = "http://blog.csdn.net/b47248054/article/details/8277789";
		final int count =1000;
		int thread = 4;
		ExecutorService executor = Executors.newFixedThreadPool(thread * 2);
		System.out.println("实际数据数量:" + workSpace.doingSize());
		workSpace.clearDoing();
		System.out.println("实际数据数量:" + workSpace.doingSize());
		for (int i = 0; i < thread; i++) {
			executor.execute(() -> {
				Page page = null;
				String threadName = Thread.currentThread().getName();
				for (int j = 0; j < count; j++) {
					String newUrl = url + threadName + j;
					page = new Page("test", 1, newUrl, newUrl);
					long start = System.currentTimeMillis();
					if(!workSpace.isDone(page.getKey())){
						workSpace.push(page);
					}
					long end = System.currentTimeMillis();
					System.out.println("push time:" + (end - start));
				}
			});
		}
	}

	public void testPull(String workSpaceName) {
		DistributedLock distributedLock = clusterManager.getWriteLock(WORKSPACE_PRE + workSpaceName);
		SegmentRedisWorkSpace<Page> workSpace = new SegmentRedisWorkSpace<>(redisManager, distributedLock,
				workSpaceName, Page.class);
		int thread = 1;
		ExecutorService executor = Executors.newFixedThreadPool(thread * 2);
		for (int i = 0; i < thread; i++) {
			executor.execute(() -> {
				Page page = null;
				boolean repair = false;
				while (true) {
					long start = System.currentTimeMillis();
					page = workSpace.pull();
					long end = System.currentTimeMillis();
					System.out.println("pull time:" + (end - start));
					if (null != page) {
						workSpace.addDone(page.getKey());
						workSpace.ack(page);
					} else {
						if (!repair) {
							workSpace.repair();
							repair = true;
							continue;
						} else {
							break;
						}
					}
				}
				workSpace.close();
			});
		}
	}

}
