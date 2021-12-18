package ru.spring.app.engine.service;

import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.spring.app.engine.api.request.EditProfileRequest;
import ru.spring.app.engine.api.response.EditProfileResponse;
import ru.spring.app.engine.api.response.errors.EditProfileError;
import ru.spring.app.engine.entity.User;
import ru.spring.app.engine.repository.UserRepository;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final ImageStorage imageStorage;

    @SneakyThrows
    public EditProfileResponse editProfile(String name, String email, String password, Integer removePhoto,
                                           MultipartFile file, Principal principal) {
        EditProfileRequest request = createEditProfileRequest(name, email, password, removePhoto);
        EditProfileResponse response = new EditProfileResponse();
        List<EditProfileError> errors = getErrors(request, file, principal);
        User currentUser = userRepository.findByEmail(principal.getName()).get();
        if (errors.isEmpty() && !currentUser.getEmail().equals(request.getEmail())) {
            userRepository.updateUserEmail(request.getEmail(), currentUser.getId());
            response.setResult(true);
        } else if (errors.isEmpty() && !currentUser.getName().equals(request.getName())) {
            userRepository.updateUserName(request.getName(), currentUser.getId());
            response.setResult(true);
        } else if (errors.isEmpty() && request.getPassword() != null) {
            userRepository.updateUserPassword(passwordEncoder.encode(request.getPassword()), currentUser.getId());
            response.setResult(true);
        } else if (errors.isEmpty() && file != null){
            userRepository.updateUserImage(imageStorage.uploadUserPhoto(file), currentUser.getId());
            response.setResult(true);
        } else {
            response.setErrors(errors);
            response.setResult(false);
        }
        return response;
    }

    private EditProfileRequest createEditProfileRequest(String name, String email, String password, Integer removePhoto) {
        EditProfileRequest request = new EditProfileRequest();
        request.setName(name);
        request.setEmail(email);
        request.setPassword(password);
        request.setRemovePhoto(removePhoto);
        return request;
    }

    private List<EditProfileError> getErrors(EditProfileRequest request, MultipartFile file, Principal principal) {
        List<EditProfileError> errors = new ArrayList<>();
        if (!request.getEmail().isEmpty() && !principal.getName().equals(request.getEmail()) &&
                userRepository.findByEmail(request.getEmail()).isPresent()) {
            EditProfileError error = new EditProfileError();
            error.setEmail("you sent an incorrect email");
            errors.add(error);
        } else if (request.getName().isEmpty()) {
            EditProfileError error = new EditProfileError();
            error.setName("you sent incorrect name");
            errors.add(error);
        } else if (request.getPassword() != null && request.getPassword().length() < 6) {
            EditProfileError error = new EditProfileError();
            error.setPassword("you sent incorrect password");
            errors.add(error);
        } else if (!file.isEmpty() && file.getSize() > Math.pow(10, 6) * 5) {
            EditProfileError error = new EditProfileError();
            error.setPhoto("file size is too big or file is empty");
            errors.add(error);
        }

        return errors;
    }
}
