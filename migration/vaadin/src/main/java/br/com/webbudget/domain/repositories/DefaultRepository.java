package br.com.webbudget.domain.repositories;

import br.com.webbudget.domain.entities.PersistentEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.repository.NoRepositoryBean;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;

@NoRepositoryBean
public interface DefaultRepository<T extends PersistentEntity>
        extends JpaRepository<T, Long>, JpaSpecificationExecutor<T> {

    default List<T> findAllInactive() {
        return findAll(getEntityStateSpec(false));
    }

    default List<T> findAllActive() {
        return findAll(getEntityStateSpec(true));
    }

    default List<T> findAllBy(String filter, Boolean active) {
        return findAll(buildSpecification(filter, active));
    }

    default Specification<T> buildSpecification(String filter, Boolean active) {
        Specification<T> spec = Specification.allOf();
        if (filter != null && !filter.isBlank()) {
            spec = spec.and(getFilterSpecification(filter));
        }
        if (active != null) {
            spec = spec.and(getEntityStateSpec(active));
        }
        return spec;
    }

    default Specification<T> getFilterSpecification(String filter) {
        throw new UnsupportedOperationException("getFilterSpecification not implemented for " + getClass().getSimpleName());
    }

    default Specification<T> getEntityStateSpec(Boolean active) {
        throw new UnsupportedOperationException("getEntityStateSpec not implemented for " + getClass().getSimpleName());
    }

    default String likeAny(String filter) {
        return "%" + filter + "%";
    }

    default T saveAndFlushAndRefresh(T entity) {
        return saveAndFlush(entity);
    }

    default void attachAndRemove(T entity) {
        delete(entity);
    }

    default void removeAndFlush(T entity) {
        delete(entity);
        flush();
    }

    default void remove(T entity) {
        delete(entity);
    }
}
