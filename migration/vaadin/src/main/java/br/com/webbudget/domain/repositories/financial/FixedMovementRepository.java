/*
 * Copyright (C) 2019 Arthur Gregorio, AG.Software
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
package br.com.webbudget.domain.repositories.financial;

import br.com.webbudget.application.components.ui.filter.FixedMovementFilter;
import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.FixedMovement;
import br.com.webbudget.domain.entities.financial.FixedMovementState;
import br.com.webbudget.domain.entities.financial.FixedMovement_;
import br.com.webbudget.domain.repositories.DefaultRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.List;
import java.util.Optional;


/**
 * The {@link FixedMovement} repository
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 3.0.0, 21/03/2019
 */

public interface FixedMovementRepository extends DefaultRepository<FixedMovement> {

    /**
     * Find a {@link FixedMovement} by the ID
     *
     * @param id to search for
     * @return an {@link Optional} of the {@link FixedMovement}
     */
    @Override
    @EntityGraph(value = "Movement.full")
    Optional<FixedMovement> findById(Long id);

    /**
     * Find all {@link FixedMovement} by the auto launch flag
     *
     * @param autoLaunch true or false
     * @param state the state that we want to get
     * @return a {@link List} of the {@link FixedMovement} found
     */
    List<FixedMovement> findByAutoLaunchAndFixedMovementState(boolean autoLaunch, FixedMovementState state);

    /**
     * Method used to search for {@link FixedMovement} using pagination
     *
     * @param filter to be applied
     * @param start starting point
     * @param pageSize maximum size of the page
     * @return a {@link Page} with the {@link FixedMovement} found
     */
    default Page<FixedMovement> findAllBy(FixedMovementFilter filter, int start, int pageSize) {
        final Specification<FixedMovement> spec = buildSpecification(filter);
        final int page = pageSize > 0 ? start / pageSize : 0;
        final org.springframework.data.domain.Page<FixedMovement> result = findAll(spec,
                PageRequest.of(page, Math.max(1, pageSize), Sort.by(Sort.Direction.DESC, "createdOn")));
        return Page.of(result.getContent(), (int) result.getTotalElements());
    }

    /**
     * Create and apply the filters to produce a {@link Specification}
     *
     * @param filter to be used
     * @return the {@link Specification} created to search for {@link FixedMovement}
     */
    default Specification<FixedMovement> buildSpecification(FixedMovementFilter filter) {

        Specification<FixedMovement> spec = Specification.allOf();

        if (filter.getFixedMovementState() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get(FixedMovement_.fixedMovementState), filter.getFixedMovementState()));
        }

        if (filter.getValue() != null && !filter.getValue().isBlank()) {

            final String pattern = ("%" + filter.getValue() + "%").toLowerCase();

            Specification<FixedMovement> orSpec = (root, query, cb) -> {
                jakarta.persistence.criteria.Predicate identPred =
                    cb.like(cb.lower(root.get(FixedMovement_.identification)), pattern);
                jakarta.persistence.criteria.Predicate descPred =
                    cb.like(cb.lower(root.get(FixedMovement_.description)), pattern);

                // if the value can be parsed as BigDecimal, also filter on value
                Optional<java.math.BigDecimal> bdValue = filter.valueToBigDecimal();
                if (bdValue.isPresent()) {
                    return cb.or(identPred, descPred,
                        cb.equal(root.get(FixedMovement_.value), bdValue.get()));
                }
                return cb.or(identPred, descPred);
            };

            spec = spec.and(orSpec);
        }

        return spec;
    }
}
