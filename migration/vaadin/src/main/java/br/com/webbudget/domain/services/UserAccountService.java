package br.com.webbudget.domain.services;

import br.com.webbudget.application.components.dto.PasswordChangeDTO;
import br.com.webbudget.domain.entities.configuration.*;
import br.com.webbudget.domain.exceptions.BusinessLogicException;
import br.com.webbudget.domain.logics.tools.group.GroupDeletingLogic;
import br.com.webbudget.domain.logics.tools.user.UserDeletingLogic;
import br.com.webbudget.domain.logics.tools.user.UserSavingLogic;
import br.com.webbudget.domain.logics.tools.user.UserUpdatingLogic;
import br.com.webbudget.domain.repositories.configuration.*;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserAccountService {

    private final PasswordEncoder passwordEncoder;

    private final UserRepository userRepository;
    private final GrantRepository grantRepository;
    private final GroupRepository groupRepository;
    private final ProfileRepository profileRepository;
    private final AuthorizationRepository authorizationRepository;

    private final List<UserSavingLogic> userSavingBusinessLogics;
    private final List<UserUpdatingLogic> userUpdatingBusinessLogics;
    private final List<UserDeletingLogic> userDeletingBusinessLogics;
    private final List<GroupDeletingLogic> groupDeletingBusinessLogics;

    @Transactional
    public User save(User user) {
        this.userSavingBusinessLogics.forEach(logic -> logic.run(user));
        return this.userRepository.save(user);
    }

    @Transactional
    public void update(User user) {
        this.userUpdatingBusinessLogics.forEach(logic -> logic.run(user));
        this.userRepository.saveAndFlushAndRefresh(user);
    }

    @Transactional
    public void delete(User user) {
        this.userDeletingBusinessLogics.forEach(logic -> logic.run(user));
        this.userRepository.attachAndRemove(user);
    }

    @Transactional
    public void changePassword(PasswordChangeDTO passwordChangeDTO, User user) {
        final boolean actualMatch = this.passwordEncoder.matches(
                passwordChangeDTO.getActualPassword(), user.getPassword());
        if (actualMatch) {
            if (passwordChangeDTO.isNewPassMatching()) {
                user.setPassword(this.passwordEncoder.encode(passwordChangeDTO.getNewPassword()));
                this.userRepository.saveAndFlushAndRefresh(user);
                return;
            }
            throw new BusinessLogicException("error.change-password.new-pass-not-match");
        }
        throw new BusinessLogicException("error.change-password.actual-pass-not-match");
    }

    @Transactional
    public Group save(Group group) {
        return this.groupRepository.save(group);
    }

    @Transactional
    public void save(Group group, List<Authorization> authorizations) {
        this.groupRepository.save(group);
        authorizations.forEach(auth -> this.authorizationRepository
                .findByFunctionalityAndPermission(auth.getFunctionality(), auth.getPermission())
                .ifPresent(authorization -> this.grantRepository.save(new Grant(group, authorization)))
        );
    }

    @Transactional
    public void update(Group group) {
        this.groupRepository.saveAndFlushAndRefresh(group);
    }

    @Transactional
    public void update(Group group, List<Authorization> authorizations) {
        this.groupRepository.saveAndFlushAndRefresh(group);
        final List<Grant> oldGrants = this.grantRepository.findByGroup(group);
        oldGrants.forEach(grant -> this.grantRepository.remove(grant));
        authorizations.forEach(auth ->
                this.authorizationRepository
                        .findByFunctionalityAndPermission(auth.getFunctionality(), auth.getPermission())
                        .ifPresent(authorization -> this.grantRepository.save(new Grant(group, authorization)))
        );
    }

    @Transactional
    public void delete(Group group) {
        this.groupDeletingBusinessLogics.forEach(logic -> logic.run(group));
        this.groupRepository.attachAndRemove(group);
    }

    @Transactional
    public Profile updateUserProfile(Profile profile) {
        return this.profileRepository.saveAndFlushAndRefresh(profile);
    }

    public User findByUsername(String username) {
        return this.userRepository.findByUsername(username)
                .orElseThrow(() -> new BusinessLogicException("error.user.not-found"));
    }
}
