package org.addy.automapper;

import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

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

	@Test
	void getPropertiesWorks() {
		final int flags = PropertyHelper.DECLARED | PropertyHelper.INSTANCE
				| PropertyHelper.FIELD | PropertyHelper.ENCAPSULATED;

		List<Property> properties = PropertyHelper.getProperties(Person.class, flags);

		// There is an additional property introduced by the getClass method
		assertEquals(5, properties.size());

		Property name = properties.stream()
				.filter(p -> p.getName().equals("name"))
				.findFirst().orElse(null);
		assertInstanceOf(MethodProperty.class, name);
		assertEquals(String.class, name.getType());
		assertTrue(name.isReadable());
		assertTrue(name.isWritable());

		Property age = properties.stream()
				.filter(p -> p.getName().equals("age"))
				.findFirst().orElse(null);
		assertInstanceOf(MethodProperty.class, age);
		assertEquals(int.class, age.getType());
		assertTrue(age.isReadable());
		assertTrue(age.isWritable());

		Property sex = properties.stream()
				.filter(p -> p.getName().equals("sex"))
				.findFirst().orElse(null);
		assertInstanceOf(MethodProperty.class, sex);
		assertEquals(char.class, sex.getType());
		assertTrue(sex.isReadable());
		assertTrue(sex.isWritable());

		Property address = properties.stream()
				.filter(p -> p.getName().equals("address"))
				.findFirst().orElse(null);
		assertInstanceOf(FieldProperty.class, address);
		assertEquals(String.class, address.getType());
		assertTrue(address.isReadable());
		assertTrue(address.isWritable());
	}

	@Test
	void getPropertiesWorksWithRecords() {
		final int flags = PropertyHelper.DECLARED | PropertyHelper.INSTANCE | PropertyHelper.FIELD | PropertyHelper.ENCAPSULATED;
		List<Property> properties = PropertyHelper.getProperties(Address.class, flags);

		assertEquals(3, properties.size());

		Property street = properties.get(0);
		assertInstanceOf(MethodProperty.class, street);
		assertEquals("street", street.getName());
		assertEquals(String.class, street.getType());
		assertTrue(street.isReadable());
		assertFalse(street.isWritable());

		Property city = properties.get(1);
		assertInstanceOf(MethodProperty.class, city);
		assertEquals("city", city.getName());
		assertEquals(String.class, city.getType());
		assertTrue(city.isReadable());
		assertFalse(city.isWritable());

		Property country = properties.get(2);
		assertInstanceOf(MethodProperty.class, country);
		assertEquals("country", country.getName());
		assertEquals(String.class, country.getType());
		assertTrue(country.isReadable());
		assertFalse(country.isWritable());
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
