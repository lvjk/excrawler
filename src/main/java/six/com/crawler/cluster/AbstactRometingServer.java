package six.com.crawler.cluster;

import java.rmi.AccessException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;

import org.springframework.beans.factory.InitializingBean;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 上午9:41:15
 */
public class AbstactRometingServer implements InitializingBean {

	private Registry registry;

	@Override
	public void afterPropertiesSet() throws Exception {
		try {
			// 创建一个服务注册管理器
			registry = LocateRegistry.createRegistry(8088);
		} catch (RemoteException e) {

		}
	}

	public void register(String name, Remote Remote) throws AccessException, RemoteException {
		registry.rebind(name, Remote);
	}
}
