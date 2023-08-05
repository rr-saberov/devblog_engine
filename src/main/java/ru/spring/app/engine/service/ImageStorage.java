package ru.spring.app.engine.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.spring.app.engine.exception.ImageUploadException;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorage {

    @Value("${upload.path}")
    String uploadPath;

    private final Cloudinary cloudinary;

    public String uploadImageFile(MultipartFile file) throws IOException, ImageUploadException {
        if (Objects.equals(file.getContentType(), "image/jpeg") && !file.isEmpty()) {
            Map uploadResult;
            File uploadedImage = convertMultipartFileToFile(file);
            uploadResult = cloudinary.uploader().upload(uploadedImage, ObjectUtils.emptyMap());
            uploadedImage.delete();
            log.info("image uploaded success!");
            return uploadResult.get("url").toString();
        } else {
            throw new ImageUploadException("wrong file type or empty file");
        }
    }

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        var file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (var outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        }
        return file;
    }

}
