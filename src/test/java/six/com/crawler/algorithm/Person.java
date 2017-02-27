package six.com.crawler.algorithm;

/**
 * @author six
 * @date 2016年8月1日 下午2:21:04
 */
public class Person {

	private int age;// 年纪
	private int sex;// 性别
	private EducationBackground education;// 学历
	private Role role;// 角色
	private GroupType groupType;// 单位类型
	private Income income;// 收入

	public Income getIncome() {
		return income;
	}

	public void setIncome(Income income) {
		this.income = income;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public int getSex() {
		return sex;
	}

	public void setSex(int sex) {
		this.sex = sex;
	}

	public EducationBackground getEducation() {
		return education;
	}

	public void setEducation(EducationBackground education) {
		this.education = education;
	}

	public Role getRole() {
		return role;
	}

	public void setRole(Role role) {
		this.role = role;
	}

	public GroupType getGroupType() {
		return groupType;
	}

	public void setGroupType(GroupType groupType) {
		this.groupType = groupType;
	}
}
