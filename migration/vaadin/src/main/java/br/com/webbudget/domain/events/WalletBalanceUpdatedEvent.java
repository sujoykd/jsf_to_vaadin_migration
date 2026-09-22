package br.com.webbudget.domain.events;

import br.com.webbudget.domain.entities.financial.WalletBalance;

public record WalletBalanceUpdatedEvent(WalletBalance walletBalance) {}
