package six.com.common;

import org.apache.commons.lang3.time.DateFormatUtils;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年1月9日 上午9:15:54 
*/
public class ComTest {

	public static void main(String[] args) {
		String DEFAULT_FOMAT = "yyyy-MM-dd HH:mm:ss";
		String date=DateFormatUtils.format(System.currentTimeMillis(), DEFAULT_FOMAT);
		System.out.println(date);
	}

}
