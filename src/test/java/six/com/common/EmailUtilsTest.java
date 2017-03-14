package six.com.common;

import javax.mail.MessagingException;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import six.com.crawler.BaseTest;

/**
 * @author six
 * @date 2016年8月31日 上午10:23:29
 */
public class EmailUtilsTest extends BaseTest {

	protected final static Logger LOG = LoggerFactory.getLogger(EmailUtilsTest.class);

	@Test
	public void test() {
		String subject = "test send mail";
		String msg = "hello";
		for (String to : spiderConfigure.getAdminEmails()) {
			try {
				QQEmailClient.sendMail(to, subject, msg);
			} catch (MessagingException e1) {
				LOG.error("send mail err:" + to, e1);
			}
		}
	}

}
