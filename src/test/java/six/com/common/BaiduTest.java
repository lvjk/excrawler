package six.com.common;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月31日 下午2:01:41 
*/
public class BaiduTest {

	public static void main(String[] args) {
		List<String> longitudes = null;
		List<String> latitudes = null;
		try {
			longitudes = FileUtils.readLines(new File("C:/Users/38134/Desktop/masa需求/longitude.txt"));
			latitudes = FileUtils.readLines(new File("C:/Users/38134/Desktop/masa需求/latitude.txt"));
			Set<String> lgSet=new HashSet<>();
			Set<String> ltSet=new HashSet<>();
			lgSet.addAll(longitudes);
			ltSet.addAll(latitudes);
			System.out.println("经度size:"+lgSet.size());
			System.out.println("纬度size:"+ltSet.size());
		} catch (IOException e) {
			throw new RuntimeException("read longitudes or latitudes err", e);
		}

	}

}
