package ru.spring.app.engine.service;

import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.request.CommentRequest;
import ru.spring.app.engine.api.response.AddCommentResponse;
import ru.spring.app.engine.api.response.errors.AddCommentError;
import ru.spring.app.engine.entity.PostComments;
import ru.spring.app.engine.exceptions.AddCommentFailException;
import ru.spring.app.engine.repository.CommentRepository;
import ru.spring.app.engine.repository.PostRepository;
import ru.spring.app.engine.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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

    public AddCommentResponse addComment(CommentRequest comment, String email) throws AddCommentFailException {
        List<AddCommentError> errors = addErrorsToList(comment);
        if (!errors.isEmpty()) {
            return createResponseWithErrors(comment, errors);
        }
        commentsRepository.save(convertRequestToPostComments(comment, email));
        AddCommentResponse response = new AddCommentResponse();
        response.setId(new Random().nextLong());
        response.setResult(true);
        return response;
    }

    private List<AddCommentError> addErrorsToList(CommentRequest comment) throws AddCommentFailException {
        List<AddCommentError> errors = new ArrayList<>();
        if (comment.getPostId() == 0 && commentsRepository.findById(comment.getParentId()).isEmpty()) {
            throw new AddCommentFailException("no comment with such id");
        }
        if (postRepository.findById(comment.getPostId()).isEmpty()) {
            throw new AddCommentFailException("no post with such id");
        }
        if (comment.getText().length() <= 10) {
            errors.add(new AddCommentError("add fail, the text is too short"));
        }
        return errors;
    }

    private PostComments convertRequestToPostComments(CommentRequest comment, String email) {
        PostComments postComments = new PostComments();
        LocalDateTime time = LocalDateTime.ofInstant(Instant.ofEpochMilli(System.currentTimeMillis()),
                ZoneId.of("UTC").normalized());
        postComments.setParentId(comment.getParentId());
        postComments.setPostId(comment.getPostId());
        postComments.setText(comment.getText());
        postComments.setTime(time);
        postComments.setUserId(userRepository.getUserIdByEmail(email));
        return postComments;
    }

    private AddCommentResponse createResponseWithErrors(CommentRequest commentRequest, List<AddCommentError> errors) {
        AddCommentResponse response = new AddCommentResponse();
        response.setResult(false);
        response.setErrors(errors);
        return response;
    }
}
