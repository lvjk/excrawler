package six.com.crawler.dao;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月7日 下午2:13:05 redis 执行 命令方法
 */
@FunctionalInterface
public interface RedisCommand<T> {

	T execute();

}
