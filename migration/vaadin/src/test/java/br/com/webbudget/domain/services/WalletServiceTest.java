package br.com.webbudget.domain.services;

import br.com.webbudget.domain.entities.registration.Wallet;
import br.com.webbudget.domain.entities.registration.WalletType;
import br.com.webbudget.domain.events.WalletBalanceUpdatedEvent;
import br.com.webbudget.domain.logics.registration.wallet.WalletSavingLogic;
import br.com.webbudget.domain.logics.registration.wallet.WalletUpdatingLogic;
import br.com.webbudget.domain.repositories.registration.WalletBalanceRepository;
import br.com.webbudget.domain.repositories.registration.WalletRepository;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

class WalletServiceTest {

    @Test
    void save_runs_all_saving_logics_and_persists() {
        var wallet = wallet("Cash", null, WalletType.PERSONAL);
        var repo = mock(WalletRepository.class);
        when(repo.save(wallet)).thenReturn(wallet);

        var balanceRepo = mock(WalletBalanceRepository.class);
        var savingLogic = mock(WalletSavingLogic.class);
        var service = new WalletService(repo, balanceRepo, List.of(savingLogic), List.of(),
                mock(ApplicationEventPublisher.class));

        service.save(wallet);

        verify(savingLogic).run(wallet);
        // save(wallet) is called twice: once for the entity, once inside updateWalletBalance
        verify(repo, times(2)).save(wallet);
    }

    @Test
    void update_runs_all_updating_logics_and_persists() {
        var wallet = wallet("Cash", null, WalletType.PERSONAL);
        var repo = mock(WalletRepository.class);
        when(repo.save(wallet)).thenReturn(wallet);

        var updatingLogic = mock(WalletUpdatingLogic.class);
        var service = new WalletService(repo, mock(WalletBalanceRepository.class), List.of(),
                List.of(updatingLogic), mock(ApplicationEventPublisher.class));

        var result = service.update(wallet);

        verify(updatingLogic).run(wallet);
        verify(repo).save(wallet);
        assertThat(result).isSameAs(wallet);
    }

    @Test
    void delete_removes_balances_then_wallet() {
        var wallet = wallet("Cash", null, WalletType.PERSONAL);
        setId(wallet, 42L);

        var repo = mock(WalletRepository.class);
        var balanceRepo = mock(WalletBalanceRepository.class);
        when(balanceRepo.findByWallet_id(42L)).thenReturn(List.of());

        var service = new WalletService(repo, balanceRepo, List.of(), List.of(),
                mock(ApplicationEventPublisher.class));

        service.delete(wallet);

        verify(balanceRepo).findByWallet_id(42L);
        verify(repo).attachAndRemove(wallet);
    }

    @Test
    void adjust_balance_publishes_wallet_balance_updated_event() {
        var wallet = wallet("Cash", null, WalletType.PERSONAL);
        var publisher = mock(ApplicationEventPublisher.class);

        var service = new WalletService(mock(WalletRepository.class), mock(WalletBalanceRepository.class),
                List.of(), List.of(), publisher);

        service.adjustBalance(wallet, BigDecimal.TEN, "Initial balance");

        var captor = ArgumentCaptor.forClass(WalletBalanceUpdatedEvent.class);
        verify(publisher).publishEvent(captor.capture());
        assertThat(captor.getValue().walletBalance().getTransactionValue()).isEqualByComparingTo(BigDecimal.TEN);
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
