package ru.spring.app.engine.service;

import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.request.ChangePasswordRequest;
import ru.spring.app.engine.api.request.LoginRequest;
import ru.spring.app.engine.api.request.RegistrationRequest;
import ru.spring.app.engine.api.request.RestoreRequest;
import ru.spring.app.engine.api.response.AuthResponse;
import ru.spring.app.engine.api.response.AuthUserResponse;
import ru.spring.app.engine.api.response.ChangePasswordResponse;
import ru.spring.app.engine.api.response.RegistrationResponse;
import ru.spring.app.engine.api.response.errors.ChangePasswordError;
import ru.spring.app.engine.api.response.errors.RegistrationProfileError;
import ru.spring.app.engine.entity.Captcha;
import ru.spring.app.engine.exception.CaptchaNotFoundException;
import ru.spring.app.engine.exception.RegistrationFailedException;
import ru.spring.app.engine.repository.CaptchaRepository;
import ru.spring.app.engine.repository.GlobalSettingsRepository;
import ru.spring.app.engine.repository.UserRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final CaptchaRepository captchaRepository;
    private final AuthenticationManager authenticationManager;
    private final PasswordEncoder passwordEncoder;
    private final GlobalSettingsRepository globalSettingsRepository;
    private final EmailService emailService;
    private final CaptchaService captchaService;

    public AuthResponse login(LoginRequest loginRequest) {
        Authentication auth = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getEmail(), loginRequest.getPassword()));
            SecurityContextHolder.getContext().setAuthentication(auth);
            User user = (User) auth.getPrincipal();
            return convertToResponse(user.getUsername());
    }

    public AuthResponse check(Principal principal) {
        return principal != null ? convertToResponse(principal.getName()) : new AuthResponse(false);
    }

    public RegistrationResponse registration(RegistrationRequest request) throws RegistrationFailedException {
        if (!globalSettingsRepository.getMultiuserMode().getValue().equals("YES")) {
            throw new RegistrationFailedException("Registration failed check correctness of input");
        }

        if (getErrors(request).isEmpty()) {
            var user = new ru.spring.app.engine.entity.User();
            user.setEmail(request.getEmail());
            user.setIsModerator(-1);
            user.setPassword(passwordEncoder.encode(request.getPassword()));
            user.setName(request.getName());
            user.setRegTime(new Date(System.currentTimeMillis()));
            userRepository.save(user);
            return new RegistrationResponse(true);
        } else {
            return new RegistrationResponse(false, getErrors(request));
        }

    }

    public ChangePasswordResponse changePassword(ChangePasswordRequest request) throws CaptchaNotFoundException {
        var response = new ChangePasswordResponse();
        List<ChangePasswordError> errors = changePasswordErrors(request);
        ru.spring.app.engine.entity.User currentUser = userRepository.findByCode(request.getCode());
        if (errors.isEmpty() && captchaService.validCaptcha(request.getCaptchaSecret(), request.getCaptcha())) {
            userRepository.updateUserPassword(passwordEncoder.encode(request.getPassword()), currentUser.getId());
            response.setResult(true);
        } else if (captchaService.validCaptcha(request.getCaptchaSecret(), request.getCaptcha())) {
            response.setResult(false);
            response.setErrors(errors);
        } else {
            throw new CaptchaNotFoundException("wrong captcha");
        }
        return response;
    }

    public Boolean restore(RestoreRequest request) {
        var randomString = new RandomString(20);
        String secret = randomString.nextString();
        String message = "https://localhost:8080/login/change-password/" + secret;
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            userRepository.updateUserCode(secret, request.getEmail());
            emailService.sendEmail(request.getEmail(), "subject", message);
            return true;
        } else {
            return false;
        }
    }

    private List<RegistrationProfileError> getErrors(RegistrationRequest request) {
        List<RegistrationProfileError> errors = new ArrayList<>();
        if (!captchaService.validCaptcha(request.getCaptchaSecret(), request.getCaptcha())) {
            var error = new RegistrationProfileError();
            error.setCaptcha("incorrect captcha entered ");
            errors.add(error);
        }
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            var error = new RegistrationProfileError();
            error.setEmail("This email is already registered");
            errors.add(error);
        }
        if (request.getName().length() < 4 || request.getName().length() > 25) {
            var error = new RegistrationProfileError();
            error.setName("Name is incorrect");
            errors.add(error);
        }
        if (request.getPassword().length() < 6) {
            var error = new RegistrationProfileError();
            error.setPassword("Length of the password is less than 6 characters");
            errors.add(error);
        }
        return errors;
    }

    private List<ChangePasswordError> changePasswordErrors(ChangePasswordRequest request) throws CaptchaNotFoundException {
        List<ChangePasswordError> errors = new ArrayList<>();
        Captcha captcha = captchaRepository
                .findBySecretCode(request.getCaptchaSecret()).orElseThrow(() ->
                        new CaptchaNotFoundException(request.getCaptchaSecret()));
        if (request.getPassword().length() < 6) {
            var error = new ChangePasswordError();
            error.setPassword("password must be at least 6 characters");
            errors.add(error);
        }
        if (!captcha.getSecretCode().equals(request.getCaptchaSecret())) {
            var error = new ChangePasswordError();
            error.setCaptcha("invalid captcha");
            errors.add(error);
        }
        if (!request.getCode().equals(userRepository.findByCode(request.getCode()).getCode())) {
            var error = new ChangePasswordError();
            error.setCode("invalid restore code");
            errors.add(error);
        }
        return errors;
    }

    private AuthResponse convertToResponse(String email) {
        ru.spring.app.engine.entity.User currentUser = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException(email));
        var userResponse = new AuthUserResponse();
        userResponse.setEmail(currentUser.getEmail());
        userResponse.setName(currentUser.getName());
        userResponse.setPhoto(currentUser.getPhoto());
        userResponse.setModeration(currentUser.getIsModerator() == 1);
        userResponse.setId(currentUser.getId());
        var authResponse = new AuthResponse();
        authResponse.setResult(true);
        authResponse.setAuthUserResponse(userResponse);
        return authResponse;
    }
}
