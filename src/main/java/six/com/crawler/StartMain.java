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
import six.com.crawler.node.NodeManager;

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
 *     
 *    <p> 人为触发任务------</p>
 *    <p>                |-->主节点执行任务,在缓存中初始化任务执行信息--->主节点计算出执行任务的节点List---->call工作节点执行任务---></p>
 *    <p> 定时触发任务------</p>
 *       
 *       
 *    <p>---->工作节点接收到主节点执行任务的信号--->工作节点创建worker--->工作节点运行worker--->通知主节点worker运行---></p>
 *     
 *    <p>---->主节点接收到工作节点worker运行信息，并登记运行信息---></p>
 *     
 *                   <p>  --->工作空间处理数据为空--->任务完成</p>
 *    <p>---->worker运行--->停止                                                              ----工作节点worker通知主节点,worker运行结束---> </p>  
 *                   <p>  --->手动停止------------->任务停止 </p>  
 *     
 *                                                               <p> --->没有全部结束，不做任何处理  </p>
 *    <p>---->主节点接收到工作节点worker运行结束信号--->检查任务的全部worker是否结束</p>
 *                                                                
 *     
 *    <p>---->全部结束，统计各worker的运行信息，并生成运行任务运行记录入库，删除缓存中任务运行记录---></p>
 *    
 *    <p>---->如果任务是完成结束的话,那么检查检查任务是否有下一个执行任务 </p>
 *       
 */
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class StartMain extends WebMvcConfigurerAdapter {

	protected final static Logger log = LoggerFactory.getLogger(StartMain.class);

	@Autowired
	private NodeManager clusterManager;

	public NodeManager getClusterManager() {
		return clusterManager;
	}

	public void setClusterManager(NodeManager configure) {
		this.clusterManager = configure;
	}

	public static void main(String[] args) {
		String spiderHome = null;
		if (args == null || args.length == 0) {
			System.out.println("please set spider home");
			log.info("please set spider home");
		} else {
			spiderHome = args[0];
			System.out.println("set spider home:" + spiderHome);
			log.info("set spider home:" + spiderHome);
			SpringApplication.run(StartMain.class, spiderHome);
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
