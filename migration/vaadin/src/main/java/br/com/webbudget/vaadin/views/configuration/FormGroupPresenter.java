package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.domain.entities.configuration.Authorization;
import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.domain.entities.configuration.Permissions;
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
public class FormGroupPresenter {

    private final GroupRepository groupRepository;
    private final UserAccountService userAccountService;
    private final Permissions permissions;

    public Optional<Group> findById(long id) {
        return groupRepository.findById(id);
    }

    public List<Group> findAllGroups() {
        return groupRepository.findAllActive();
    }

    public List<Authorization> loadAllAuthorizations() {
        return permissions.toAuthorizationList();
    }

    public void save(Group group, List<Authorization> authorizations) {
        userAccountService.save(group, authorizations);
    }

    public void update(Group group, List<Authorization> authorizations) {
        userAccountService.update(group, authorizations);
    }
}
