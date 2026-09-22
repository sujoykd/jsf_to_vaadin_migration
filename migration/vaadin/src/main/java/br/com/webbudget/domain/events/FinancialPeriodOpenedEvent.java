package br.com.webbudget.domain.events;

import br.com.webbudget.domain.entities.registration.FinancialPeriod;

public record FinancialPeriodOpenedEvent(FinancialPeriod financialPeriod) {}
