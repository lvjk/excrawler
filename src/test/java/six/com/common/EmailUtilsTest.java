package six.com.common;

import javax.mail.MessagingException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.BaseTest;
import six.com.crawler.common.email.QQEmailClient;

/**
 * @author six
 * @date 2016年8月31日 上午10:23:29
 */
public class EmailUtilsTest extends BaseTest{

	protected final static Logger LOG = LoggerFactory.getLogger(EmailUtilsTest.class);
			
	@Test
	public void test(){
		String mailHost =spiderConfigure.getConfig("email.host","smtp.qq.com");
		String mailPost = spiderConfigure.getConfig("email.post","465");
		String mailUser = spiderConfigure.getConfig("email.user","359852326@qq.com");
		String mailPwd = spiderConfigure.getConfig("email.pwd","auqoidnoizodbijf");
		QQEmailClient emailClient=new QQEmailClient(mailHost, mailPost, mailUser, mailPwd);
		String subject = "test send mail";
		String msg = "hello";
		for (String to : spiderConfigure.getAdminEmails()) {
			try {
				emailClient.sendMail(to, subject,msg);
			} catch (MessagingException e1) {
				LOG.error("send mail err:"+to,e1);
			}
		}
	}

}
