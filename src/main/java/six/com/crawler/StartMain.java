package six.com.crawler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import six.com.crawler.admin.interceptor.BaseInterceptor;
import six.com.crawler.admin.interceptor.MasterScheduledApiInterceptor;
import six.com.crawler.node.ClusterManager;

/**
 * @author six
 * @date 2016年5月30日 下午2:47:46 程序启动入口类
 * 
 *       整个系统分层:
 *       <p>
 *       后台管理api层
 *       </p>
 *       <p>
 *       后台管理服务层
 *       </p>
 *       <p>
 *       任务调度层
 *       </p>
 *       <p>
 *       节点基础层
 *       </p>
 *       <p>
 *       数据存储层
 *       </p>
 *       <p>
 *       -->主节点执行任务,在缓存中初始化任务执行信息--->主节点计算出可执行任务的节点---->call工作节点执行任务--->
 *       </p>
 *       <p>
 *       ---->工作节点接收到主节点执行任务的信号--->工作节点创建worker--->工作节点运行worker--->
 *       </p>
 *       <p>
 *       --->工作空间处理数据为空--->worker设置状态为wait,并且询问管理者是否end,然后等待
 *       </p>
 *       <p>
 *       --->管理者接收到worker询问后检查所有worker是否都等待，如果都等待那么检查工作队列是否需要修复
 *       然后判断队列是否有数据，如果有数据那么通知worker恢复运行状态,否则通知worker结束
 *       </p>
 *       <p>
 *       ---->worker接收到结束命令后退出工作流程循环，然后清理资源。然后通知管理者结束
 *       管理者接收到worker结束通知后检查所有worker是否全部结束，如果全部结束, 对job进行运行统计，然后将统计信息进行保存。
 *       </p>
 * 
 */
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class StartMain extends WebMvcConfigurerAdapter {

	protected final static Logger log = LoggerFactory.getLogger(StartMain.class);

	@Autowired
	private ClusterManager clusterManager;

	public ClusterManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(ClusterManager configure) {
		this.clusterManager = configure;
	}

	public static void main(String[] args) {
		String homePath = null;
		if (args == null || args.length == 0) {
			System.out.println("please set homePath");
			log.info("please set homePath");
		} else {
			homePath = args[0];
			System.out.println("set homePath:" + homePath);
			log.info("set homePath:" + homePath);
			SpringApplication.run(StartMain.class, homePath);
		}
	}

	/**
	 * 配置拦截器
	 * 
	 * @author lance
	 * @param registry
	 */
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new BaseInterceptor(clusterManager)).addPathPatterns("/**");
		registry.addInterceptor(new MasterScheduledApiInterceptor(clusterManager)).addPathPatterns("/**");

	}
}
