package org.addy.automapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PropertyHelperTest {
	
	@Test
	void getPropertiesWorks() {
		Person p = new Person("Michel Mbem", 17, 'M', "2105 Rue Tupper, MTL, QC, CAN");
		Property name = PropertyHelper.getProperty(Person.class, "name",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.ENCAPSULATED);
		Property age = PropertyHelper.getProperty(Person.class, "age",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.ENCAPSULATED);
		Property address = PropertyHelper.getProperty(Person.class, "address",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.FIELD);
		
		assertEquals(name.getValue(p), "Michel Mbem");
		
		age.setValue(p, 43);
		assertEquals(p.getAge(), 43);
		
		assertEquals(address.getValue(p), "2105 Rue Tupper, MTL, QC, CAN");
		address.setValue(p, "115-2105 Rue Tupper, Montréal, Québec, Canada");
		assertEquals(p.address, "115-2105 Rue Tupper, Montréal, Québec, Canada");
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

}
