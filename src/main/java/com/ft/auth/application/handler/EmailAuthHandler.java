package com.ft.auth.application.handler;

import com.ft.auth.application.dto.LoginCommand;
import com.ft.auth.application.port.RefreshTokenRepository;
import com.ft.auth.application.port.TokenProvider;
import com.ft.auth.application.port.UserRepository;
import com.ft.auth.domain.PasswordValidator;
import com.ft.auth.domain.User;
import com.ft.common.exception.CustomException;
import org.springframework.stereotype.Component;

import static com.ft.back.common.exception.ErrorCode.AUTH_USER_NOT_FOUND;

@Component
public class EmailAuthHandler extends AbstractAuthHandler {

    private final PasswordValidator passwordValidator;

    public EmailAuthHandler(UserRepository userRepository,
                            RefreshTokenRepository refreshTokenRepository,
                            TokenProvider tokenProvider,
                            PasswordValidator passwordValidator) {
        super(userRepository, refreshTokenRepository, tokenProvider);
        this.passwordValidator = passwordValidator;
    }

    @Override
    protected User loadUser(LoginCommand command) {
        return userRepository.findByEmail(command.email())
                .orElseThrow(() -> new CustomException(AUTH_USER_NOT_FOUND));
    }

    @Override
    protected void verifyCredentials(User user, LoginCommand command) {
        user.validatePassword(command.rawPassword(), passwordValidator);
    }
}
