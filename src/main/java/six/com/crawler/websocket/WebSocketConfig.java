package six.com.crawler.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.AbstractWebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月19日 上午9:42:24
 */

@Configuration
@EnableWebSocketMessageBroker
public class WebSocketConfig extends AbstractWebSocketMessageBrokerConfigurer {

	/**
	 * 服务器主动推送消息
	 */
	/**
	 * @Resource private JobActivityInfoWebSocketHandler handler;
	 * 
	 *           public JobActivityInfoWebSocketHandler getHandler() { return
	 *           handler; }
	 * 
	 *           public void setHandler(JobActivityInfoWebSocketHandler handler)
	 *           { this.handler = handler; }
	 * 
	 *           public void registerWebSocketHandlers(WebSocketHandlerRegistry
	 *           registry) { //registry.addHandler(handler,
	 *           "/crawler").addInterceptors(new HandShake());
	 *           //registry.addHandler(handler,
	 *           "/crawler/websocket").addInterceptors(new
	 *           HandShake()).withSockJS(); }
	 **/

	@Override
	public void configureMessageBroker(MessageBrokerRegistry config) {
		config.enableSimpleBroker("/topic");
		config.setApplicationDestinationPrefixes("/crawler");
	}

	@Override
	public void registerStompEndpoints(StompEndpointRegistry registry) {
		registry.addEndpoint("/crawler/websocket").withSockJS();
	}
}
