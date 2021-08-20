package ru.spring.app.engine.service;

import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.request.CommentRequest;
import ru.spring.app.engine.api.response.AddCommentResponse;
import ru.spring.app.engine.api.response.errors.AddCommentError;
import ru.spring.app.engine.entity.PostComments;
import ru.spring.app.engine.repository.CommentRepository;
import ru.spring.app.engine.repository.PostRepository;
import ru.spring.app.engine.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Random;

@Service
public class CommentService {

    private final CommentRepository commentsRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    public CommentService(CommentRepository commentsRepository, PostRepository postRepository, UserRepository userRepository) {
        this.commentsRepository = commentsRepository;
        this.postRepository = postRepository;
        this.userRepository = userRepository;
    }

    public AddCommentResponse addComment(CommentRequest comment, String email) {
        AddCommentResponse response = new AddCommentResponse();
        if (commentsRepository.findById(comment.getParentId()).isPresent()
                || postRepository.findById(comment.getPostId()).isPresent()) {
            commentsRepository.save(convertRequestToPostComments(comment, email));
            response.setId(new Random().nextLong());
            response.setResult(true);
        } else {
            response.setResult(false);
            response.setErrors(addErrorsToList(comment));
        }
        return response;
    }

    private PostComments convertRequestToPostComments(CommentRequest comment, String email) {
        PostComments postComments = new PostComments();
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()), ZoneId.systemDefault());
        postComments.setParentId(comment.getParentId());
        postComments.setPostId(comment.getPostId());
        postComments.setText(comment.getText());
        postComments.setTime(time);
        postComments.setUserId(userRepository.getUserIdByEmail(email));
        return postComments;
    }

    private List<AddCommentError> addErrorsToList(CommentRequest comment) {
        List<AddCommentError> errors = new ArrayList<>();
        if (commentsRepository.findById(comment.getParentId()).isEmpty()) {
            errors.add(new AddCommentError("add fail, no comment with such id"));
        }
        if (postRepository.findById(comment.getPostId()).isEmpty()) {
            errors.add(new AddCommentError("add fail, no post with such id"));
        }
        if (comment.getText().length() <= 10) {
            errors.add(new AddCommentError("add fail, the text is too short"));
        }
        return errors;
    }
}
