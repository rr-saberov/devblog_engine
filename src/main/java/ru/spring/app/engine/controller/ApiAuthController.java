package ru.spring.app.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.spring.app.engine.api.request.ChangePasswordRequest;
import ru.spring.app.engine.api.request.LoginRequest;
import ru.spring.app.engine.api.request.RegistrationRequest;
import ru.spring.app.engine.api.response.AuthResponse;
import ru.spring.app.engine.api.response.CaptchaResponse;
import ru.spring.app.engine.api.response.ChangePasswordResponse;
import ru.spring.app.engine.api.response.RegistrationResponse;
import ru.spring.app.engine.exceptions.CaptchaNotFoundException;
import ru.spring.app.engine.exceptions.RegistrationFailedException;
import ru.spring.app.engine.service.AuthService;
import ru.spring.app.engine.service.CaptchaService;
import ru.spring.app.engine.service.EmailService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;

@RestController
@RequestMapping("/api/auth")
@Tag(name = "auth controller")
public class ApiAuthController {

    private static final Logger LOGGER = LogManager.getLogger(ApiAuthController.class);
    private final AuthService authService;
    private final CaptchaService captchaService;
    private final EmailService emailService;

    public ApiAuthController(AuthService authService, CaptchaService captchaService, EmailService emailService) {
        this.authService = authService;
        this.captchaService = captchaService;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    @Operation(summary = "method to login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        LOGGER.info("trying to login");
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("/check")
    @Operation(summary = "method to check the current session")
    public ResponseEntity<AuthResponse> check(Principal principal) {
        LOGGER.info("checking the current user session");
        if (principal != null) {
            return ResponseEntity.ok(authService.check(principal.getName()));
        } else {
            return ResponseEntity.ok(new AuthResponse(false));
        }
    }

    @GetMapping("/captcha")
    @Operation(summary = "method to generate captcha")
    public ResponseEntity<CaptchaResponse> captcha() {
        LOGGER.info("generate new captcha");
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    @GetMapping("/logout")
    @Operation(summary = "method to logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request) {
        LOGGER.info("ending the current user session");
        HttpSession session = request.getSession();
        SecurityContextHolder.clearContext();
        session.invalidate();
        return ResponseEntity.ok(true);
    }

    @PostMapping("/register")
    @Operation(summary = "method to registration new user")
    public ResponseEntity<RegistrationResponse> registration(@RequestBody RegistrationRequest request) throws RegistrationFailedException {
        if (captchaService.validCaptcha(request.getCaptchaSecret(), request.getCaptcha())) {
            LOGGER.info("try to registration new user");
            return ResponseEntity.ok(authService.registration(request));
        } else {
            LOGGER.info("user entered the wrong captcha");
            throw new RegistrationFailedException("wrong captcha");
        }
  }

    @PostMapping("/restore")
    @Operation(summary = "method to restore password")
    public ResponseEntity<Boolean> restore(@RequestParam("email") String email) {
        LOGGER.info("restore users password");
        Boolean result = authService.restore(email);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password")
    @Operation(summary = "method to change password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest request) throws CaptchaNotFoundException {
        if (captchaService.validCaptcha(request.getCaptchaSecret(), request.getCaptcha())) {
            LOGGER.info("user attempt to change password");
            return ResponseEntity.ok(authService.changePassword(request));
        } else {
            LOGGER.error("user entered the wrong captcha");
            throw new CaptchaNotFoundException("wrong captcha");
        }
    }
}
