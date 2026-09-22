package br.com.webbudget.domain.events;

import br.com.webbudget.domain.entities.registration.Card;

public record CardCreatedEvent(Card card) {}
