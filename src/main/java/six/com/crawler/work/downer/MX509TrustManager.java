package six.com.crawler.work.downer;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.security.KeyStore;

import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

/**
 * @author six
 * @date 2016年5月11日 下午2:01:16
 */
public class MX509TrustManager implements X509TrustManager {

	X509TrustManager sunJSSEX509TrustManager;

	static SSLSocketFactory sSLSocketFactory;

	public static MX509TrustManager myX509TrustManager = new MX509TrustManager();

	private MX509TrustManager() {
		// create a "default" JSSE X509TrustManager.
		KeyStore ks = null;
		try {
			ks = KeyStore.getInstance(KeyStore.getDefaultType());
			File file = new File("jssecacerts");
			if (file.isFile() == false) {
				char SEP = File.separatorChar;
				File dir = new File(System.getProperty("java.home") + SEP + "lib" + SEP + "security");
				file = new File(dir, "jssecacerts");
				if (file.isFile() == false) {
					file = new File(dir, "cacerts");
				}
			}
			InputStream in = new FileInputStream(file);
			ks.load(in, null);
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509", "SunJSSE");
			tmf.init(ks);
			TrustManager tms[] = tmf.getTrustManagers();
			/*
			 * Iterate over the returned trustmanagers, look for an instance of
			 * X509TrustManager. If found, use that as our "default" trust
			 * manager.
			 */
			for (int i = 0; i < tms.length; i++) {
				if (tms[i] instanceof X509TrustManager) {
					sunJSSEX509TrustManager = (X509TrustManager) tms[i];
					return;
				}
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}

	}

	public static SSLSocketFactory getSSLSocketFactory() {
		if (null != sSLSocketFactory) {
			return sSLSocketFactory;
		} else {
			synchronized (SSLSocketFactory.class) {
				if (null == sSLSocketFactory) {
					TrustManager[] tm = { MX509TrustManager.myX509TrustManager };
					try {
						SSLContext sslContext = SSLContext.getInstance("SSL", "SunJSSE");
						sslContext.init(null, tm, new java.security.SecureRandom());
						// 从上述SSLContext对象中得到SSLSocketFactory对象
						sSLSocketFactory = sslContext.getSocketFactory();
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}
		}
		return sSLSocketFactory;
	}

	public void checkClientTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		try {
			sunJSSEX509TrustManager.checkClientTrusted(arg0, arg1);
		} catch (CertificateException excep) {
			// do any special handling here, or rethrow exception.
		}
	}

	public void checkServerTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
		try {
			sunJSSEX509TrustManager.checkServerTrusted(arg0, arg1);
		} catch (CertificateException excep) {
			/*
			 * Possibly pop up a dialog box asking whether to trust the cert
			 * chain.
			 */
		}
	}

	public X509Certificate[] getAcceptedIssuers() {
		return sunJSSEX509TrustManager.getAcceptedIssuers();
	}

}
