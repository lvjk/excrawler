package six.com.crawler.email;


import javax.mail.Authenticator;
import javax.mail.PasswordAuthentication;

/**
 * @author six
 * @date 2016年8月31日 上午10:29:17
 */
public class Email_Authenticator extends Authenticator {
	String userName;
	String password;

	public Email_Authenticator() {
	}

	public Email_Authenticator(String username, String password) {
		this.userName = username;
		this.password = password;
	}

	protected PasswordAuthentication getPasswordAuthentication() {
		return new PasswordAuthentication(userName, password);
	}
}
