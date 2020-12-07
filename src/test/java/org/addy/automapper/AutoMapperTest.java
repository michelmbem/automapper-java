package org.addy.automapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.Test;

class AutoMapperTest {
	
	@Test
	void mapWith2ArgsWorks() {
		Person p = new Person("Michel Mbem", 43, 'M', "2105 Rue Tupper, MTL, QC, CAN");
		Employee e = new Employee();
		AutoMapper mapper = new AutoMapper(new TestProfile());
		
		mapper.map(p, e);
		
		assertEquals(p.getName(), e.getName());
		assertEquals(p.getAge(), e.getAge());
		assertEquals(e.getSex(), (byte) 1);
		assertNotEquals(e.address, p.address);
	}
	
	@Test
	void reflexiveMapWorks() {
		Person p = new Person("Michel Mbem", 43, 'M', "2105 Rue Tupper, MTL, QC, CAN");
		AutoMapper mapper = new AutoMapper(new TestProfile());
		
		Person p2 = mapper.map(p, Person.class);

		assertNotSame(p, p2);
		assertEquals(p.getName(), p2.getName());
		assertEquals(p.getAge(), p2.getAge());
		assertEquals(p.getSex(), p2.getSex());
		assertEquals(p.address, p2.address);
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

	static class TestProfile extends Profile {
		
		public TestProfile() {
			createMap(Person.class, Employee.class)
				.forMember("address", ignore())
				.forMember("sex", convertUsing(v -> (byte) (v.equals('M') ? 1 : 0)));
			
			createMap(Person.class, Person.class);
		}
		
	}
	
}
