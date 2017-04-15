package six.com.crawler;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import six.com.crawler.work.WorkerLifecycleState;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月15日 上午10:40:31 
*/
public class ConditionTest {
	
	private static final ReentrantLock reentrantLock = new ReentrantLock();
	// 用来Condition.await() 和condition.signalAll();
	private static final Condition condition = reentrantLock.newCondition();
	
	public static void main(String[] args) {
		reentrantLock.lock();
		try {
			System.out.println("开始等待");
			condition.await(2000, TimeUnit.MILLISECONDS);
			System.out.println("结束等待");
		} catch (InterruptedException e) {
			e.printStackTrace();
		} finally {
			reentrantLock.unlock();
		}
	}

}
