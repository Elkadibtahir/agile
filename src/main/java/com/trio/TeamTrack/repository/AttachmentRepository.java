package com.trio.TeamTrack.repository;

import com.trio.TeamTrack.entity.Attachment;
import com.trio.TeamTrack.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AttachmentRepository extends JpaRepository<Attachment, Long> {
    List<Attachment> findByProjectOrderByUploadedAtDesc(Project project);
}
