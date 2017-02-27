package six.com.crawler.work.downer;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

public class TestUtils {

	
	public static void writeHtmlToTestFile(String html){
		try {
			FileUtils.write(new File("F:/test/test.html"), html);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
