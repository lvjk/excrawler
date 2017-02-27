package six.com.crawler.admin.api;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 *@author six    
 *@date 2016年5月30日 下午4:38:03  
*/
@Controller
public class WelcomeApi {

	@RequestMapping ("/crawler")
	public String crawler() {
		return "/html/main.html";
	}
}
