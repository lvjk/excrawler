package six.com.crawler.schedule;

import org.junit.Test;

import six.com.crawler.BaseTest;
import six.com.crawler.work.Worker;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月8日 上午10:47:14
 */
public class WorkerPlugsManagerTest extends BaseTest {

	@Test
	public void test() {
		Worker<?> worker = workerPlugsManager.newWorker("six.com.crawler.work.plugs.TestWorker");
		log.info(null != worker ? worker.toString() : "did not find worker");
	}
	
	public static void main(String[] args){
		log.info(WorkerPlugsManagerTest.class.getName());
	}
}
