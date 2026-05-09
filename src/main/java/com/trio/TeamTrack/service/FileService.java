package com.trio.TeamTrack.service;

import com.trio.TeamTrack.entity.Attachment;
import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.repository.AttachmentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.UUID;

@Service
public class FileService {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Autowired
    private AttachmentRepository attachmentRepository;

    public Attachment saveFile(MultipartFile file, Project project, User uploader) throws IOException {
        Path root = Paths.get(uploadDir);
        if (!Files.exists(root)) {
            Files.createDirectories(root);
        }

        String originalFileName = file.getOriginalFilename();
        String fileExtension = "";
        if (originalFileName != null && originalFileName.contains(".")) {
            fileExtension = originalFileName.substring(originalFileName.lastIndexOf("."));
        }
        
        String storedFileName = UUID.randomUUID().toString() + fileExtension;
        Path targetPath = root.resolve(storedFileName);

        Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        Attachment attachment = new Attachment();
        attachment.setFileName(originalFileName);
        attachment.setFileType(file.getContentType());
        attachment.setFileSize(file.getSize());
        attachment.setFilePath(storedFileName);
        attachment.setProject(project);
        attachment.setUploader(uploader);

        return attachmentRepository.save(attachment);
    }

    public List<Attachment> getProjectFiles(Project project) {
        return attachmentRepository.findByProjectOrderByUploadedAtDesc(project);
    }

    public Attachment getAttachment(Long id) {
        return attachmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Fichier introuvable."));
    }

    public Path getFilePath(Attachment attachment) {
        return Paths.get(uploadDir).resolve(attachment.getFilePath());
    }

    public void deleteFile(Long id) throws IOException {
        Attachment attachment = getAttachment(id);
        Path path = getFilePath(attachment);
        Files.deleteIfExists(path);
        attachmentRepository.delete(attachment);
    }
}
