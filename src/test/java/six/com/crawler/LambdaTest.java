package six.com.crawler;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.function.Predicate;

/**
 * @author 作者
 * @E-mail: 359852326@qq.com
 * @date 创建时间：2016年11月21日 下午5:35:14
 */
public class LambdaTest {
	public static long java7Filter(List<Student> lists) {
//		int count = 0;
//		for (Student student : lists) {
//			if (student.age >= 18) {
//				count++;
//			}
//		}
//		return count;
		return lists.stream().parallel().filter(new Predicate<Student>() {
		 public boolean test(Student student) {
		 return (student.age >= 18);
		 }
		 }).count();
	}

	/** lambda测试 */
	public static long java8Filter(List<Student> lists) {
		return lists.stream().parallel().filter(student -> {
			return student.age >= 18;
		}).count();
	}

	public static void main(String[] args) {
		int forSize = 20;
		int dateSize = 10000000;
		long java7Time = 0;
		long java8Time = 0;
		List<Student> list = creatDate(dateSize);
		for (int i = 0; i < forSize; i++) {
			long start = System.currentTimeMillis();
			java7Filter(list);
			long end = System.currentTimeMillis();
			java7Time += end - start;
			start = System.currentTimeMillis();
			java8Filter(list);
			end = System.currentTimeMillis();
			java8Time += end - start;
		}
		System.out.println("java7 avg time:" + (java7Time / forSize));
		System.out.println("java8 avg time:" + (java8Time / forSize));
	}

	// 随机对象
	private static Random randomNum = new Random();

	public static List<Student> creatDate(int size) {
		List<Student> list = new ArrayList<LambdaTest.Student>(size);
		Student student = null;
		for (int i = 0; i < size; i++) {
			student = new Student();
			student.name = "student-" + i;
			student.age = randomNum.nextInt(30);
			list.add(student);

		}
		return list;
	}

	static class Student {
		String name;
		int age;
	}

}
