package ru.spring.app.engine.service;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import lombok.RequiredArgsConstructor;
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
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ImageStorage {

    @Value("${upload.path}")
    String uploadPath;

    private final Cloudinary cloudinaryConfig;

    public String uploadImageFile(MultipartFile file) throws IOException, ImageUploadException {
        if (Objects.equals(file.getContentType(), "image/jpeg") || file.isEmpty()) {
            Map uploadResult;
            File uploadedImage = convertMultipartFileToFile(file);
            uploadResult = cloudinaryConfig.uploader().upload(uploadedImage, ObjectUtils.emptyMap());
            uploadedImage.delete();
            //log.info(fileName + " uploaded OK!");
            return uploadResult.get("url").toString();
        } else {
            throw new ImageUploadException("wrong file type or empty file");
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

    private File convertMultipartFileToFile(MultipartFile multipartFile) throws IOException {
        File file = new File(Objects.requireNonNull(multipartFile.getOriginalFilename()));
        try (FileOutputStream outputStream = new FileOutputStream(file)) {
            outputStream.write(multipartFile.getBytes());
        }
        return file;
    }

    private void createDir(String path) throws IOException {
        if (!new File(path).exists()) {
            Files.createDirectories(Paths.get(path));
            log.info("create image folder in " + path);
        }
    }

//    public String uploadImageFile(MultipartFile file) throws IOException, ImageUploadException {
//        if (Objects.equals(file.getContentType(), "image/jpeg")) {
//            String resourceURI = null;
//            RandomString random = new RandomString(4);
//            String path = "/" + random.nextString() + "/" + random.nextString();
//            if (!file.isEmpty()) {
//                createDir(uploadPath + path);
//                String fileName = random.nextString() + random.nextString()
//                        + "." + FilenameUtils.getExtension(file.getOriginalFilename());
//                Path imagePath = Paths.get(uploadPath + path, fileName);
//                resourceURI = "/upload" + path + fileName;
//                file.transferTo(imagePath);
//                log.info(fileName + " uploaded OK!");
//            }
//            return resourceURI;
//        } else {
//            throw new ImageUploadException("wrong file type");
//        }
//    }
//
//    public String uploadUserPhoto(MultipartFile file) throws IOException, ImageUploadException {
//        if (Objects.equals(file.getContentType(), "image/jpeg")) {
//            String resourceURI = null;
//            RandomString random = new RandomString(4);
//            String path = "/" + random.nextString() + "/" + random.nextString();
//            if (!file.isEmpty()) {
//                createDir(uploadPath + path);
//                String fileName = random.nextString() + random.nextString()
//                        + "." + FilenameUtils.getExtension(file.getOriginalFilename());
//                Path imagePath = Paths.get(uploadPath + path, fileName);
//                resourceURI = "/upload" + path + "/" + fileName;
//                file.transferTo(imagePath);
//                File imageFile = new File(imagePath.toString());
//                BufferedImage image = ImageIO.read(imageFile);
//                image.getSubimage(0, 0, 36, 36);
//                ImageIO.write(image, "jpg", imageFile);
//                log.info(fileName + " uploaded OK!");
//            }
//            return resourceURI;
//        } else {
//            throw new ImageUploadException("wrong file type");
//        }
//    }
//
//    private void createDir(String path) throws IOException {
//        if (!new File(path).exists()) {
//            Files.createDirectories(Paths.get(path));
//            log.info("create image folder in " + path);
//        }
//    }

}
