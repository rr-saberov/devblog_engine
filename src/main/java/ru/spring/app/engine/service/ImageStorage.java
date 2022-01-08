package ru.spring.app.engine.service;

import lombok.extern.slf4j.Slf4j;
import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FilenameUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.spring.app.engine.exception.ImageUploadException;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Objects;

@Slf4j
@Service
public class ImageStorage {

    @Value("${upload.path}")
    String uploadPath;

    public String uploadImageFile(MultipartFile file) throws IOException, ImageUploadException {
        if (Objects.equals(file.getContentType(), "image/jpeg")) {
            String resourceURI = null;
            RandomString random = new RandomString(4);
            String path = "/" + random.nextString() + "/" + random.nextString();
            if (!file.isEmpty()) {
                createDir(uploadPath + path);
                String fileName = random.nextString() + random.nextString()
                        + "." + FilenameUtils.getExtension(file.getOriginalFilename());
                Path imagePath = Paths.get(uploadPath + path, fileName);
                resourceURI = "/upload" + path + fileName;
                file.transferTo(imagePath);
                log.info(fileName + " uploaded OK!");
            }
            return resourceURI;
        } else {
            throw new ImageUploadException("wrong file type");
        }
    }

    public String uploadUserPhoto(MultipartFile file) throws IOException, ImageUploadException {
        if (Objects.equals(file.getContentType(), "image/jpeg")) {
            String resourceURI = null;
            RandomString random = new RandomString(4);
            String path = "/" + random.nextString() + "/" + random.nextString();
            if (!file.isEmpty()) {
                createDir(uploadPath + path);
                String fileName = random.nextString() + random.nextString()
                        + "." + FilenameUtils.getExtension(file.getOriginalFilename());
                Path imagePath = Paths.get(uploadPath + path, fileName);
                resourceURI = "/upload" + path + "/" + fileName;
                file.transferTo(imagePath);
                File imageFile = new File(imagePath.toString());
                BufferedImage image = ImageIO.read(imageFile);
                image.getSubimage(0, 0, 36, 36);
                ImageIO.write(image, "jpg", imageFile);
                log.info(fileName + " uploaded OK!");
            }
            return resourceURI;
        } else {
            throw new ImageUploadException("wrong file type");
        }
    }

    private void createDir(String path) throws IOException {
        if (!new File(path).exists()) {
            Files.createDirectories(Paths.get(path));
            log.info("create image folder in " + path);
        }
    }

}
