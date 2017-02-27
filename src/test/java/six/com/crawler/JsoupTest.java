package six.com.crawler;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;


/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年1月12日 上午11:11:56 
*/
public class JsoupTest {

	public static void main(String[] args) throws IOException {
		String html=FileUtils.readFileToString(new File("C:/Users/38134/Desktop/test.html"));
		Document doc=Jsoup.parse(html);
		Element element=doc.select("body>script").first();
		String scripthtml=element.html();
		System.out.println(scripthtml);
	}

}
