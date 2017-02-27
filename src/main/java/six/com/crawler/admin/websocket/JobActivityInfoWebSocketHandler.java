package six.com.crawler.admin.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年10月19日 下午3:37:46 监听客户端监听的任务活动信息
 */
public class JobActivityInfoWebSocketHandler implements WebSocketHandler, InitializingBean {

	static Logger LOG = LoggerFactory.getLogger(JobActivityInfoWebSocketHandler.class);

	private Map<String, WebSocketSession> userSocketSessionMap = new ConcurrentHashMap<String, WebSocketSession>();

	private Map<String, List<String>> userListenJobMap = new ConcurrentHashMap<String, List<String>>();

	
	Thread listenJobActivityThead;


	public void afterPropertiesSet() throws Exception {
		// listenJobActivityThead = new Thread(() -> listenJobActivity(),
		// "listenJobActivityThead");
		// listenJobActivityThead.setDaemon(true);
		// listenJobActivityThead.start();
	}

	protected void listenJobActivity() {}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		userSocketSessionMap.put(session.getId(), session);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> message) throws Exception {
		if (message.getPayloadLength() > 0) {
			String uid = session.getId();
			String jobsStr = message.getPayload().toString();
			String[] tempJobNames = jobsStr.split(";");
			List<String> jobNameList = new ArrayList<>(tempJobNames.length);
			for (String jobName : tempJobNames) {
				jobNameList.add(jobName);
			}
			userListenJobMap.put(uid, jobNameList);
		}
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {
		String uid = session.getId();
		WebSocketSession removeSession = userSocketSessionMap.remove(uid);
		if (removeSession.isOpen()) {
			removeSession.close();
		}
	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		String uid = session.getId();
		WebSocketSession removeSession = userSocketSessionMap.remove(uid);
		if (removeSession.isOpen()) {
			removeSession.close();
		}
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}

}
