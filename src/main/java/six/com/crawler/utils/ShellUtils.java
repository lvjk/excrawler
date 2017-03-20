package six.com.crawler.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


import ch.ethz.ssh2.ChannelCondition;
import ch.ethz.ssh2.Connection;
import ch.ethz.ssh2.Session;
import ch.ethz.ssh2.StreamGobbler;

/**
 * @author six
 * @date 2015年12月31日 上午11:23:56
 */
public class ShellUtils {
	private static Logger log = LoggerFactory.getLogger(ShellUtils.class);
	private static String charset = Charset.defaultCharset().toString();
	private static final int TIME_OUT = 1000 * 5 * 60;

	private static Connection openShell(String ip, String usr, String passwd) {
		Connection conn = new Connection(ip);
		try {
			conn.connect();
			if(!conn.authenticateWithPassword(usr, passwd)){
				log.error("open connectio err:"+ip);
			}
		} catch (IOException e) {
			log.error("open connectio err:"+ip,e);
		}
		return conn;
	}

	public static int executeShell(String ip, String usr, String passwd, String command){
		InputStream stdOut = null;
		InputStream stdErr = null;
		String outStr = null;
		String outErr = null;
		int ret = -1;
		Connection conn = openShell(ip, usr, passwd);
		try {
			Session session = conn.openSession();
			session.execCommand(command);
			log.info("execute shell command:"+command);
			stdOut = new StreamGobbler(session.getStdout());
			outStr = processStream(stdOut, charset);
			stdErr = new StreamGobbler(session.getStderr());
			outErr = processStream(stdErr, charset);
			session.waitForCondition(ChannelCondition.EXIT_STATUS, TIME_OUT);
			log.info("execute shell command respone:"+outStr);
			log.info("execute shell command respone err:"+outErr);
			ret = session.getExitStatus();
		}catch(Exception e){
			log.error("execute shell command err:"+command,e);
		}finally {
			if (conn != null) {
				conn.close();
			}
			IOUtils.closeQuietly(stdOut);
			IOUtils.closeQuietly(stdErr);
		}
		return ret;
	}

	/** */
	/**
	 * @param in
	 * @param charset
	 * @return
	 * @throws IOException
	 * @throws UnsupportedEncodingException
	 */
	private static String processStream(InputStream in, String charset)
			throws Exception {
		byte[] buf = new byte[1024];
		StringBuilder sb = new StringBuilder();
		while (in.read(buf) != -1) {
			sb.append(new String(buf, charset));
		}
		return sb.toString();
	}


}
