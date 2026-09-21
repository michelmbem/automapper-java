package org.addy.automapper;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class PropertyHelperTest {
	
	@Test
	void getPropertyWorks() {
		Person p = new Person("Michel Mbem", 17, 'M', "2105 Rue Tupper, MTL, QC, CAN");
		Property name = PropertyHelper.getProperty(Person.class, "name",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.ENCAPSULATED);
		Property age = PropertyHelper.getProperty(Person.class, "age",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.ENCAPSULATED);
		Property address = PropertyHelper.getProperty(Person.class, "address",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.FIELD);
		
		assertEquals("Michel Mbem", name.getValue(p));
		
		age.setValue(p, 43);
		assertEquals(43, p.getAge());
		
		assertEquals("2105 Rue Tupper, MTL, QC, CAN", address.getValue(p));
		address.setValue(p, "115-2105 Rue Tupper, Montréal, Québec, Canada");
		assertEquals("115-2105 Rue Tupper, Montréal, Québec, Canada", p.address);
	}

	@Test
	void getPropertyWorksWithRecords() {
		Address a = new Address("1852 Ultraford Street", "Manchester", "UK");
		Property street = PropertyHelper.getProperty(Address.class, "street",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.ENCAPSULATED);
		Property city = PropertyHelper.getProperty(Address.class, "city",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.ENCAPSULATED);
		Property country = PropertyHelper.getProperty(Address.class, "country",
				PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.ENCAPSULATED);

		assertEquals("1852 Ultraford Street", street.getValue(a));
		assertEquals("Manchester", city.getValue(a));
		assertEquals("UK", country.getValue(a));
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

	record Address(String street, String city, String country) {}

}
