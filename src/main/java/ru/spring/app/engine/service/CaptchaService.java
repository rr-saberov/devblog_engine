package ru.spring.app.engine.service;

import com.github.cage.Cage;
import com.github.cage.GCage;
import lombok.RequiredArgsConstructor;
import net.bytebuddy.utility.RandomString;
import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.response.CaptchaResponse;
import ru.spring.app.engine.entity.Captcha;
import ru.spring.app.engine.repository.CaptchaRepository;

import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class CaptchaService {

    private final CaptchaRepository captchaRepository;

    public CaptchaResponse generateCaptcha() {
        restoreOldCaptcha();
        var cage = new GCage();
        var token = cage.getTokenGenerator().next();
        var secretCode = new RandomString(12).nextString();
        captchaRepository.save(new Captcha(LocalDateTime.now(), token, secretCode));
        byte[] fileContent = cage.draw(token);
        String encodedString = "data:image/png;base64, " + Base64.getEncoder().encodeToString(fileContent);
        return new CaptchaResponse(secretCode, encodedString);
    }

    public Boolean validCaptcha(String secretCode, String currentCaptcha) {
        return captchaRepository.findBySecretCode(secretCode).isPresent()
                && captchaRepository.findBySecretCode(secretCode).get().getCode().equals(currentCaptcha);
    }

    private void restoreOldCaptcha() {
        captchaRepository.findAll().forEach(cp -> {
            if(cp.getTime().isBefore(LocalDateTime.now().plusHours(1)))
                captchaRepository.delete(cp);
        });
    }
}
