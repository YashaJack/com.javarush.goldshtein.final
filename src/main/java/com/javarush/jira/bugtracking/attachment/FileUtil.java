package com.javarush.jira.bugtracking.attachment;

import com.javarush.jira.common.error.IllegalRequestDataException;
import com.javarush.jira.common.error.NotFoundException;
import lombok.experimental.UtilityClass;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

@UtilityClass
public class FileUtil {
    private static final String ATTACHMENT_PATH = "./attachments/%s/";

    public static void upload(MultipartFile multipartFile, String directoryPath, String fileName) {
        if (multipartFile == null || multipartFile.isEmpty()) {
            throw new IllegalRequestDataException("Select a file to upload.");
        }
        if (fileName == null || fileName.isBlank()) {
            throw new IllegalRequestDataException("File name is invalid.");
        }

        try {
            // что директория существует
            Path dir = Path.of(directoryPath).toAbsolutePath().normalize();
            Files.createDirectories(dir);

            // чтобы не было
            Path target = dir.resolve(fileName).normalize();

            // Запрещаем выход из базовой директории
            if (!target.startsWith(dir)) {
                throw new IllegalRequestDataException("Invalid file path.");
            }

            // Копируем поток
            Files.copy(multipartFile.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException ex) {
            throw new IllegalRequestDataException("Failed to upload file " + multipartFile.getOriginalFilename());
        }
    }

    public static Resource download(String fileLink) {
        try {
            Path path = Path.of(fileLink).toAbsolutePath().normalize();
            Resource resource = new UrlResource(path.toUri());
            if (resource.exists() && resource.isReadable()) {
                return resource;
            }
            throw new IllegalRequestDataException("Failed to download file " + resource.getFilename());
        } catch (MalformedURLException ex) {
            throw new NotFoundException("File " + fileLink + " not found");
        }
    }

    public static void delete(String fileLink) {
        try {
            Path path = Path.of(fileLink).toAbsolutePath().normalize();
            if (!Files.deleteIfExists(path)) {
                throw new NotFoundException("File " + fileLink + " not found");
            }
        } catch (IOException ex) {
            throw new IllegalRequestDataException("File " + fileLink + " deletion failed.");
        }
    }

    public static String getPath(String titleType) {
        return String.format(ATTACHMENT_PATH, titleType.toLowerCase());
    }
}