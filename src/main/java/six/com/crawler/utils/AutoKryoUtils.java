package six.com.crawler.utils;

import com.esotericsoftware.kryo.Kryo;
import com.esotericsoftware.kryo.io.Input;
import com.esotericsoftware.kryo.io.Output;

/**
 * @author six
 * @date 2016年7月6日 上午11:47:33
 */
public enum AutoKryoUtils {

	INSTANCE;
	private Kryo Kryo = new Kryo();
	private final static int min = 1;
	private final static int max = Integer.MAX_VALUE;

	public byte[] serialize(Object ob) {
		byte[] bytes = null;
		try (Output output = new Output(min, max);) {
			Kryo.writeObject(output, ob);
			bytes = output.getBuffer();
			output.flush();
		}
		return bytes;
	}

	public <T> T unSerialize(byte[] bytes, Class<T> clz) {
		T result = null;
		try (Input input = new Input(bytes);) {
			Object ob = Kryo.readObject(input, clz);
			result = clz.cast(ob);
		}
		return result;
	}

}
