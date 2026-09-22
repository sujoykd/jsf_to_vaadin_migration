package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.configuration.User;
import br.com.webbudget.domain.repositories.configuration.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListUsersPresenter {

    private final UserRepository userRepository;

    public Page<User> findAll(String filter, Boolean active, int offset, int limit) {
        return userRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) userRepository.count(userRepository.buildSpecification(filter, active));
    }
}
