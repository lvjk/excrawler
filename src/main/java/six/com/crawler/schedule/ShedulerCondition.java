package six.com.crawler.schedule;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年4月11日 下午2:32:25 
*/
public class ShedulerCondition implements Condition{

	@Override
	public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
		return false;
	}

}
