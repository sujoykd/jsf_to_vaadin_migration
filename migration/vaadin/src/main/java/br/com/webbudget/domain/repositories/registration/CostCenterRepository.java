/*
 * Copyright (C) 2013 Arthur Gregorio, AG.Software
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
package br.com.webbudget.domain.repositories.registration;

import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.CostCenter_;
import br.com.webbudget.domain.repositories.LazyDefaultRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

/**
 * The {@link CostCenter} repository
 *
 * @author Arthur Gregorio
 *
 * @version 3.0.0
 * @since 1.0.0, 04/03/2013
 */

public interface CostCenterRepository extends LazyDefaultRepository<CostCenter> {

    /**
     * Find a {@link CostCenter} by the name
     *
     * @param name the of the {@link CostCenter} to find
     * @return an {@link Optional} of the {@link CostCenter}
     */
    Optional<CostCenter> findByName(String name);

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    default Specification<CostCenter> getEntityStateSpec(Boolean active) {
        return (root, query, cb) -> cb.equal(root.get(CostCenter_.active), active);
    }

    /**
     * {@inheritDoc}
     *
     * @param filter
     * @return
     */
    @Override
    default Specification<CostCenter> getFilterSpecification(String filter) {
        final String pattern = likeAny(filter).toLowerCase();
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get(CostCenter_.description)), pattern),
            cb.like(cb.lower(root.get(CostCenter_.name)), pattern)
        );
    }
}
