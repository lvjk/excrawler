package six.com.crawler.common.entity;

import java.io.StringWriter;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月1日 下午5:24:24
 */
public class Profile {

	public static String getValueStr(Node node) {
		String text = null;
		if (null != node) {
			text = node.getNodeValue();
		}
		return text;
	}

	public static int getValueInt(Node node) {
		return null != node ? Integer.valueOf(node.getNodeValue()) : 0;
	}

	public static String getValue(String value) {
		if ("null".equalsIgnoreCase(value) || null == value) {
			return "";
		}
		return value;
	}

	public static String toXml(Document doc) throws TransformerException {
		TransformerFactory tff = TransformerFactory.newInstance();
		Transformer tf = tff.newTransformer();
		tf.setOutputProperty(OutputKeys.INDENT, "yes");
		StringWriter stringWriter = new StringWriter();
		StreamResult result = new StreamResult(stringWriter);
		tf.transform(new DOMSource(doc), result);
		return stringWriter.toString();
	}
}
