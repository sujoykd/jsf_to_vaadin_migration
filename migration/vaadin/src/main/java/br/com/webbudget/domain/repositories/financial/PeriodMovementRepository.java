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

import br.com.webbudget.application.components.ui.filter.PeriodMovementFilter;
import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.Apportionment;
import br.com.webbudget.domain.entities.financial.Apportionment_;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.financial.PeriodMovement_;
import br.com.webbudget.domain.entities.registration.CostCenter;
import br.com.webbudget.domain.entities.registration.CostCenter_;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.FinancialPeriod_;
import br.com.webbudget.domain.entities.registration.MovementClass;
import br.com.webbudget.domain.entities.registration.MovementClass_;
import br.com.webbudget.domain.repositories.DefaultRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;


/**
 * The {@link PeriodMovement} repository
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 3.0.0, 04/12/2018
 */

public interface PeriodMovementRepository extends DefaultRepository<PeriodMovement> {

    /**
     * {@inheritDoc}
     *
     * @param id
     * @return
     */
    @Override
    @EntityGraph(value = "Movement.full")
    Optional<PeriodMovement> findById(Long id);

    /**
     * Find a {@link PeriodMovement} by the code
     *
     * @param movementCode used as a filter
     * @return an {@link Optional} of the {@link PeriodMovement}
     */
    @EntityGraph(value = "Movement.full")
    Optional<PeriodMovement> findByCode(String movementCode);

    /**
     * List all {@link PeriodMovement} by the {@link FinancialPeriod}
     *
     * @param period to be used as a filter
     * @return the {@link List} of {@link PeriodMovement} found
     */
    List<PeriodMovement> findByFinancialPeriod(FinancialPeriod period);

    /**
     * Calculate the total of paid or received {@link PeriodMovement} on a list of {@link FinancialPeriod}
     *
     * @return the total value of paid and received {@link PeriodMovement}
     */
    @Query("SELECT COALESCE(SUM(pm.paidValue), 0) " +
            "FROM PeriodMovement mv " +
            "JOIN mv.payment pm " +
            "WHERE pm.paymentMethod <> 'CREDIT_CARD' " +
            "AND mv.periodMovementState <> 'OPEN'")
    BigDecimal calculateTotalPaidAndReceived();

    /**
     * Same as {@link #calculateTotalPaidAndReceived()} but filtering by {@link FinancialPeriod}
     *
     * @param periods the list of {@link FinancialPeriod} to search for
     * @return the total value of paid and received {@link PeriodMovement}
     */
    @Query("SELECT COALESCE(SUM(pm.paidValue), 0) " +
            "FROM PeriodMovement mv " +
            "JOIN mv.financialPeriod fp " +
            "JOIN mv.payment pm " +
            "WHERE fp.id IN (?1) " +
            "AND pm.paymentMethod <> 'CREDIT_CARD' " +
            "AND mv.periodMovementState <> 'OPEN'")
    BigDecimal calculateTotalPaidAndReceived(List<Long> periods);

    /**
     * Calculate the total of open {@link PeriodMovement} on a list of {@link FinancialPeriod}
     *
     * @return the total value of open {@link PeriodMovement}
     */
    @Query("SELECT COALESCE(SUM(mv.value), 0) " +
            "FROM PeriodMovement mv " +
            "WHERE mv.periodMovementState = 'OPEN'")
    BigDecimal calculateTotalOpen();

    /**
     * Same as {@link #calculateTotalOpen()} but filtering by {@link FinancialPeriod}
     *
     * @param periods the list of {@link FinancialPeriod} to search for
     * @return the total value of open {@link PeriodMovement}
     */
    @Query("SELECT COALESCE(SUM(mv.value), 0) " +
            "FROM PeriodMovement mv " +
            "JOIN mv.financialPeriod fp " +
            "WHERE fp.id IN (?1) " +
            "AND mv.periodMovementState = 'OPEN'")
    BigDecimal calculateTotalOpen(List<Long> periods);

    /**
     * Calculate the total of expenses on a list of {@link FinancialPeriod}
     *
     * @return the total value of expenses
     */
    @Query("SELECT COALESCE(SUM(pm.paidValue), 0) " +
            "FROM PeriodMovement mv " +
            "JOIN mv.payment pm " +
            "JOIN mv.apportionments ap " +
            "JOIN ap.movementClass mc " +
            "WHERE pm.paymentMethod <> 'CREDIT_CARD' " +
            "AND mc.movementClassType = 'EXPENSE' " +
            "AND mv.periodMovementState <> 'OPEN'")
    BigDecimal calculateTotalExpenses();

    /**
     * Same as {@link #calculateTotalExpenses()} but filtering by {@link FinancialPeriod}
     *
     * @param periods the list of {@link FinancialPeriod} to search for
     * @return the total value of expenses
     */
    @Query("SELECT COALESCE(SUM(pm.paidValue), 0) " +
            "FROM PeriodMovement mv " +
            "JOIN mv.financialPeriod fp " +
            "JOIN mv.payment pm " +
            "JOIN mv.apportionments ap " +
            "JOIN ap.movementClass mc " +
            "WHERE fp.id IN (?1) " +
            "AND pm.paymentMethod <> 'CREDIT_CARD' " +
            "AND mc.movementClassType = 'EXPENSE' " +
            "AND mv.periodMovementState <> 'OPEN'")
    BigDecimal calculateTotalExpenses(List<Long> periods);

    /**
     * Calculate the total of revenues on a list of {@link FinancialPeriod}
     *
     * @return the total value of revenues
     */
    @Query("SELECT COALESCE(SUM(pm.paidValue), 0) " +
            "FROM PeriodMovement mv " +
            "JOIN mv.payment pm " +
            "JOIN mv.apportionments ap " +
            "JOIN ap.movementClass mc " +
            "WHERE mc.movementClassType = 'REVENUE' " +
            "AND mv.periodMovementState <> 'OPEN'")
    BigDecimal calculateTotalRevenues();

