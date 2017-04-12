package six.com.crawler.schedule;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Conditional;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月11日 下午2:33:52
 */
public class WrapperScheduler extends AbstractSchedulerManager {

	private AbstractSchedulerManager schedulerManager;
	
	private six.com.crawler.schedule.master.MasterAbstractSchedulerManager MasterAbstractSchedulerManager;

//	@Bean
//	@Conditional(ShedulerCondition.class) // 通过@Condition注解,符合Linux条件则实例化linuxListService
//	public AbstractSchedulerManager linuxListService() {
//		return null;
//	}

	@Override
	public void execute(String jobName) {

	}

	@Override
	public void suspend(String jobName) {

	}

	@Override
	public void goOn(String jobName) {

	}

	@Override
	public void stop(String jobName) {

	}

	@Override
	public void stopAll() {

	}

	@Override
	protected void init() {

	}

}
