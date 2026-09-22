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

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.logics.registration.costcenter.CostCenterSavingLogic;
import br.com.webbudget.domain.logics.registration.costcenter.CostCenterUpdatingLogic;
import br.com.webbudget.domain.repositories.registration.CostCenterRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * The service responsible for the business operations with {@link CostCenter}
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 1.0.0, 28/03/2014
 */
@Service
@RequiredArgsConstructor
public class CostCenterService {

    private final CostCenterRepository costCenterRepository;
    private final List<CostCenterSavingLogic> savingBusinessLogics;
    private final List<CostCenterUpdatingLogic> updatingBusinessLogics;

    /**
     * Use this method to persist a {@link CostCenter}
     *
     * @param costCenter the {@link CostCenter} to be persisted
     */
    @Transactional
    public void save(CostCenter costCenter) {
        this.savingBusinessLogics.forEach(logic -> logic.run(costCenter));
        this.costCenterRepository.save(costCenter);
    }

    /**
     * Use this method to update a persisted {@link CostCenter}
     *
     * @param costCenter the {@link CostCenter} to be updated
     * @return the updated {@link CostCenter}
     */
    @Transactional
    public CostCenter update(CostCenter costCenter) {
        this.updatingBusinessLogics.forEach(logic -> logic.run(costCenter));
        return this.costCenterRepository.saveAndFlushAndRefresh(costCenter);
    }

    /**
     * Use this method to delete a persisted {@link CostCenter}
     *
     * @param costCenter the {@link CostCenter} to be deleted
     */
    @Transactional
    public void delete(CostCenter costCenter) {
        this.costCenterRepository.attachAndRemove(costCenter);
    }
}
