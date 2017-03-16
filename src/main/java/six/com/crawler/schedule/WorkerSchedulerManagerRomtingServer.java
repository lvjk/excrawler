package six.com.crawler.schedule;

import java.rmi.Naming;
import java.rmi.Remote;
import java.rmi.registry.LocateRegistry;
import java.util.List;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;

import six.com.crawler.common.entity.Job;
import six.com.crawler.common.entity.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月15日 下午10:01:00
 */

public class WorkerSchedulerManagerRomtingServer implements Remote, InitializingBean {

	@Autowired
	private WorkerSchedulerManager workerSchedulerManager;

	public WorkerSchedulerManager getWorkerSchedulerManager() {
		return workerSchedulerManager;
	}

	public void setWorkerSchedulerManager(WorkerSchedulerManager workerSchedulerManager) {
		this.workerSchedulerManager = workerSchedulerManager;
	}

	private String host;

	private int port;

	@Override
	public void afterPropertiesSet() throws Exception {
		LocateRegistry.createRegistry(port);
		String name="rmi://"+host+":"+port+"/workerSchedulerManager";
		Naming.bind(name, this);
	}

	public void execute(Node callNode,Job job) {
		
	}

	public void suspend(Node callNode,Job job) {
		
	}

	public void goOn(Node callNode,Job job) {
		
	}

	public void stop(Node callNode,Job job) {
		
	}

	public void stopAll(List<Node> callNodes) {
		
	}

}
