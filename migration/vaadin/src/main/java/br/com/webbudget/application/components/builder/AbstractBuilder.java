package br.com.webbudget.application.components.builder;

import br.com.webbudget.domain.entities.PersistentEntity;

public abstract class AbstractBuilder<T extends PersistentEntity> {

    protected T instance;

    abstract T build();
}
