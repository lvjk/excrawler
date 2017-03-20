package six.com.crawler.schedule;


import org.springframework.stereotype.Component;

import six.com.crawler.common.entity.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午3:47:35
 */
@Component
public class WorkerScheduledClient extends RemotingClient {

	public void execute(Node targetNode, String jobName) {
		String path = "/crawler/worker/scheduled/execute/" + jobName;
		doExecute(targetNode, path);
	}

	public void suspend(Node targetNode, String jobName) {
		String path = "/crawler/worker/scheduled/suspend/" + jobName;
		doExecute(targetNode, path);
	}

	public void goOn(Node targetNode, String jobName) {
		String path = "/crawler/worker/scheduled/goOn/" + jobName;
		doExecute(targetNode, path);
	}

	public void stop(Node targetNode, String jobName) {
		String path = "/crawler/worker/scheduled/stop/" + jobName;
		doExecute(targetNode, path);

	}

	public void stopAll(Node targetNode) {
		String path = "/crawler/worker/scheduled/stopAll";
		doExecute(targetNode, path);
	}
}
