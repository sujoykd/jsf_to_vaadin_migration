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

import br.com.webbudget.domain.entities.registration.Card;
import br.com.webbudget.domain.entities.registration.CardType;
import br.com.webbudget.domain.entities.registration.Card_;
import br.com.webbudget.domain.repositories.LazyDefaultRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

/**
 * The {@link Card} repository
 *
 * @author Arthur Gregorio
 *
 * @version 2.0.0
 * @since 1.0.0, 04/03/2013
 */

public interface CardRepository extends LazyDefaultRepository<Card> {

    /**
     * Find a {@link Card} by the number or type
     *
     * @param number the number of the card
     * @param type the type of the card defined by the {@link CardType} enum
     * @return an {@link Optional} of the card
     */
    Optional<Card> findByNumberAndCardType(String number, CardType type);

    /**
     * Find the {@link Card} with the {@link CardType} specified and status
     *
     * @param cardType the {@link CardType} to filter the {@link Card}
     * @param active which status you want to get
     * @return the {@link List} of {@link Card} found
     */
    List<Card> findByCardTypeAndActive(CardType cardType, boolean active);

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    default Specification<Card> getEntityStateSpec(Boolean active) {
        return (root, query, cb) -> cb.equal(root.get(Card_.active), active);
    }

    /**
     * {@inheritDoc}
     *
     * @param filter
     * @return
     */
    @Override
    default Specification<Card> getFilterSpecification(String filter) {
        final String pattern = likeAny(filter).toLowerCase();
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get(Card_.name)), pattern),
            cb.like(cb.lower(root.get(Card_.number)), pattern),
            cb.like(cb.lower(root.get(Card_.flag)), pattern),
            cb.like(cb.lower(root.get(Card_.owner)), pattern)
        );
    }
}
