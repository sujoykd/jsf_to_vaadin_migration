package br.com.webbudget.domain.entities;

import org.springframework.security.core.context.SecurityContextHolder;

public class RevisionListener implements org.hibernate.envers.RevisionListener {

    @Override
    public void newRevision(Object revisionEntity) {
        final Revision revision = (Revision) revisionEntity;
        revision.setCreatedBy(this.getLoggedUser());
    }

    private String getLoggedUser() {
        try {
            final var auth = SecurityContextHolder.getContext().getAuthentication();
            return auth != null ? auth.getName() : "unknown";
        } catch (Exception ex) {
            return "unknown";
        }
    }
}
