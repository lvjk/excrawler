package six.com.crawler.admin.websocket;

import org.springframework.context.annotation.Bean;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月9日 上午9:41:41
 */
//@Configuration
//@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

	@Override
	public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
		String[] allowsOrigins = { "http://www.xxx.com" };
		registry.addHandler(chatWebSocketHandler(), "/webSocketIMServer").setAllowedOrigins(allowsOrigins);
		registry.addHandler(chatWebSocketHandler(), "/sockjs/webSocketIMServer").setAllowedOrigins(allowsOrigins)
				.withSockJS();
	}

	@Bean
	public WebSocketHandler chatWebSocketHandler() {
		return new WebSocketHandler();
	}

}
