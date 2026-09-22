/*
 * Copyright (C) 2018 Arthur Gregorio, AG.Software
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

import br.com.webbudget.application.components.ui.filter.TransferenceFilter;
import br.com.webbudget.domain.entities.financial.Transference;
import br.com.webbudget.domain.entities.financial.Transference_;
import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.entities.registration.Wallet_;
import br.com.webbudget.domain.repositories.DefaultRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

/**
 * The {@link Transference} repository
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 3.0.0, 03/10/2018
 */

public interface TransferenceRepository extends DefaultRepository<Transference> {

    /**
     * Find all transference using a given filter
     *
     * @param filter used to search for {@link Transference}
     * @return a list of {@link Transference} found
     */
    default List<Transference> findByFilter(TransferenceFilter filter) {

        Specification<Transference> spec = Specification.allOf();

        if (filter.getOriginWallet() != null) {
            final Long originId = filter.getOriginWallet().getId();
            spec = spec.and((root, query, cb) -> {
                Join<Transference, Wallet> join = root.join(Transference_.origin, JoinType.INNER);
                return cb.equal(join.get(Wallet_.id), originId);
            });
        }

        if (filter.getDestinationWallet() != null) {
            final Long destinationId = filter.getDestinationWallet().getId();
            spec = spec.and((root, query, cb) -> {
                Join<Transference, Wallet> join = root.join(Transference_.destination, JoinType.INNER);
                return cb.equal(join.get(Wallet_.id), destinationId);
            });
        }

        if (filter.getOperationDate() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get(Transference_.transferDate), filter.getOperationDate()));
        }

        return findAll(spec);
    }
}
