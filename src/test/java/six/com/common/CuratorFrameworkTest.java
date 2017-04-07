package six.com.common;

import org.apache.curator.RetryPolicy;
import org.apache.curator.RetrySleeper;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月7日 下午4:55:27 
*/
public class CuratorFrameworkTest {

	public static void main(String[] args) throws InterruptedException {
		String connectString="172.18.84.44:2181;172.18.84.45:2181;172.18.84.46:2181";
		CuratorFramework zKClient = CuratorFrameworkFactory.newClient(connectString, new RetryPolicy() {
			@Override
			public boolean allowRetry(int arg0, long arg1, RetrySleeper arg2) {
				return false;
			}
		});
		zKClient.blockUntilConnected();
		zKClient.close();
		System.out.println("test ok");
	}

}
