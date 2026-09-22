/*
 * Copyright (C) 2014 Arthur Gregorio, AG.Software
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

import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.logics.registration.movementclass.MovementClassSavingLogic;
import br.com.webbudget.domain.logics.registration.movementclass.MovementClassUpdatingLogic;
import br.com.webbudget.domain.repositories.registration.MovementClassRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The service responsible for the business operations with {@link MovementClass}
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 1.0.0, 04/03/2014
 */
@Service
@RequiredArgsConstructor
public class MovementClassService {

    private final MovementClassRepository movementClassRepository;
    private final List<MovementClassSavingLogic> savingBusinessLogics;
    private final List<MovementClassUpdatingLogic> updatingBusinessLogics;

    /**
     * Use this method to persist a {@link MovementClass}
     *
     * @param movementClass the {@link MovementClass} to be persisted
     */
    @Transactional
    public void save(MovementClass movementClass) {
        this.savingBusinessLogics.forEach(logic -> logic.run(movementClass));
        this.movementClassRepository.save(movementClass);
    }

    /**
     * Use this method to update a persisted {@link MovementClass}
     *
     * @param movementClass the {@link MovementClass} to be updated
     * @return the updated {@link MovementClass}
     */
    @Transactional
    public MovementClass update(MovementClass movementClass) {
        this.updatingBusinessLogics.forEach(logic -> logic.run(movementClass));
        return this.movementClassRepository.saveAndFlushAndRefresh(movementClass);
    }

    /**
     * Use this method to delete a persisted {@link MovementClass}
     *
     * @param movementClass the {@link MovementClass} to be deleted
     */
    @Transactional
    public void delete(MovementClass movementClass) {
        this.movementClassRepository.attachAndRemove(movementClass);
    }
}
