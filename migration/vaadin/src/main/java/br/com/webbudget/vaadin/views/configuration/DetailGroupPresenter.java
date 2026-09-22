package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.domain.repositories.configuration.GroupRepository;
import br.com.webbudget.domain.services.UserAccountService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class DetailGroupPresenter {

    private final GroupRepository groupRepository;
    private final UserAccountService userAccountService;

    public Optional<Group> findById(long id) {
        return groupRepository.findById(id);
    }

    public void delete(Group group) {
        userAccountService.delete(group);
    }

    public List<Group> findAllActive() {
        return groupRepository.findAllActive();
    }
}
