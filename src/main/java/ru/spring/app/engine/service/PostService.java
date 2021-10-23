package ru.spring.app.engine.service;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.request.AddPostRequest;
import ru.spring.app.engine.api.request.ModerationRequest;
import ru.spring.app.engine.api.request.VoteRequest;
import ru.spring.app.engine.api.response.AddPostResponse;
import ru.spring.app.engine.api.response.CalendarResponse;
import ru.spring.app.engine.api.response.CommentResponse;
import ru.spring.app.engine.api.response.CommentUserResponse;
import ru.spring.app.engine.api.response.CurrentPostResponse;
import ru.spring.app.engine.api.response.OkResult;
import ru.spring.app.engine.api.response.PostsResponse;
import ru.spring.app.engine.api.response.SinglePostResponse;
import ru.spring.app.engine.api.response.StatisticsResponse;
import ru.spring.app.engine.api.response.UserResponse;
import ru.spring.app.engine.api.response.enums.PostStatus;
import ru.spring.app.engine.api.response.enums.ResultStatus;
import ru.spring.app.engine.api.response.enums.UserPostsStatus;
import ru.spring.app.engine.api.response.errors.AddPostError;
import ru.spring.app.engine.entity.Post;
import ru.spring.app.engine.entity.PostComments;
import ru.spring.app.engine.entity.PostVotes;
import ru.spring.app.engine.entity.Tag;
import ru.spring.app.engine.entity.Tag2Post;
import ru.spring.app.engine.entity.User;
import ru.spring.app.engine.entity.enums.ModerationStatus;
import ru.spring.app.engine.exception.AccessIsDeniedException;
import ru.spring.app.engine.exception.AddPostFailException;
import ru.spring.app.engine.exception.PostNotFoundException;
import ru.spring.app.engine.repository.PostRepository;
import ru.spring.app.engine.repository.PostVotesRepository;
import ru.spring.app.engine.repository.Tag2PostRepository;
import ru.spring.app.engine.repository.TagRepository;
import ru.spring.app.engine.repository.UserRepository;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostVotesRepository postVotesRepository;
    private final Tag2PostRepository tag2PostRepository;
    private final SettingsService settingsService;

    public PostsResponse getPosts(Integer offset, Integer limit, String mode) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        Page<Post> page = switch (PostStatus.valueOf(mode.toUpperCase())) {
            case POPULAR -> postRepository.getPostsOrderByCommentCount(nextPage);
            case BEST -> postRepository.getPostsOrderByLikeCount(nextPage);
            case EARLY -> postRepository.getOldPostsOrderByTime(nextPage);
            case RECENT -> postRepository.getPostsOrderByTime(nextPage);
        };
        return convertPagePostsToResponse(page);
    }

    public PostsResponse getPostsForModeration(Integer offset, Integer limit, String status) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        Page<Post> page = switch (ModerationStatus.valueOf(status.toUpperCase())) {
            case NEW -> postRepository.getNewPosts(nextPage);
            case ACCEPTED -> postRepository.getAcceptedPosts(nextPage);
            case DECLINED -> postRepository.getDeclinedPosts(nextPage);
        };
        return convertPagePostsToResponse(page);
    }

    public PostsResponse getUserPosts(Integer offset, Integer limit, String status, String email) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        Page<Post> page = switch (UserPostsStatus.valueOf(status.toUpperCase())) {
            case INACTIVE -> postRepository.getInactivePostsByUser(nextPage, email);
            case PENDING -> postRepository.getPendingPostsByUser(nextPage, email);
            case DECLINED -> postRepository.getDeclinedPostsByUser(nextPage, email);
            case PUBLISHED -> postRepository.getPublishedPostsByUser(nextPage, email);
        };
        return convertPagePostsToResponse(page);
    }

    public PostsResponse getPostsByUserRequest(Integer offset, Integer limit, String query) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        PostsResponse postsResponse = new PostsResponse();
        Page<Post> postsPage = postRepository.searchInText(query, nextPage);
        postsResponse.setCount(postsPage.getTotalElements());
        postsResponse.setPosts(postsPage.get().map(this::convertPostToSingleResponse).collect(Collectors.toList()));
        return postsResponse;
    }

    public PostsResponse getPostsOnDay(Integer offset, Integer limit, LocalDate date) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        PostsResponse postsResponse = new PostsResponse();
        List<Post> posts = postRepository.getPosts().stream()
                .filter(p -> p.getTime().getYear() == date.getYear()
                        && p.getTime().getMonthValue() == date.getMonthValue()
                        && p.getTime().getDayOfMonth() == date.getDayOfMonth()).collect(Collectors.toList());
        Page<Post> postsPage = new PageImpl<>(posts, nextPage, posts.size());
        postsResponse.setCount(postsPage.getTotalElements());
        postsResponse.setPosts(postsPage.get().map(this::convertPostToSingleResponse).collect(Collectors.toList()));
        return postsResponse;
    }

    public PostsResponse getPostsByTag(Integer offset, Integer limit, String tag) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        PostsResponse postsResponse = new PostsResponse();
        Page<Post> postsPage = postRepository.getPostsWithTag(tag, nextPage);
        postsResponse.setCount(postsPage.getTotalElements());
        postsResponse.setPosts(postsPage.get().map(this::convertPostToSingleResponse).collect(Collectors.toList()));
        return postsResponse;
    }

    public CalendarResponse getPostsCountInTheYear(Integer year) {
        return year == 0 ? convertMapToResponse() : convertMapToResponse(year);
    }

    public CurrentPostResponse getPostById(Long id) throws PostNotFoundException {
        if (postRepository.findById(id).isPresent()) {
            postRepository.updatePostInfo(postRepository.getPostById(id).getViewCount() + 1, id);
            return convertPostToCurrentPostResponse(postRepository.getPostById(id));
        } else {
            throw new PostNotFoundException("post with id: " + id + " not found");
        }
    }

    public AddPostResponse addNewPost(AddPostRequest request, Principal principal) throws AddPostFailException {
        AddPostResponse response = new AddPostResponse();
        Long userId = userRepository.getUserIdByEmail(principal.getName());
        if (addPostErrors(request).isEmpty()) {
            response.setResult(true);
            savePostFromRequest(request, userId);
        } else {
            response.setResult(false);
            response.setErrors(addPostErrors(request));
        }
        return response;
    }

    public AddPostResponse updatePost(long id, AddPostRequest request) {
        AddPostResponse response = new AddPostResponse();
        if (addPostErrors(request).isEmpty()) {
            response.setResult(true);
            editPostFromRequest(id, request);
        } else {
            response.setResult(false);
            response.setErrors(addPostErrors(request));
        }
        return response;
    }

    public StatisticsResponse getStatistics(String email) throws AccessIsDeniedException {
        if (!statisticIsPublicAndUserRoleModerator(email)) {
            throw new AccessIsDeniedException("access to statistic is denied");
        }
        StatisticsResponse response = new StatisticsResponse();
        response.setPostsCount(postRepository.findAll().size());
        response.setViewCount(postRepository.getTotalViewCount());
        response.setLikesCount(postRepository.getTotalLikesCount());
        response.setDislikesCount(postRepository.getTotalDislikesCount());
        response.setFirstPublication(postRepository.getPostsOrderByTime().get(0).getTime()
                .toEpochSecond(ZoneOffset.UTC));
        return response;
    }

    public StatisticsResponse getUserStatistics(String email) {
        StatisticsResponse response = new StatisticsResponse();
        User currentUser = userRepository.findByEmail(email).get();
        response.setPostsCount(postRepository.findAll().stream()
                .filter(post -> post.getUserId() == currentUser.getId()).count());
        response.setViewCount(postRepository.getViewCountOnUserPosts(currentUser.getId()));
        response.setLikesCount(postRepository.getLikesCountForUserPosts(currentUser.getId()));
        response.setDislikesCount(postRepository.getDislikesCountForUserPosts(currentUser.getId()));
        response.setFirstPublication(postRepository.getUsersPostsOrderByTime(currentUser.getId()).get(0).getTime()
                .toEpochSecond(ZoneOffset.UTC));
        return response;
    }

    public OkResult addLike(VoteRequest request, String email) {
        Optional<User> currentUser = userRepository.findByEmail(email);
        if (currentUser.isPresent() && isPostHasUserDislike(request.getPostId(), currentUser.get().getId())) {
            postVotesRepository.changeDislikeToLike(currentUser.get().getId(), request.getPostId());
            return new OkResult(ResultStatus.TRUE);
        } else if (currentUser.isPresent() && !isPostHasUserLike(request.getPostId(), currentUser.get().getId())) {
            postVotesRepository.addLike(request.getPostId(), LocalDate.now(), currentUser.get().getId());
            return new OkResult(ResultStatus.TRUE);
        } else {
            return new OkResult(ResultStatus.FALSE);
        }
    }

    public OkResult addDislike(VoteRequest request, String email) {
        Optional<User> currentUser = userRepository.findByEmail(email);
        if (currentUser.isPresent() && isPostHasUserLike(request.getPostId(), currentUser.get().getId())) {
            postVotesRepository.changeLikeToDislike(currentUser.get().getId());
            return new OkResult(ResultStatus.TRUE);
        } else if (currentUser.isPresent() && !isPostHasUserDislike(request.getPostId(), currentUser.get().getId())) {
            postVotesRepository.addDislike(request.getPostId(), LocalDate.now(), currentUser.get().getId());
            return new OkResult(ResultStatus.TRUE);
        } else {
            return new OkResult(ResultStatus.FALSE);
        }
    }

    public Boolean moderatePost(ModerationRequest request) {
        if (request.getDecision().equals("accept")) {
            postRepository.updatePostStatus(ModerationStatus.ACCEPTED.name(), request.getPostId());
            return true;
        } else if (request.getDecision().equals("decline")) {
            postRepository.updatePostStatus(ModerationStatus.DECLINED.name(), request.getPostId());
            return true;
        }
        return false;
    }

    private boolean isPostHasUserLike(Long postId, Long userId) {
        Optional<PostVotes> postVotes = postVotesRepository.findByUserId(postId, userId);
        return postVotes.isPresent() && postVotes.get().getValue() == 1;
    }

    private boolean isPostHasUserDislike(Long postId, Long userId) {
        Optional<PostVotes> postVotes = postVotesRepository.findByUserId(postId, userId);
        return postVotes.isPresent() && postVotes.get().getValue() == -1;
    }

    private boolean statisticIsPublicAndUserRoleModerator(String email) {
        return userRepository.findByEmail(email).get().getIsModerator() == 1
                && settingsService.getGlobalSettings().isStatisticsIsPublic();
    }

    private List<AddPostError> addPostErrors(AddPostRequest request) {
        List<AddPostError> errors = new ArrayList<>();
        if (request.getTitle().isEmpty() || request.getTitle().length() < 3) {
            AddPostError titleError = new AddPostError();
            titleError.setTitle("No title set");
            errors.add(titleError);
        } else if (request.getText().isEmpty() || request.getText().length() < 50) {
            AddPostError textError = new AddPostError();
            textError.setText("The text is too short ");
            errors.add(textError);
        }
        return errors;
    }

    private PostsResponse convertPagePostsToResponse(Page<Post> posts) {
        PostsResponse postsResponse = new PostsResponse();
        postsResponse.setCount(posts.getTotalElements());
        postsResponse.setPosts(posts.stream().map(this::convertPostToSingleResponse).collect(Collectors.toList()));
        return postsResponse;
    }

    private SinglePostResponse convertPostToSingleResponse(Post post) {
        SinglePostResponse postResponse = new SinglePostResponse();
        UserResponse userResponse = new UserResponse();
        Timestamp timestamp = new Timestamp(post.getTime().toEpochSecond(ZoneOffset.UTC));
        userResponse.setId(post.getUserId());
        userResponse.setName(postRepository.getNameFromPost(post.getUserId()));
        postResponse.setId(post.getId());
        postResponse.setTimestamp(timestamp.getTime());
        postResponse.setTitle(post.getTitle());
        postResponse.setText(post.getText());
        postResponse.setAnnounce(createAnnounce(post.getText()));
        postResponse.setLikeCount(postRepository.getVotesForPost(post.getId())
                .stream().filter(vote -> vote.getValue() == 1).count());
        postResponse.setDislikeCount(postRepository.getVotesForPost(post.getId())
                .stream().filter(vote -> vote.getValue() == -1).count());
        postResponse.setCommentCount(post.getPostComments().size());
        postResponse.setViewCount(post.getViewCount());
        postResponse.setUserResponse(userResponse);
        return postResponse;
    }

    private String createAnnounce(String text) {
        StringBuilder builder = new StringBuilder();
        String[] words = text.split(" ").length > 10 ?
                text.split(" ") : (String[]) List.of(text.substring(0, 10)).toArray();
        for (int i = 0; i < 10; i++) {
            words[i].replaceAll("\\<.+/\\>", " ");
            builder.append(words[i]).append(" ");
        }
        return builder.toString();
    }

    private CurrentPostResponse convertPostToCurrentPostResponse(Post post) {
        CurrentPostResponse postResponse = new CurrentPostResponse();
        CommentUserResponse userResponse = new CommentUserResponse();
        String[] tags = new String[post.getTags().size()];
        List<String> tagNameList = post.getTags().stream().map(Tag::getName).collect(Collectors.toList());
        tags = tagNameList.toArray(tags);
        userResponse.setId(post.getUserId());
        userResponse.setName(postRepository.getNameFromPost(post.getUserId()));
        postResponse.setId(post.getId());
        postResponse.setTimestamp(new Timestamp(post.getTime().toEpochSecond(ZoneOffset.UTC)).getTime());
        postResponse.setTitle(post.getTitle());
        postResponse.setText(post.getText());
        postResponse.setLikeCount(postRepository.getVotesForPost(post.getId())
                .stream().filter(vote -> vote.getValue() == 1).count());
        postResponse.setDislikeCount(postRepository.getVotesForPost(post.getId())
                .stream().filter(vote -> vote.getValue() == -1).count());
        postResponse.setViewCount(post.getViewCount());
        postResponse.setUserResponse(userResponse);
        postResponse.setActive(post.getIsActive() == 1);
        postResponse.setTags(tags);
        postResponse.setComments(convertPostCommentsToResponse(post.getId()));
        return postResponse;
    }

    private List<CommentResponse> convertPostCommentsToResponse(Long postId) {
        List<CommentResponse> responses = new ArrayList<>();
        for (PostComments pc : postRepository.getCommentsForPost(postId)) {
            User currentUser = userRepository.getUsersById(pc.getUserId());
            CommentResponse commentResponse = new CommentResponse();
            commentResponse.setId(pc.getId());
            commentResponse.setText(pc.getText());
            commentResponse.setTimestamp(pc.getTime().toEpochSecond(ZoneOffset.UTC));
            commentResponse.setUser(new UserResponse(
                    currentUser.getId(),
                    currentUser.getName(),
                    currentUser.getPhoto()));
            responses.add(commentResponse);
        }
        return responses;
    }

    private CalendarResponse convertMapToResponse() {
        Map<String, Long> calendarMap = new HashMap<>();
        CalendarResponse calendarResponse = new CalendarResponse();
        postRepository.getPostsCountInYear().forEach(el ->
                calendarMap.put(el.get("date"), Long.parseLong(el.get("amount_posts"))));
        calendarResponse.setYears(postRepository.getYears());
        calendarResponse.setPosts(calendarMap);
        return calendarResponse;
    }

    private CalendarResponse convertMapToResponse(Integer year) {
        Map<String, Long> calendarMap = new HashMap<>();
        CalendarResponse calendarResponse = new CalendarResponse();
        postRepository.getPostsInYear(year).forEach(el ->
                calendarMap.put(el.get("date"), Long.parseLong(el.get("amount_posts"))));
        calendarResponse.setYears(postRepository.getYears());
        calendarResponse.setPosts(calendarMap);
        return calendarResponse;
    }

    private void savePostFromRequest(AddPostRequest request, Long userId) throws AddPostFailException {
        LocalDateTime time = setDateToPost(request);
        if (postRepository.getPostByText(request.getText()).isEmpty()) {
            postRepository.savePost(request.getIsActive(), request.getTitle(), request.getText(), time, userId);
            saveTagsForPost(request, postRepository.getPostByText(request.getText()).get().getId());
        } else {
            throw new AddPostFailException("similar post already exists");
        }
    }

    private void editPostFromRequest(long id, AddPostRequest request) {
        LocalDateTime time = setDateToPost(request);
        postRepository.updatePost(request.getIsActive(), request.getTitle(), request.getText(), time, id);
        saveTagsForPost(request, postRepository.getPostByText(request.getText()).get().getId());
    }

    private void saveTagsForPost(AddPostRequest request, Long postId) {
        List<String> requestTags = request.getTags();
        for (String t : requestTags) {
            if (tagRepository.getTagByName(t).isEmpty()) {
                tagRepository.save(new Tag(null, t));
            }
            long tagId = tagRepository.getTagByName(t).get().getId();
            tag2PostRepository.save(new Tag2Post(null, postId, tagId));
        }
    }

    private LocalDateTime setDateToPost(AddPostRequest request) {
        return LocalDateTime.now()
                .isAfter(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneId.systemDefault())) ?
                LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp() * 1000), ZoneId.systemDefault()) :
                LocalDateTime.now();
    }
}
