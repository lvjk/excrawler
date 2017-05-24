package six.com.crawler.node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月24日 上午9:07:55
 */
public enum NodeChangeEvent {

	/**成为主节点**/
	TO_MASTER, 
	/**丢失工作节点**/
	MISS_WORKER;
}
