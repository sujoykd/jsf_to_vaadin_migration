package br.com.webbudget.infrastructure.initializer;

import org.springframework.transaction.annotation.Transactional;

public abstract class TransactionalInitializationTask implements InitializationTask {

    @Override
    @Transactional
    public void run() {
        this.runInsideTransaction();
    }

    public abstract void runInsideTransaction();
}
