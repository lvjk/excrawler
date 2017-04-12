package six.com.crawler.node.lock;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午9:59:04 分布式锁接口
 */
public interface DistributedLock {

	public void lock() throws Exception;

	public void unLock() throws Exception;
}
