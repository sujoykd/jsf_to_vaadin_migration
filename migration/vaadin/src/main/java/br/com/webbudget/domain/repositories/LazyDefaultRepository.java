package br.com.webbudget.domain.repositories;

import br.com.webbudget.application.components.ui.table.Page;
import br.com.webbudget.domain.entities.PersistentEntity;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.repository.NoRepositoryBean;

@NoRepositoryBean
public interface LazyDefaultRepository<T extends PersistentEntity> extends DefaultRepository<T> {

    default Page<T> findAllBy(String filter, Boolean active, int start, int pageSize) {
        final Specification<T> spec = buildSpecification(filter, active);
        final int page = pageSize > 0 ? start / pageSize : 0;
        final org.springframework.data.domain.Page<T> result =
                findAll(spec, PageRequest.of(page, Math.max(1, pageSize), Sort.by(Sort.Direction.ASC, "id")));
        return Page.of(result.getContent(), (int) result.getTotalElements());
    }
}
