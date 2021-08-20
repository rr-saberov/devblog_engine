package ru.spring.app.engine.service;

import net.bytebuddy.utility.RandomString;
import org.apache.commons.io.FilenameUtils;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class ImageStorage {

    private final static Logger LOGGER = LogManager.getLogger(ImageStorage.class);

    @Value("${upload.path}")
    String uploadPath;

    public String saveNewImage(MultipartFile file) throws IOException {

        String resourceURI = null;
        RandomString random = new RandomString(4);
        String path = uploadPath + "/" + random.nextString() + "/" + random.nextString();

        if (!file.isEmpty()) {
            if (!new File(path).exists()) {
                Files.createDirectories(Paths.get(path));
                LOGGER.info("create image folder in " + path);
            }

            String fileName = random.nextString() + random.nextString()
                    + "." + FilenameUtils.getExtension(file.getOriginalFilename());
            Path imagePath = Paths.get(path, fileName);
            resourceURI = "/upload/" + fileName;
            file.transferTo(imagePath);
            LOGGER.info(fileName + " uploaded OK!");
        }

        return resourceURI;
    }
}
