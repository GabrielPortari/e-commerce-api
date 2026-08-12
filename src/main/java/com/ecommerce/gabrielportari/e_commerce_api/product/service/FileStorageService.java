package com.ecommerce.gabrielportari.e_commerce_api.product.service;

import com.ecommerce.gabrielportari.e_commerce_api.exception.BusinessException;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.UUID;
import javax.imageio.ImageIO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
public class FileStorageService {

    private static final List<String> ALLOWED_CONTENT_TYPES =
            List.of("image/png", "image/jpeg", "image/webp");

    private final Path uploadDir;

    public FileStorageService(@Value("${app.upload.dir}") String uploadDir) {
        this.uploadDir = Paths.get(uploadDir).toAbsolutePath().normalize();
        try {
            Files.createDirectories(this.uploadDir);
        } catch (IOException ex) {
            throw new IllegalStateException("Não foi possível criar o diretório de uploads", ex);
        }
    }

    public String store(MultipartFile file) {
        if (file.isEmpty()) {
            throw new BusinessException("Arquivo de imagem vazio");
        }
        if (!ALLOWED_CONTENT_TYPES.contains(file.getContentType())) {
            throw new BusinessException("Formato de imagem não suportado. Use PNG, JPEG ou WEBP");
        }

        BufferedImage image;
        try {
            image = ImageIO.read(file.getInputStream());
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao ler a imagem enviada", ex);
        }
        if (image == null) {
            throw new BusinessException("Arquivo enviado não é uma imagem válida");
        }

        String extension = StringUtils.getFilenameExtension(file.getOriginalFilename());
        String filename = UUID.randomUUID() + (extension != null ? "." + extension : "");

        try {
            Path target = uploadDir.resolve(filename);
            Files.copy(file.getInputStream(), target);
        } catch (IOException ex) {
            throw new IllegalStateException("Falha ao salvar a imagem", ex);
        }

        return "/uploads/" + filename;
    }
}
