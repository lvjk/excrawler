package six.com.common;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.apache.commons.lang3.time.DateUtils;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2017年4月12日 上午11:49:46
 */
public class DownerTimeFileDoTest {

	public static void main(String[] args) throws Exception {
		doFile();
	}

	private static void filterFile() throws IOException {
		String fileName = "F:/six/crawler.20170411.0.log";
		String newFileName = "F:/six/downerTime.txt";
		List<String> srcList = FileUtils.readLines(new File(fileName));
		List<String> list = new ArrayList<>();
		for (String line : srcList) {
			if (StringUtils.contains(line, "down time")) {
				list.add(line);
			}
		}
		FileUtils.writeLines(new File(newFileName), list);
	}

	private static void doFile() throws Exception {
		String fileName = "F:/six/downerTime.txt";
		List<String> srcList = FileUtils.readLines(new File(fileName));
		Map<Date, Long> timeMap = new TreeMap<>(new Comparator<Date>() {
			@Override
			public int compare(Date o1,Date o2) {
				return o1.compareTo(o2);
			}
		});
		Map<String, Date> dateMap = new HashMap<>();
		Map<String, Integer> countMap = new HashMap<>();
		Long total = 0L;Calendar calendar=Calendar.getInstance();
		for (String line : srcList) {
			String time = 2017 + StringUtils.substringBetween(line, "[2017", "]");
			Date date = DateUtils.parseDate(time, "yyyy-MM-dd hh:mm:ss");
			calendar.setTime(date);
			String day =(calendar.get(Calendar.MONTH)+1)+ "月"+calendar.get(Calendar.DAY_OF_MONTH)+"日"+calendar.get(Calendar.HOUR_OF_DAY);
			String downerTimeStr = StringUtils.substringBetween(line, "down time[", "]");
			Long downerTime = Long.valueOf(downerTimeStr);
			Date mapDate=dateMap.computeIfAbsent(day, mapKey->{
				try {
					return DateUtils.parseDate(time, "yyyy-MM-dd hh:mm:ss");
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			});
			total = timeMap.get(mapDate);
			if(null==total){
				total= new Long(0L);
				timeMap.put(mapDate, total);
			}
			total += downerTime;
			Integer count = countMap.computeIfAbsent(day, mapKey -> new Integer(0));
			count++;
			countMap.put(day, count);
			timeMap.put(mapDate, total);
			System.out.println("下载时间[" + time + "]:" + downerTime);
		}

		for (Date key : timeMap.keySet()) {
			Long totalTime = timeMap.get(key);
			Integer count = countMap.get(key);
			System.out.println("统计每小时平均下载时间[" + key.toString() + "]:" + (totalTime / count));
		}
	}

}
