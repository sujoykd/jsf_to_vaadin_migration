package br.com.webbudget.domain.entities.configuration;

import br.com.webbudget.domain.entities.PersistentEntity;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import org.hibernate.envers.AuditTable;
import org.hibernate.envers.Audited;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import java.util.HashSet;
import java.util.Set;

import static br.com.webbudget.infrastructure.utils.DefaultSchemes.*;

@Entity
@Audited
@ToString(callSuper = true)
@EqualsAndHashCode(callSuper = true)
@Table(name = "users", schema = CONFIGURATION)
@AuditTable(value = "users", schema = CONFIGURATION_AUDIT)
public class User extends PersistentEntity {

    @Getter
    @Setter
    @NotNull(message = "{user.name}")
    @Column(name = "name", length = 90, nullable = false)
    private String name;
    @Getter
    @Setter
    @NotNull(message = "{user.email}")
    @Column(name = "email", length = 90, nullable = false)
    private String email;
    @Getter
    @Setter
    @NotNull(message = "{user.username}")
    @Column(name = "username", length = 20, nullable = false)
    private String username;
    @Getter
    @Setter
    @Column(name = "password", length = 60)
    private String password;
    @Getter
    @Setter
    @Column(name = "active", nullable = false)
    private boolean active;

    @Getter
    @Setter
    @JoinColumn(name = "id_profile", nullable = false)
    @OneToOne(optional = false, cascade = CascadeType.ALL)
    private Profile profile;
    @Getter
    @Setter
    @ManyToOne
    @NotNull(message = "{user.group}")
    @JoinColumn(name = "id_group", nullable = false)
    private Group group;

    @Getter
    @Setter
    @Enumerated(EnumType.STRING)
    @NotNull(message = "{user.store-type}")
    @Column(name = "store_type", nullable = false)
    private StoreType storeType;

    @Getter
    @Setter
    @Transient
    private String passwordConfirmation;

    public User() {
        this.active = true;
        this.profile = new Profile();
        this.storeType = StoreType.LOCAL;
    }

    public boolean isBlocked() {
        return !this.isActive();
    }

    public boolean isLdapBindAccount() {
        return this.storeType == StoreType.LDAP;
    }

    public Set<String> getPermissions() {
        return this.group != null ? this.group.getPermissions() : new HashSet<>();
    }

    public String getGroupName() {
        return this.group != null ? this.group.getName() : null;
    }

    public boolean isPasswordValid() {
        return this.password != null && !this.password.isBlank()
                && this.passwordConfirmation != null && !this.passwordConfirmation.isBlank()
                && this.password.equals(this.passwordConfirmation);
    }

    public boolean hasChangedPasswords() {
        return this.password != null && !this.password.isBlank()
                && this.passwordConfirmation != null && !this.passwordConfirmation.isBlank();
    }

    public boolean isAdministrator() {
        return this.username.equals("admin");
    }
}
