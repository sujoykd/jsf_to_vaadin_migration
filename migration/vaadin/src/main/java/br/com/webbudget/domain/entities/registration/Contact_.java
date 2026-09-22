package br.com.webbudget.domain.entities.registration;

import java.time.LocalDate;
import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.ListAttribute;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Contact.class)
public abstract class Contact_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Contact, String> code;
	public static volatile SingularAttribute<Contact, String> city;
	public static volatile SingularAttribute<Contact, String> document;
	public static volatile ListAttribute<Contact, Telephone> telephones;
	public static volatile SingularAttribute<Contact, Boolean> active;
	public static volatile SingularAttribute<Contact, ContactType> contactType;
	public static volatile SingularAttribute<Contact, LocalDate> birthDate;
	public static volatile SingularAttribute<Contact, String> otherInformation;
	public static volatile SingularAttribute<Contact, String> zipcode;
	public static volatile SingularAttribute<Contact, String> number;
	public static volatile SingularAttribute<Contact, String> province;
	public static volatile SingularAttribute<Contact, String> street;
	public static volatile SingularAttribute<Contact, String> name;
	public static volatile SingularAttribute<Contact, String> neighborhood;
	public static volatile SingularAttribute<Contact, String> complement;
	public static volatile SingularAttribute<Contact, String> email;

	public static final String CODE = "code";
	public static final String CITY = "city";
	public static final String DOCUMENT = "document";
	public static final String TELEPHONES = "telephones";
	public static final String ACTIVE = "active";
	public static final String CONTACT_TYPE = "contactType";
	public static final String BIRTH_DATE = "birthDate";
	public static final String OTHER_INFORMATION = "otherInformation";
	public static final String ZIPCODE = "zipcode";
	public static final String NUMBER = "number";
	public static final String PROVINCE = "province";
	public static final String STREET = "street";
	public static final String NAME = "name";
	public static final String NEIGHBORHOOD = "neighborhood";
	public static final String COMPLEMENT = "complement";
	public static final String EMAIL = "email";

}

