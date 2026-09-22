package br.com.webbudget.domain.repositories.registration;

import br.com.webbudget.domain.entities.registration.Address;

public interface AddressRepository {

    Address findByZipcode(String zipcode);
}
