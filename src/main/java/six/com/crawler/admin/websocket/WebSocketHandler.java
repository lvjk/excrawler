package six.com.crawler.admin.websocket;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年5月17日 下午6:13:36
 */
public class WebSocketHandler extends TextWebSocketHandler {

	private final static List<WebSocketSession> sessions = Collections
			.synchronizedList(new ArrayList<WebSocketSession>());

	// 接收文本消息，并发送出去
	@Override
	protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
		super.handleTextMessage(session, message);
	}

	// 连接建立后处理
	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		sessions.add(session);
		// 处理离线消息
	}

	// 抛出异常时处理
	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		if (session.isOpen()) {
			session.close();
		}
		sessions.remove(session);
	}

	// 连接关闭后处理
	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		sessions.remove(session);
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
