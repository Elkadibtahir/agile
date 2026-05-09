package com.trio.TeamTrack.controller;

import com.trio.TeamTrack.entity.Attachment;
import com.trio.TeamTrack.entity.Project;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.service.FileService;
import com.trio.TeamTrack.service.ProjectService;
import com.trio.TeamTrack.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.Path;

@Controller
@RequestMapping("/projects/{projectId}/files")
public class FileController {

    @Autowired
    private FileService fileService;

    @Autowired
    private ProjectService projectService;

    @Autowired
    private UserService userService;

    @PostMapping("/upload")
    public String uploadFile(@PathVariable Long projectId,
                             @RequestParam("file") MultipartFile file,
                             @AuthenticationPrincipal UserDetails ud,
                             RedirectAttributes ra) {
        if (file.isEmpty()) {
            ra.addFlashAttribute("error", "Veuillez sélectionner un fichier.");
            return "redirect:/projects/" + projectId;
        }

        try {
            Project project = projectService.findById(projectId);
            User uploader = userService.findByUsername(ud.getUsername());
            fileService.saveFile(file, project, uploader);
            ra.addFlashAttribute("message", "Fichier mis en ligne avec succès !");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Erreur lors de l'enregistrement du fichier : " + e.getMessage());
        }

        return "redirect:/projects/" + projectId;
    }

    @GetMapping("/download/{fileId}")
    public ResponseEntity<Resource> downloadFile(@PathVariable Long fileId) {
        try {
            Attachment attachment = fileService.getAttachment(fileId);
            Path path = fileService.getFilePath(attachment);
            Resource resource = new UrlResource(path.toUri());

            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(attachment.getFileType() != null ? attachment.getFileType() : "application/octet-stream"))
                        .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + attachment.getFileName() + "\"")
                        .body(resource);
            } else {
                throw new RuntimeException("Impossible de lire le fichier.");
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException("Erreur : " + e.getMessage());
        }
    }

    @PostMapping("/delete/{fileId}")
    public String deleteFile(@PathVariable Long projectId,
                             @PathVariable Long fileId,
                             RedirectAttributes ra) {
        try {
            fileService.deleteFile(fileId);
            ra.addFlashAttribute("message", "Fichier supprimé.");
        } catch (IOException e) {
            ra.addFlashAttribute("error", "Erreur lors de la suppression : " + e.getMessage());
        }
        return "redirect:/projects/" + projectId;
    }
}
