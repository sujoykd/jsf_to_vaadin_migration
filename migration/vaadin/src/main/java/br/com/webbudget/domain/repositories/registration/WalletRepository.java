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

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.entities.registration.WalletType;
import br.com.webbudget.domain.entities.registration.Wallet_;
import br.com.webbudget.domain.repositories.LazyDefaultRepository;
import org.springframework.data.jpa.domain.Specification;

import java.util.Optional;

/**
 * The {@link Wallet} repository
 *
 * @author Arthur Gregorio
 *
 * @version 2.0.0
 * @since 1.0.0, 04/03/2013
 */

public interface WalletRepository extends LazyDefaultRepository<Wallet> {

    /**
     * Use this method to search for a wallet by the name, bank and type
     *
     * @param name of the wallet
     * @param bank used with this {@link Wallet}
     * @param walletType of this {@link Wallet}
     * @return an {@link Optional} of the {@link Wallet}
     */
    Optional<Wallet> findByNameAndBankAndWalletType(String name, String bank, WalletType walletType);

    /**
     * {@inheritDoc}
     *
     * @param filter
     * @return
     */
    @Override
    default Specification<Wallet> getFilterSpecification(String filter) {
        final String pattern = likeAny(filter).toLowerCase();
        return (root, query, cb) -> cb.or(
            cb.like(cb.lower(root.get(Wallet_.name)), pattern),
            cb.like(cb.lower(root.get(Wallet_.account)), pattern),
            cb.like(cb.lower(root.get(Wallet_.agency)), pattern),
            cb.like(cb.lower(root.get(Wallet_.bank)), pattern)
        );
    }

    /**
     * {@inheritDoc}
     *
     * @return
     */
    @Override
    default Specification<Wallet> getEntityStateSpec(Boolean active) {
        return (root, query, cb) -> cb.equal(root.get(Wallet_.active), active);
    }
}
