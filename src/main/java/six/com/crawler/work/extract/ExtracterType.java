package six.com.crawler.work.extract;


/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年3月27日 上午11:03:06
 * 抽取类型
 */
public enum ExtracterType {

	CssCommonSelect(0), CssTableForOne(1), CssTableForMany(2), Regular(3), Json(4);

	final int value;

	ExtracterType(int value) {
		this.value = value;
	}

	public int value() {
		return value;
	}

	public static ExtracterType valueOf(int type) {
		if (0 == type) {
			return CssCommonSelect;
		} else if (1 == type) {
			return CssTableForOne;
		} else if (2== type) {
			return CssTableForMany;
		} else if (3 == type) {
			return Regular;
		} else if (4 == type) {
			return Json;
		} else {
			return CssCommonSelect;
		}

	}
}
