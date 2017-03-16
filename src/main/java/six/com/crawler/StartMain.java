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

import six.com.crawler.common.configure.SpiderConfigure;
import six.com.crawler.common.interceptor.BaseInterceptor;

/**
 * @author six
 * @date 2016年5月30日 下午2:47:46 程序启动入口类
 */
@ComponentScan
@Configuration
@EnableAutoConfiguration
public class StartMain extends WebMvcConfigurerAdapter {

	protected final static Logger log = LoggerFactory.getLogger(StartMain.class);
	
	@Autowired
	private SpiderConfigure configure;
	
	public SpiderConfigure getConfigure() {
		return configure;
	}

	public void setConfigure(SpiderConfigure configure) {
		this.configure = configure;
	}
	
	public static void main(String[] args) {
		String spiderHome=null;
		if(args==null||args.length==0){
			System.out.println("please set spider home");
			log.info("please set spider home");
		}else{
			spiderHome=args[0];
			System.out.println("set spider home:"+spiderHome);
			log.info("set spider home:"+spiderHome);
			SpringApplication.run(StartMain.class,spiderHome);
		}
	}

	/**
	 * 配置拦截器
	 * 
	 * @author lance
	 * @param registry
	 */
	public void addInterceptors(InterceptorRegistry registry) {
		registry.addInterceptor(new BaseInterceptor(configure)).addPathPatterns("/**");
	}
}
