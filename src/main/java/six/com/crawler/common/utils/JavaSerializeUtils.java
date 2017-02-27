package six.com.crawler.common.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * @author sixliu E-mail:359852326@qq.com
 * @version 创建时间：2016年8月27日 下午2:12:45 类说明 java 自带序列化
 */
public class JavaSerializeUtils {

	/**
	 * 将对象序列化成 byte[]
	 * 
	 * @param object
	 * @return
	 * @throws IOException
	 */
	public static byte[] serialize(Object object) {
		byte[] dts = null;
		if (null != object) {
			try (ByteArrayOutputStream baos = new ByteArrayOutputStream();
					ObjectOutputStream obstr = new ObjectOutputStream(baos);) {
				obstr.writeObject(object);
				dts = baos.toByteArray();
			} catch (IOException e) {
				throw new RuntimeException("JavaSerializeUtils serialize err:" + object.getClass(), e);
			}
		}
		return dts;
	}

	/**
	 * 将bytes数组序列化成 对象
	 * 
	 * @param dts
	 * @param clz
	 * @return
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public static <T> T unSerialize(byte[] dts, Class<T> clz) {
		T result = null;
		if (null != dts && dts.length > 0 && null != clz) {
			try (ByteArrayInputStream bais = new ByteArrayInputStream(dts);
					ObjectInputStream ois = new ObjectInputStream(bais);) {
				Object ob = ois.readObject();
				result = clz.cast(ob);
			} catch (Exception e) {
				throw new RuntimeException("JavaSerializeUtils unSerialize err:" + clz, e);
			}
		}
		return result;
	}

	public static byte[] serializeString(String key) {
		byte[] bytes = key.getBytes();
		return bytes;
	}

	public static String unSerializeString(byte[] bytes) {
		return new String(bytes);
	}
}
