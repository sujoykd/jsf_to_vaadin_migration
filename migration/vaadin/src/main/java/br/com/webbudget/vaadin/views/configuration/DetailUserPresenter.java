package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.domain.entities.configuration.User;
import br.com.webbudget.domain.repositories.configuration.GroupRepository;
import br.com.webbudget.domain.repositories.configuration.UserRepository;
import br.com.webbudget.domain.services.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailUserPresenter {

    private final UserRepository userRepository;
    private final GroupRepository groupRepository;
    private final UserAccountService userAccountService;

    public Optional<User> findById(long id) {
        return userRepository.findById(id);
    }

    public void delete(User user) {
        userAccountService.delete(user);
    }

    public List<Group> findAllGroups() {
        return groupRepository.findAllActive();
    }
}
