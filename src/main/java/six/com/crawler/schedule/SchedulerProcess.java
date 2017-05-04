package six.com.crawler.schedule;
/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年5月4日 下午5:21:48 
*/
@FunctionalInterface
public interface SchedulerProcess<T> {

	public T process();
}
