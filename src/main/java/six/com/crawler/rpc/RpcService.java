package six.com.crawler.rpc;

import java.util.Map;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月14日 下午5:33:33
 */
@FunctionalInterface
public interface  RpcService{

	public Object execute(Map<String,Object> params);
}
