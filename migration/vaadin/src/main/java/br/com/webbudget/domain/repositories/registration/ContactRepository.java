/*
 * Copyright (C) 2015 Arthur Gregorio, AG.Software
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

import br.com.webbudget.domain.entities.registration.Contact;
import br.com.webbudget.domain.entities.registration.Contact_;
import br.com.webbudget.domain.repositories.LazyDefaultRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.EntityGraph;

import java.util.Optional;

/**
 * The {@link Contact} repository
 *
 * @author Arthur Gregorio
 *
 * @version 3.0.0
 * @since 1.2.0, 12/04/2015
 */

public interface ContactRepository extends LazyDefaultRepository<Contact> {

    /**
     * {@inheritDoc }
     *
     * @param id
     * @return
     */
    @Override
    @EntityGraph(value = "Contact.withTelephones")
    Optional<Contact> findById(Long id);

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    default Specification<Contact> getEntityStateSpec(Boolean active) {
        return (root, query, cb) -> cb.equal(root.get(Contact_.active), active);
    }

    /**
     * {@inheritDoc}
     *
     * @param filter
     * @return
     */
    @Override
    default Specification<Contact> getFilterSpecification(String filter) {
        final String pattern = likeAny(filter).toLowerCase();
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get(Contact_.name)), pattern),
            cb.like(cb.lower(root.get(Contact_.city)), pattern),
            cb.like(cb.lower(root.get(Contact_.email)), pattern),
            cb.like(cb.lower(root.get(Contact_.document)), pattern)
        );
    }
}
