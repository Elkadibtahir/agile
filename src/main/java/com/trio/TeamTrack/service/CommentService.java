package com.trio.TeamTrack.service;

import com.trio.TeamTrack.entity.Comment;
import com.trio.TeamTrack.entity.Task;
import com.trio.TeamTrack.entity.User;
import com.trio.TeamTrack.repository.CommentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private AiService aiService;

    @Autowired
    private EmailService emailService; // ADDED: notify assignee on new comment

    public Comment addComment(String content, Task task, User author) {
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setTask(task);
        comment.setAuthor(author);

        // Analyse sentiment via AiService
        String sentiment = aiService.analyserSentiment(content);
        comment.setSentiment(sentiment);

        Comment saved = commentRepository.save(comment);

        // ADDED: notify the task assignee about the new comment
        emailService.envoyerNotificationCommentaire(saved, task);

        return saved;
    }

    public List<Comment> findByTask(Task task) {
        return commentRepository.findByTaskOrderByCreatedAtDesc(task);
    }

    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
