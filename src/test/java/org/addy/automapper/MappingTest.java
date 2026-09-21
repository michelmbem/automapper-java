package org.addy.automapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

class MappingTest {
	
	@Test
	void applyWorks() {
		Person p = new Person("Jordan Mbem", 10, 'M', "2105 Rue Tupper, MTL, QC, CAN");
		Employee e = new Employee();
		
		Mapping<Person, Employee> mapping = new Mapping<>(Person.class, Employee.class)
				.forMember("address", Profile.ignore())
				.forMember("sex", Profile.convertUsing(v -> (byte) (v.equals('M') ? 1 : 0)));
		
		mapping.apply(p, e, null);
		
		assertEquals(e.getName(), p.getName());
		assertEquals(e.getAge(), p.getAge());
		assertEquals((byte) 1, e.getSex());
		assertNotEquals(e.address, p.address);
	}

	@Test
	void constructWorksWithRecords() {
		Person p1 = new Person("Daniel Mbem", 10, 'M', "2105 Rue Tupper, MTL, QC, CAN");
		Mapping<Person, Patient> mapping = new Mapping<>(Person.class, Patient.class);
		Patient p2 = mapping.construct(p1);

		assertEquals(p2.name(), p1.getName());
		assertEquals(p2.age(), p1.getAge());
		assertEquals(p2.sex(), p1.getSex());
		assertEquals(p2.address(), p1.address);
	}
	
	
	static class Person {
		
		private String name;
		private int age;
		private char sex;
		public String address;
		
		public Person() {
		}
		
		public Person(String name, int age, char sex, String address) {
			this.name = name;
			this.age = age;
			this.sex = sex;
			this.address = address;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public char getSex() {
			return sex;
		}

		public void setSex(char sex) {
			this.sex = sex;
		}
	}
	
	static class Employee {
		
		private String name;
		private int age;
		private byte sex;
		public String address;
		private String jobTitle;
		
		public Employee() {
		}
		
		public Employee(String name, int age, byte sex, String jobTitle) {
			this.name = name;
			this.age = age;
			this.sex = sex;
			this.jobTitle = jobTitle;
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public byte getSex() {
			return sex;
		}

		public void setSex(byte sex) {
			this.sex = sex;
		}

		public String getJobTitle() {
			return jobTitle;
		}

		public void setJobTitle(String jobTitle) {
			this.jobTitle = jobTitle;
		}
	}

	record Patient(String name, int age, char sex, String address) {}

}