    /**
     * Same as {@link #calculateTotalRevenues()} but filtering by {@link FinancialPeriod}
     *
     * @param periods the list of {@link FinancialPeriod} to search for
     * @return the total value of revenues
     */
    @Query("SELECT COALESCE(SUM(pm.paidValue), 0) " +
            "FROM PeriodMovement mv " +
            "JOIN mv.financialPeriod fp " +
            "JOIN mv.payment pm " +
            "JOIN mv.apportionments ap " +
            "JOIN ap.movementClass mc " +
            "WHERE fp.id IN (?1) " +
            "AND mc.movementClassType = 'REVENUE' " +
            "AND mv.periodMovementState <> 'OPEN'")
    BigDecimal calculateTotalRevenues(List<Long> periods);

    /**
     * Use this method to find all {@link PeriodMovement} using the lazy load strategy
     *
     * @param filter the {@link PeriodMovementFilter}
     * @param start starting row
     * @param pageSize page size
     * @return the {@link Page} filled with the {@link PeriodMovement} found
     */
    default Page<PeriodMovement> findAllBy(PeriodMovementFilter filter, int start, int pageSize) {
        final Specification<PeriodMovement> spec = buildSpecification(filter);
        final int page = pageSize > 0 ? start / pageSize : 0;
        final org.springframework.data.domain.Page<PeriodMovement> result = findAll(spec,
                PageRequest.of(page, Math.max(1, pageSize), Sort.by(Sort.Direction.DESC, "createdOn")));
        return Page.of(result.getContent(), (int) result.getTotalElements());
    }

    /**
     * This method is used to build the {@link Specification} used to find the {@link PeriodMovement}
     *
     * @param filter the {@link PeriodMovementFilter}
     * @return the {@link Specification} with the restrictions to find the {@link PeriodMovement}
     */
    default Specification<PeriodMovement> buildSpecification(PeriodMovementFilter filter) {

        Specification<PeriodMovement> spec = Specification.allOf();

        // set the movement state filter if present
        if (filter.getPeriodMovementState() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get(PeriodMovement_.periodMovementState), filter.getPeriodMovementState()));
        }

        // the movement type filter if present
        if (filter.getPeriodMovementType() != null) {
            spec = spec.and((root, query, cb) ->
                cb.equal(root.get(PeriodMovement_.periodMovementType), filter.getPeriodMovementType()));
        }

        // now the OR filters, more generic
        if (filter.getValue() != null && !filter.getValue().isBlank()) {

            final String pattern = ("%" + filter.getValue() + "%").toLowerCase();

            Specification<PeriodMovement> orSpec = (root, query, cb) -> {
                jakarta.persistence.criteria.Predicate codePred =
                    cb.equal(root.get(PeriodMovement_.code), filter.getValue());
                jakarta.persistence.criteria.Predicate descPred =
                    cb.like(cb.lower(root.get(PeriodMovement_.description)), pattern);
                jakarta.persistence.criteria.Predicate identPred =
                    cb.like(cb.lower(root.get(PeriodMovement_.identification)), pattern);
                jakarta.persistence.criteria.Predicate fpPred =
                    cb.like(cb.lower(root.join(PeriodMovement_.financialPeriod, JoinType.LEFT)
                        .get(FinancialPeriod_.identification)), pattern);

                // if the value can be parsed as BigDecimal, also filter on value
                Optional<BigDecimal> bdValue = filter.valueToBigDecimal();
                if (bdValue.isPresent()) {
                    return cb.or(codePred, descPred, identPred, fpPred,
                        cb.equal(root.get(PeriodMovement_.value), bdValue.get()));
                }
                return cb.or(codePred, descPred, identPred, fpPred);
            };

            spec = spec.and(orSpec);
        }

        // put the selected cost center as a filter
        if (filter.getCostCenter() != null) {
            final Long costCenterId = filter.getCostCenter().getId();
            spec = spec.and((root, query, cb) -> {
                Join<PeriodMovement, Apportionment> apJoin = root.join(PeriodMovement_.apportionments, JoinType.INNER);
                Join<Apportionment, CostCenter> ccJoin = apJoin.join(Apportionment_.costCenter, JoinType.INNER);
                return cb.equal(ccJoin.get(CostCenter_.id), costCenterId);
            });

            // if we have a cost center then check if we have a movement class to filter too
            if (filter.getMovementClass() != null) {
                final Long movementClassId = filter.getMovementClass().getId();
                spec = spec.and((root, query, cb) -> {
                    Join<PeriodMovement, Apportionment> apJoin = root.join(PeriodMovement_.apportionments, JoinType.INNER);
                    Join<Apportionment, MovementClass> mcJoin = apJoin.join(Apportionment_.movementClass, JoinType.INNER);
                    return cb.equal(mcJoin.get(MovementClass_.id), movementClassId);
                });
            }
        }

        // put the selected financial periods as a filter
        if (filter.getSelectedFinancialPeriods() != null && !filter.getSelectedFinancialPeriods().isEmpty()) {
            final List<String> identifications = Arrays.asList(filter.getSelectedFinancialPeriodsAsStringArray());
            spec = spec.and((root, query, cb) -> {
                Join<PeriodMovement, FinancialPeriod> fpJoin = root.join(PeriodMovement_.financialPeriod, JoinType.INNER);
                return cb.lower(fpJoin.get(FinancialPeriod_.identification)).in(identifications);
            });
        }

        return spec;
    }
}
