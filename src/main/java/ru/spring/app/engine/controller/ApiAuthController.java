package ru.spring.app.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.spring.app.engine.api.request.ChangePasswordRequest;
import ru.spring.app.engine.api.request.LoginRequest;
import ru.spring.app.engine.api.request.RegistrationRequest;
import ru.spring.app.engine.api.request.RestoreRequest;
import ru.spring.app.engine.api.response.AuthResponse;
import ru.spring.app.engine.api.response.CaptchaResponse;
import ru.spring.app.engine.api.response.ChangePasswordResponse;
import ru.spring.app.engine.api.response.RegistrationResponse;
import ru.spring.app.engine.exception.RegistrationFailedException;
import ru.spring.app.engine.service.AuthService;
import ru.spring.app.engine.service.CaptchaService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "auth controller")
public class ApiAuthController {

    private final AuthService authService;
    private final CaptchaService captchaService;

    @PostMapping("/login")
    @Operation(summary = "method to login")
    public ResponseEntity<AuthResponse> login(@RequestBody LoginRequest loginRequest) {
        log.info("trying to login");
        return ResponseEntity.ok(authService.login(loginRequest));
    }

    @GetMapping("/check")
    @Operation(summary = "method to check the current session")
    public ResponseEntity<AuthResponse> check(Principal principal) {
        log.info("checking the current user session");
        return ResponseEntity.ok(authService.check(principal));
    }

    @GetMapping("/captcha")
    @Operation(summary = "method to generate captcha")
    public ResponseEntity<CaptchaResponse> captcha() {
        log.info("generate new captcha");
        return ResponseEntity.ok(captchaService.generateCaptcha());
    }

    @GetMapping("/logout")
    @Operation(summary = "method to logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request) {
        log.info("ending the current user session");
        HttpSession session = request.getSession();
        SecurityContextHolder.clearContext();
        session.invalidate();
        return ResponseEntity.ok(true);
    }

    @PostMapping("/register")
    @Operation(summary = "method to registration new user")
    public ResponseEntity<RegistrationResponse> registration(@RequestBody RegistrationRequest request) throws RegistrationFailedException {
        log.info("try to registration new user");
        return ResponseEntity.ok(authService.registration(request));
    }

    @PostMapping("/restore")
    @Operation(summary = "method to restore password")
    public ResponseEntity<Boolean> restore(@RequestBody RestoreRequest response) {
        log.info("restore users password");
        Boolean result = authService.restore(response);
        return ResponseEntity.ok(result);
    }

    @PostMapping("/password")
    @SneakyThrows
    @Operation(summary = "method to change password")
    public ResponseEntity<ChangePasswordResponse> changePassword(@RequestBody ChangePasswordRequest request) {
        log.info("user attempt to change password");
        return ResponseEntity.ok(authService.changePassword(request));
    }
}
