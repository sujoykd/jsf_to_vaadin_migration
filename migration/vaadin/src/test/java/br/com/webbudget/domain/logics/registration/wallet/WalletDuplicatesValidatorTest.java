package br.com.webbudget.domain.logics.registration.wallet;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.entities.registration.WalletType;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.*;

class WalletDuplicatesValidatorTest {

    @Test
    void save_throws_when_wallet_with_same_name_bank_type_already_exists() {
        var existing = wallet("Itaú", "001", WalletType.BANK_ACCOUNT);
        setId(existing, 1L);

        var repo = mock(WalletRepository.class);
        when(repo.findByNameAndBankAndWalletType("Itaú", "001", WalletType.BANK_ACCOUNT))
                .thenReturn(Optional.of(existing));

        var newWallet = wallet("Itaú", "001", WalletType.BANK_ACCOUNT);
        var validator = new WalletDuplicatesValidator(repo);

        assertThatThrownBy(() -> validator.run(newWallet))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.wallet.duplicated");
    }

    @Test
    void save_passes_when_no_duplicate_exists() {
        var repo = mock(WalletRepository.class);
        when(repo.findByNameAndBankAndWalletType(any(), any(), any()))
                .thenReturn(Optional.empty());

        var newWallet = wallet("Nubank", null, WalletType.PERSONAL);
        var validator = new WalletDuplicatesValidator(repo);

        assertThatCode(() -> validator.run(newWallet)).doesNotThrowAnyException();
    }

    @Test
    void update_throws_when_different_wallet_has_same_name_bank_type() {
        var conflicting = wallet("Itaú", "001", WalletType.BANK_ACCOUNT);
        setId(conflicting, 2L);

        var repo = mock(WalletRepository.class);
        when(repo.findByNameAndBankAndWalletType("Itaú", "001", WalletType.BANK_ACCOUNT))
                .thenReturn(Optional.of(conflicting));

        var toUpdate = wallet("Itaú", "001", WalletType.BANK_ACCOUNT);
        setId(toUpdate, 99L);
        var validator = new WalletDuplicatesValidator(repo);

        assertThatThrownBy(() -> validator.run(toUpdate))
                .isInstanceOf(BusinessLogicException.class)
                .hasMessage("error.wallet.duplicated");
    }

    @Test
    void update_passes_when_found_wallet_is_the_same_entity() {
        var same = wallet("Itaú", "001", WalletType.BANK_ACCOUNT);
        setId(same, 10L);

        var repo = mock(WalletRepository.class);
        when(repo.findByNameAndBankAndWalletType("Itaú", "001", WalletType.BANK_ACCOUNT))
                .thenReturn(Optional.of(same));

        var validator = new WalletDuplicatesValidator(repo);
        // same entity → no conflict
        assertThatCode(() -> validator.run(same)).doesNotThrowAnyException();
    }

    private Wallet wallet(String name, String bank, WalletType type) {
        var w = new Wallet();
        w.setName(name);
        w.setBank(bank);
        w.setWalletType(type);
        w.setActualBalance(BigDecimal.ZERO);
        return w;
    }

    private void setId(Wallet wallet, long id) {
        try {
            var field = wallet.getClass().getSuperclass().getDeclaredField("id");
            field.setAccessible(true);
            field.set(wallet, id);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
