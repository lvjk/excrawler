package six.com.crawler.common.email;

import java.io.File;
import java.util.Properties;

import javax.mail.Authenticator;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * @author six
 * @date 2016年8月31日 上午10:00:20
 */
public class QQEmailClient {

	private Properties prop;
	private Authenticator authenticator;
	private String MAIL_Host;
	private String MAIL_USER;
	private String MAIL_PWD;
	private String MAIL_PORT;

	public QQEmailClient(String MAIL_Host, String MAIL_PORT, String MAIL_USER, String MAIL_PWD) {
		this.MAIL_Host = MAIL_Host;
		this.MAIL_PORT = MAIL_PORT;
		this.MAIL_USER = MAIL_USER;
		this.MAIL_PWD = MAIL_PWD;
		prop = new Properties();
		prop.setProperty("mail.host", this.MAIL_Host);
		prop.setProperty("mail.transport.protocol", "smtp");
		prop.setProperty("mail.smtp.auth", "true");
		prop.setProperty("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
		prop.setProperty("mail.smtp.port", this.MAIL_PORT);
		authenticator = new Email_Authenticator(this.MAIL_USER, this.MAIL_PWD);
	}

	/**
	 * 发送email
	 * 
	 * @param to
	 *            目标地
	 * @param subject
	 *            主题
	 * @param message
	 *            消息
	 * @throws MessagingException
	 * @throws AddressException
	 */
	public void sendMail(String to, String subject, String msg) throws AddressException, MessagingException {
		// 1、创建session
		Session session = Session.getDefaultInstance(prop, authenticator);
		// 开启Session的debug模式，这样就可以查看到程序发送Email的运行状态
		session.setDebug(true);
		// 4、创建邮件
		Message message = null;
		message = createSimpleMail(session, to, subject, msg);
		// 5、发送邮件
		Transport.send(message);
	}

	private MimeMessage createSimpleMail(Session session, String to, String subject, String msg)
			throws AddressException, MessagingException {
		// 创建邮件对象
		MimeMessage message = new MimeMessage(session);
		// 指明邮件的发件人
		message.setFrom(new InternetAddress(MAIL_USER));
		// 指明邮件的收件人，现在发件人和收件人是一样的，那就是自己给自己发
		message.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
		// 邮件的标题
		message.setSubject(subject);
		// 邮件的文本内容
		message.setContent(msg, "text/html;charset=UTF-8");
		// 返回创建好的邮件对象
		return message;
	}

	/**
	 * 发送email
	 * 
	 * @param to
	 *            目标地
	 * @param subject
	 *            主题
	 * @param message
	 *            消息
	 * @param file
	 *            附件 文件
	 */
	public void sendMail(String to, String subject, String message, File file) {

	}
}
