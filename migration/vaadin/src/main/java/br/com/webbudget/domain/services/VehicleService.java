/*
 * Copyright (C) 2016 Arthur Gregorio, AG.Software
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.Vehicle;
import br.com.webbudget.domain.logics.registration.vehicle.VehicleSavingLogic;
import br.com.webbudget.domain.repositories.registration.VehicleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The service responsible for the business operations with {@link Vehicle}
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 2.3.0, 09/05/2016
 */
@Service
@RequiredArgsConstructor
public class VehicleService {

    private final VehicleRepository vehicleRepository;
    private final List<VehicleSavingLogic> savingBusinessLogics;

    /**
     * Use this method to persist a {@link Vehicle}
     *
     * @param vehicle the {@link Vehicle} to be persisted
     */
    @Transactional
    public void save(Vehicle vehicle) {
        this.savingBusinessLogics.forEach(logic -> logic.run(vehicle));
        this.vehicleRepository.save(vehicle);
    }

    /**
     * Use this method to update a persisted {@link Vehicle}
     *
     * @param vehicle the {@link Vehicle} to be updated
     * @return the updated {@link Vehicle}
     */
    @Transactional
    public Vehicle update(Vehicle vehicle) {
        this.savingBusinessLogics.forEach(logic -> logic.run(vehicle));
        return this.vehicleRepository.saveAndFlushAndRefresh(vehicle);
    }

    /**
     * Use this method to delete a persisted {@link Vehicle}
     *
     * @param vehicle the {@link Vehicle} to be deleted
     */
    @Transactional
    public void delete(Vehicle vehicle) {
        this.vehicleRepository.attachAndRemove(vehicle);
    }
}
