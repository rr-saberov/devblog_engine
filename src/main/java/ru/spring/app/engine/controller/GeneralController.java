package ru.spring.app.engine.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import ru.spring.app.engine.api.request.SettingsRequest;
import ru.spring.app.engine.api.response.CalendarResponse;
import ru.spring.app.engine.api.response.EditProfileResponse;
import ru.spring.app.engine.api.response.InitResponse;
import ru.spring.app.engine.api.response.SettingsResponse;
import ru.spring.app.engine.api.response.StatisticsResponse;
import ru.spring.app.engine.exception.AccessIsDeniedException;
import ru.spring.app.engine.service.ImageStorage;
import ru.spring.app.engine.service.PostService;
import ru.spring.app.engine.service.SettingsService;
import ru.spring.app.engine.service.UserService;

import java.security.Principal;

@Slf4j
@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "api general controller")
public class GeneralController {

    private final SettingsService settingsService;
    private final PostService postService;
    private final InitResponse initResponse;
    private final ImageStorage storage;
    private final UserService userService;

    @GetMapping("/init")
    @Operation(summary = "init method")
    public ResponseEntity<InitResponse> init() {
        return ResponseEntity.ok(initResponse);
    }

    @GetMapping("/settings")
    @Operation(summary = "method to get settings")
    public ResponseEntity<SettingsResponse> settings() {
        return ResponseEntity.ok(settingsService.getGlobalSettings());
    }

    @PutMapping("/settings")
    @Operation(summary = "method to change settings")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<Boolean> updateSettings(@RequestBody SettingsRequest request) {
        log.info("try to change global settings");
        return ResponseEntity.ok(settingsService.updateGlobalSettings(request));
    }

    @GetMapping("/calendar")
    @Operation(summary = "method to get calendar")
    public ResponseEntity<CalendarResponse> getPostCountInYear(@RequestParam(defaultValue = "0") Integer year) {
        return ResponseEntity.ok(postService.getPostsCountInTheYear(year));
    }

    @SneakyThrows
    @PostMapping("/image")
    @Operation(summary = "method to upload image")
    @PreAuthorize("hasAuthority('user:write')")
    public String saveImage(@RequestPart MultipartFile image) {
        log.info("try to upload image");
        String savePath = storage.uploadImageFile(image);
        return (savePath);
    }

    @PostMapping("/profile/my")
    @Operation(summary = "method to change profile")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<EditProfileResponse> editProfile(@RequestParam String name,
                                                           @RequestParam String email,
                                                           @RequestParam(required = false) String password,
                                                           @RequestParam(required = false) Integer removePhoto,
                                                           @RequestPart(name = "photo", required = false) MultipartFile photo,
                                                           Principal principal) {
        log.info("try to change user profile");
        return ResponseEntity.ok(userService.editProfile(name, email, password, removePhoto, photo, principal));
    }

    @GetMapping("/statistics/my")
    @Operation(summary = "method to get users statistics")
    @PreAuthorize("hasAuthority('user:write')")
    public ResponseEntity<StatisticsResponse> getMyStatistics(Principal principal) {
        return ResponseEntity.ok(postService.getUserStatistics(principal.getName()));
    }

    @GetMapping("/statistics/all")
    @Operation(summary = "method to get all statistics")
    @PreAuthorize("hasAuthority('user:moderate')")
    public ResponseEntity<StatisticsResponse> getStatistics(Principal principal) throws AccessIsDeniedException {
        log.info("try to view statistics");
        return ResponseEntity.ok(postService.getStatistics(principal.getName()));
    }
}
