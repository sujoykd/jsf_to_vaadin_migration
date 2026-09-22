package br.com.webbudget.domain.events;

import br.com.webbudget.domain.entities.financial.PeriodMovement;

public record PeriodMovementUpdatedEvent(PeriodMovement periodMovement) {}
