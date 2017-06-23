package six.com.crawler.schedule;

import java.io.Serializable;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年6月23日 上午11:16:44 
*/
public class SchedulerCommandGroup implements Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 3837562895500767608L;
	private SchedulerCommand[] schedulerCommands;

	public SchedulerCommand[] getSchedulerCommands() {
		return schedulerCommands;
	}

	public void setSchedulerCommands(SchedulerCommand[] schedulerCommands) {
		this.schedulerCommands = schedulerCommands;
	}
}
