package six.com.crawler.interceptor;


import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import six.com.crawler.node.NodeManager;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月17日 下午4:16:02
 */
public class MasterScheduledApiInterceptor implements HandlerInterceptor {

	NodeManager clusterManager;

	public MasterScheduledApiInterceptor(NodeManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
//		if (handler instanceof org.springframework.web.method.HandlerMethod) {
//			HandlerMethod handlerMethod = (org.springframework.web.method.HandlerMethod) handler;
//			OnlyVisitByWorker onlyVisitByWorker = handlerMethod.getMethodAnnotation(OnlyVisitByWorker.class);
//			if (null != onlyVisitByWorker) {
//				request.getLocalAddr();
//			}
//		}
		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
		// TODO Auto-generated method stub

	}

}
