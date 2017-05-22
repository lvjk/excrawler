package six.com.crawler.utils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月9日 上午11:23:59
 */
public class ExceptionUtils {

	public static String getExceptionMsg(Throwable throwable) {
		String msg = null;
		if (null != throwable) {
			StringBuilder msgSb = new StringBuilder();
			Throwable tempThrowable = throwable;
			doExceptionMsg(tempThrowable, msgSb, true);
			Throwable[] suppresseds = throwable.getSuppressed();
			for (int i = 0; i < suppresseds.length; i++) {
				tempThrowable = suppresseds[i];
				getExceptionMsg(tempThrowable, msgSb, false);
			}
			msg = msgSb.toString();
		}
		return msg;
	}

	public static void getExceptionMsg(Throwable throwable, StringBuilder msgSb, boolean isGetStackTrace) {
		Throwable tempThrowable = throwable;
		doExceptionMsg(tempThrowable, msgSb, isGetStackTrace);
		Throwable[] suppresseds = throwable.getSuppressed();
		for (int i = 0; i < suppresseds.length; i++) {
			tempThrowable = suppresseds[i];
			getExceptionMsg(tempThrowable, msgSb, isGetStackTrace);
		}
	}

	public static void doExceptionMsg(Throwable throwable, StringBuilder msgSb, boolean isGetStackTrace) {
		do {
			msgSb.append(throwable.getClass());
			msgSb.append(":");
			msgSb.append(throwable.getMessage());
			msgSb.append("\n");
			if (isGetStackTrace) {
				StackTraceElement[] stackTraceElements = throwable.getStackTrace();
				for (StackTraceElement stackTraceElement : stackTraceElements) {
					msgSb.append("\t\t\t");
					msgSb.append(stackTraceElement.getClassName());
					msgSb.append(".");
					msgSb.append(stackTraceElement.getMethodName());
					msgSb.append("(");
					msgSb.append(stackTraceElement.getFileName());
					msgSb.append(":");
					msgSb.append(stackTraceElement.getLineNumber());
					msgSb.append(")");
					msgSb.append("\n");
				}
			}
			throwable = throwable.getCause();
		} while (null != throwable);
	}
}
