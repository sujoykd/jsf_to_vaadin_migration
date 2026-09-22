package br.com.webbudget.domain.entities.registration;

import jakarta.annotation.Generated;
import jakarta.persistence.metamodel.SingularAttribute;
import jakarta.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Vehicle.class)
public abstract class Vehicle_ extends br.com.webbudget.domain.entities.PersistentEntity_ {

	public static volatile SingularAttribute<Vehicle, String> identification;
	public static volatile SingularAttribute<Vehicle, String> licensePlate;
	public static volatile SingularAttribute<Vehicle, Integer> manufacturingYear;
	public static volatile SingularAttribute<Vehicle, Long> odometer;
	public static volatile SingularAttribute<Vehicle, Integer> fuelCapacity;
	public static volatile SingularAttribute<Vehicle, CostCenter> costCenter;
	public static volatile SingularAttribute<Vehicle, Boolean> active;
	public static volatile SingularAttribute<Vehicle, String> model;
	public static volatile SingularAttribute<Vehicle, Integer> modelYear;
	public static volatile SingularAttribute<Vehicle, String> brand;
	public static volatile SingularAttribute<Vehicle, VehicleType> vehicleType;

	public static final String IDENTIFICATION = "identification";
	public static final String LICENSE_PLATE = "licensePlate";
	public static final String MANUFACTURING_YEAR = "manufacturingYear";
	public static final String ODOMETER = "odometer";
	public static final String FUEL_CAPACITY = "fuelCapacity";
	public static final String COST_CENTER = "costCenter";
	public static final String ACTIVE = "active";
	public static final String MODEL = "model";
	public static final String MODEL_YEAR = "modelYear";
	public static final String BRAND = "brand";
	public static final String VEHICLE_TYPE = "vehicleType";

}

