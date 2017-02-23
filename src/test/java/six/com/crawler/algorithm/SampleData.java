package six.com.crawler.algorithm;

import java.util.List;

/**
 * @author six
 * @date 2016年8月1日 下午2:37:27
 */
public class SampleData {

	public static List<Person> getSampleData() {
		Person person=new Person();
		person.setAge(30);
		person.setSex(0);
		person.setEducation(EducationBackground.Doctor);
		person.setGroupType(GroupType.BigCompany);
		person.setRole(Role.Engineer);
		person.setIncome(Income.Income_4);
		return null;
	}
}
