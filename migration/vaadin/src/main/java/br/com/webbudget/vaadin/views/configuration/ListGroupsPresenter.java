package br.com.webbudget.vaadin.views.configuration;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.configuration.Group;
import br.com.webbudget.domain.repositories.configuration.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope("prototype")
@RequiredArgsConstructor
public class ListGroupsPresenter {

    private final GroupRepository groupRepository;

    public Page<Group> findAll(String filter, Boolean active, int offset, int limit) {
        return groupRepository.findAllBy(filter, active, offset, limit);
    }

    public int count(String filter, Boolean active) {
        return (int) groupRepository.count(groupRepository.buildSpecification(filter, active));
    }
}
