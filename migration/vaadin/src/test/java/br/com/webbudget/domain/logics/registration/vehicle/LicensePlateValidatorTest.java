package br.com.webbudget.domain.logics.registration.vehicle;

import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.entities.registration.VehicleType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class LicensePlateValidatorTest {

    @Test
    void throws_when_license_plate_already_registered() {
        var existing = vehicle("ABC-1234", VehicleType.PRIVATE_CAR);
        var repo = mock(VehicleRepository.class);
        when(repo.findByLicensePlate("ABC-1234")).thenReturn(Optional.of(existing));

        var validator = new LicensePlateValidator(repo);
        var newVehicle = vehicle("ABC-1234", VehicleType.PRIVATE_CAR);

        assertThatThrownBy(() -> validator.run(newVehicle))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.vehicle.duplicated");
    }

    @Test
    void passes_when_license_plate_is_unique() {
        var repo = mock(VehicleRepository.class);
        when(repo.findByLicensePlate(any())).thenReturn(Optional.empty());

        var validator = new LicensePlateValidator(repo);
        var newVehicle = vehicle("XYZ-9999", VehicleType.COMPANY_CAR);

        assertThatCode(() -> validator.run(newVehicle)).doesNotThrowAnyException();
    }

    private Vehicle vehicle(String licensePlate, VehicleType type) {
        var v = new Vehicle();
        v.setLicensePlate(licensePlate);
        v.setVehicleType(type);
        return v;
    }
}
