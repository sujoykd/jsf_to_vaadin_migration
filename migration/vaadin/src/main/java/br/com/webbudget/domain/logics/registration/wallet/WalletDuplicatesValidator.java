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
package br.com.webbudget.domain.logics.registration.wallet;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.entities.registration.WalletType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * The validator to prevent duplicated {@link Wallet}
 *
 * @author Arthur Gregorio
 *
 * @version 1.0.0
 * @since 3.0.0, 28/09/2018
 */
@Component
@RequiredArgsConstructor
public class WalletDuplicatesValidator implements WalletSavingLogic, WalletUpdatingLogic {

    private final WalletRepository walletRepository;

    /**
     * {@inheritDoc}
     *
     * @param value
     */
    @Override
    public void run(Wallet value) {
        if (value.isSaved()) {
            this.validateSaved(value);
        } else {
            this.validateNotSaved(value);
        }
    }

    private void validateNotSaved(Wallet value) {
        final Optional<Wallet> found = this.find(value.getName(), value.getBank(), value.getWalletType());
        found.ifPresent(w -> {
            throw new BusinessLogicException("error.wallet.duplicated");
        });
    }

    private void validateSaved(Wallet value) {
        final Optional<Wallet> found = this.find(value.getName(), value.getBank(), value.getWalletType());
        if (found.isPresent() && !found.get().equals(value)) {
            throw new BusinessLogicException("error.wallet.duplicated");
        }
    }

    private Optional<Wallet> find(String name, String bank, WalletType walletType) {
        return this.walletRepository.findByNameAndBankAndWalletType(name, bank, walletType);
    }
}
