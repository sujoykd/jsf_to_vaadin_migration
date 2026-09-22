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

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.financial.CreditCardInvoice;
import br.com.webbudget.domain.entities.financial.CreditCardInvoice_;
import br.com.webbudget.domain.entities.financial.InvoiceState;
import br.com.webbudget.domain.entities.financial.PeriodMovement;
import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.Card_;
import br.com.webbudget.domain.entities.registration.FinancialPeriod;
import br.com.webbudget.domain.entities.registration.FinancialPeriod_;
import br.com.webbudget.domain.repositories.DefaultRepository;
import jakarta.persistence.criteria.JoinType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * The {@link CreditCardInvoice} repository
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 3.0.0, 10/03/2019
 */

public interface CreditCardInvoiceRepository extends DefaultRepository<CreditCardInvoice> {

    /**
     * Find a {@link CreditCardInvoice} by the {@link PeriodMovement} used to pay the invoice
     *
     * @param periodMovement linked with this invoice
     * @return an {@link Optional} of the {@link CreditCardInvoice}
     */
    Optional<CreditCardInvoice> findByPeriodMovement(PeriodMovement periodMovement);

    /**
     * Find a {@link CreditCardInvoice} by the given {@link Card} and {@link FinancialPeriod}
     *
     * @param card to use as filter
     * @param financialPeriod to use as filter
     * @return an {@link Optional} of the {@link CreditCardInvoice}
     */
    Optional<CreditCardInvoice> findByCardAndFinancialPeriod(Card card, FinancialPeriod financialPeriod);


    /**
     * Get a list of {@link CreditCardInvoice} for a given {@link Card}
     *
     * @param card to use as filter
     * @return the {@link List} of invoices
     */
    List<CreditCardInvoice> findByCard(Card card);

    /**
     * Find all {@link CreditCardInvoice} for a given {@link FinancialPeriod}
     *
     * @param financialPeriod to be used as filter
     * @return a {@link List} of the {@link CreditCardInvoice} found
     */
    List<CreditCardInvoice> findByFinancialPeriod(FinancialPeriod financialPeriod);

    /**
     * Lazy filter method to search for {@link CreditCardInvoice}
     *
     * @param filter value
     * @param invoiceState to filter
     * @param start of the page
     * @param pageSize for limiting the items
     * @return a {@link Page} with the {@link CreditCardInvoice} found
     */
    default Page<CreditCardInvoice> findAllBy(String filter, InvoiceState invoiceState, int start, int pageSize) {
        final Specification<CreditCardInvoice> spec = buildSpecification(filter, invoiceState);
        final int page = pageSize > 0 ? start / pageSize : 0;
        final org.springframework.data.domain.Page<CreditCardInvoice> result = findAll(spec,
                PageRequest.of(page, Math.max(1, pageSize), Sort.by(Sort.Direction.DESC, "createdOn")));
        return Page.of(result.getContent(), (int) result.getTotalElements());
    }

    /**
     * Build the {@link Specification} to search for {@link CreditCardInvoice}
     *
     * @param filter value
     * @param invoiceState to filter
     * @return the {@link Specification} ready to search
     */
    default Specification<CreditCardInvoice> buildSpecification(String filter, InvoiceState invoiceState) {

        Specification<CreditCardInvoice> spec = Specification.allOf();

        if (invoiceState != null) {
            spec = spec.and((root, query, cb) -> cb.equal(root.get(CreditCardInvoice_.invoiceState), invoiceState));
        }

        if (filter != null && !filter.isBlank()) {
            final String pattern = ("%" + filter + "%").toLowerCase();
            spec = spec.and((root, query, cb) -> cb.or(
                cb.like(cb.lower(root.join(CreditCardInvoice_.card, JoinType.LEFT).get(Card_.name)), pattern),
                cb.like(cb.lower(root.join(CreditCardInvoice_.financialPeriod, JoinType.LEFT).get(FinancialPeriod_.identification)), pattern)
            ));
        }

        return spec;
    }
}
