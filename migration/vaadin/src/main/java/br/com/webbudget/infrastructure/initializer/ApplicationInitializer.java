package br.com.webbudget.infrastructure.initializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.stereotype.Component;

import java.util.Comparator;
import java.util.List;

@Component
public class ApplicationInitializer implements ApplicationListener<ApplicationReadyEvent> {

    private final Logger logger = LoggerFactory.getLogger(ApplicationInitializer.class);

    private final List<InitializationTask> tasks;

    public ApplicationInitializer(List<InitializationTask> tasks) {
        this.tasks = tasks;
    }

    @Override
    public void onApplicationEvent(ApplicationReadyEvent event) {
        logger.info("webBudget is now preparing the initialization tasks...");
        this.tasks.stream()
                .sorted(Comparator.comparingInt(InitializationTask::getPriority))
                .forEach(InitializationTask::run);
        logger.info("{} initialization tasks performed", this.tasks.size());
    }
}
