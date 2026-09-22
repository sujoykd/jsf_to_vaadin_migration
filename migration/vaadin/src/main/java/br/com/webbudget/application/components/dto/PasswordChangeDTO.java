package br.com.webbudget.application.components.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

public class PasswordChangeDTO {

    @Getter
    @Setter
    @NotBlank(message = "{change-password.actual-password}")
    private String actualPassword;
    @Getter
    @Setter
    @NotBlank(message = "{change-password.new-password}")
    private String newPassword;
    @Getter
    @Setter
    @NotBlank(message = "{change-password.new-password-confirmation}")
    private String newPasswordConfirmation;

    public boolean isNewPassMatching() {
        return this.newPassword.equals(this.newPasswordConfirmation);
    }
}
