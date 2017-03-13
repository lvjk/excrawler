package six.com.crawler.common.entity;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月10日 下午1:31:19
 */
public abstract class PageQueryEntity {

	private int totalSize;

	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}

	public int getTotalSize() {
		return totalSize;
	}
}
