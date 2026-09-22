package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.entities.registration.VehicleType;
import br.com.webbudget.domain.logics.registration.vehicle.VehicleSavingLogic;
import br.com.webbudget.domain.repositories.registration.VehicleRepository;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class VehicleServiceTest {

    @Test
    void save_runs_saving_logics_and_persists() {
        var vehicle = vehicle("My Car", VehicleType.PRIVATE_CAR);
        var repo = mock(VehicleRepository.class);
        var savingLogic = mock(VehicleSavingLogic.class);

        var service = new VehicleService(repo, List.of(savingLogic));
        service.save(vehicle);

        verify(savingLogic).run(vehicle);
        verify(repo).save(vehicle);
    }

    @Test
    void update_runs_saving_logics_and_refreshes() {
        var vehicle = vehicle("My Car", VehicleType.PRIVATE_CAR);
        var repo = mock(VehicleRepository.class);
        when(repo.saveAndFlushAndRefresh(vehicle)).thenReturn(vehicle);
        var savingLogic = mock(VehicleSavingLogic.class);

        var service = new VehicleService(repo, List.of(savingLogic));
        var result = service.update(vehicle);

        verify(savingLogic).run(vehicle);
        verify(repo).saveAndFlushAndRefresh(vehicle);
        assertThat(result).isSameAs(vehicle);
    }

    @Test
    void delete_removes_vehicle() {
        var vehicle = vehicle("My Car", VehicleType.PRIVATE_CAR);
        var repo = mock(VehicleRepository.class);

        var service = new VehicleService(repo, List.of());
        service.delete(vehicle);

        verify(repo).attachAndRemove(vehicle);
    }

    @Test
    void save_with_no_logics_still_persists() {
        var vehicle = vehicle("Motorbike", VehicleType.BACKUP_CAR);
        var repo = mock(VehicleRepository.class);

        var service = new VehicleService(repo, List.of());
        service.save(vehicle);

        verify(repo).save(vehicle);
    }

    private Vehicle vehicle(String identification, VehicleType type) {
        var v = new Vehicle();
        v.setIdentification(identification);
        v.setVehicleType(type);
        v.setLicensePlate("ABC-1234");
        v.setBrand("Toyota");
        v.setModel("Corolla");
        return v;
    }
}
