package six.com.crawler.node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月23日 下午3:30:59
 * 
 *       节点变化 watcher
 */
@FunctionalInterface
public interface NodeChangeWatcher {

	void onChange(String nodeName);
}
