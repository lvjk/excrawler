package six.com.crawler.rpc;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月22日 上午10:18:08
 */
public class Signal extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 2308882870528693053L;

	private int type;

	public Signal(int type, String message) {
		super(message);
		this.type = type;
	}

	public int getType() {
		return type;
	}
}
