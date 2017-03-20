package six.com.crawler.interceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import six.com.crawler.node.NodeManager;

/**
 * @author six
 * @date 2016年1月14日 下午2:14:13 基础 拦截器
 */
public class BaseInterceptor implements HandlerInterceptor {

	NodeManager clusterManager;
	

	public BaseInterceptor(NodeManager clusterManager){
		this.clusterManager=clusterManager;
	}
	
	@Override
	public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {

		return true;
	}

	@Override
	public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			ModelAndView modelAndView) throws Exception {

	}

	@Override
	public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)
			throws Exception {
	}

}
