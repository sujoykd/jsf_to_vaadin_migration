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

import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.FinancialPeriod_;
import br.com.webbudget.domain.repositories.LazyDefaultRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * The {@link FinancialPeriod} repository
 *
 * @author Arthur Gregorio
 *
 * @version 3.0.0
 * @since 1.0.0, 04/03/2013
 */

public interface FinancialPeriodRepository extends LazyDefaultRepository<FinancialPeriod> {

    /**
     * Method used to run if the dates give are within other periods
     *
     * @param start the date to start
     * @param end the date to end
     * @return total of periods within the dates
     */
    @Query("SELECT count(*) FROM FinancialPeriod fp " +
            "WHERE (?1 BETWEEN fp.start AND fp.end) " +
            "OR (?2 BETWEEN fp.start AND fp.end)")
    Long validatePeriodDates(LocalDate start, LocalDate end);

    /**
     * List all {@link FinancialPeriod} by the closing status
     *
     * @param isClosed true for closed periods or false for open periods
     * @return a {@link List} of {@link FinancialPeriod}
     */
    List<FinancialPeriod> findByClosedOrderByIdentificationAsc(boolean isClosed);

    /**
     * Find a {@link FinancialPeriod} by the identification
     *
     * @param identification the identification of the {@link FinancialPeriod} to search
     * @return an {@link Optional} of the {@link FinancialPeriod}
     */
    Optional<FinancialPeriod> findByIdentification(String identification);

    /**
     * Same as {@link #findByIdentification(String)} but this one make a like comparison and return the resultas as a
     * {@link List}
     *
     * @param identification to query
     * @return a {@link List} with the {@link FinancialPeriod} found
     */
    List<FinancialPeriod> findByIdentificationLikeIgnoreCaseOrderByCreatedOnDesc(String identification);

    /**
     * {@inheritDoc}
     *
     * @param filter
     * @return
     */
    @Override
    default Specification<FinancialPeriod> getFilterSpecification(String filter) {
        final String pattern = likeAny(filter).toLowerCase();
        return (root, query, cb) -> cb.like(cb.lower(root.get(FinancialPeriod_.identification)), pattern);
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    default Specification<FinancialPeriod> getEntityStateSpec(Boolean active) {
        return (root, query, cb) -> cb.equal(root.get(FinancialPeriod_.closed), active);
    }
}
