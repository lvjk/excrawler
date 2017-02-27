package six.com.crawler.work.downer;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月8日 上午11:30:10 
*/
public class ProxyAuthenticator extends Authenticator {
    private String user, password;

    public ProxyAuthenticator(String user, String password) {
        this.user     = user;
        this.password = password;
    }

    protected PasswordAuthentication getPasswordAuthentication() {
        return new PasswordAuthentication(user, password.toCharArray());
    }
}