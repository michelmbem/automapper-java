package org.addy.automapper;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

class AutoMapperTest {
	
	static long timeEllapsed;
	
	Person p = new Person(
			"Michel Mbem",
			43,
			'M',
			"2105 Rue Tupper, MTL, QC, CAN",
			"Computer Scientist");

	AutoMapper mapper = new AutoMapper(new TestProfile());
	
	@BeforeAll
	static void init() {
		timeEllapsed = System.currentTimeMillis();
	}
	
	@AfterAll
	static void conclude() {
		timeEllapsed = System.currentTimeMillis() - timeEllapsed;
		System.out.printf("Time ellapsed: %dms%n", timeEllapsed);
	}
	
	@Test
	void mapWith2ArgsWorks() {
		Employee e = new Employee();
		mapper.map(p, e);
		
		assertEquals(p.getName(), e.getName());
		assertEquals(p.getAge(), e.getAge());
		assertEquals(e.getSex(), (byte) 1);
		assertNotEquals(e.address, p.address);
		assertThat(e.address).isNull();
		assertEquals(p.occupation, e.getJobTitle());
	}
	
	@Test
	void reflexiveMapWorks() {
		Person p2 = mapper.map(p, Person.class);

		assertNotSame(p, p2);
		assertEquals(p.getName(), p2.getName());
		assertEquals(p.getAge(), p2.getAge());
		assertEquals(p.getSex(), p2.getSex());
		assertEquals(p.address, p2.address);
		assertEquals(p.occupation, p2.occupation);
	}
	
	@Test
	void constructorWorks() {
		String s = mapper.map(p, String.class);
		assertThat(s).isEqualTo(p.getName());
	}
	
	
	static class Person {
		
		private String name;
		private int age;
		private char sex;
		public String address;
		public String occupation;
		
		public Person() {
		}
		
		public Person(String name, int age, char sex, String address, String occupation) {
			this.name = name;
			this.age = age;
			this.sex = sex;
			this.address = address;
			this.occupation = occupation;
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

		public void setName(String name) {
			this.name = name;
		}

		public String getName() {
			return name;
		}

		public int getAge() {
			return age;
		}

		public void setAge(int age) {
			this.age = age;
		}

		public void setSex(byte sex) {
			this.sex = sex;
		}

		public byte getSex() {
			return sex;
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
				.forMember("sex", convertUsing(v -> (byte) (v.equals('M') ? 1 : 0)))
				.forMember("jobTitle", mapFrom("occupation"));
			
			createMap(Person.class, Person.class);
			
			createMap(Person.class, String.class)
				.constructUsing(e -> e.getName());
		}
		
	}
	
}
