package ru.spring.app.engine.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import ru.spring.app.engine.api.request.AddPostRequest;
import ru.spring.app.engine.api.response.*;
import ru.spring.app.engine.api.response.errors.AddPostError;
import ru.spring.app.engine.entity.Post;
import ru.spring.app.engine.entity.Tag;
import ru.spring.app.engine.entity.Tag2Post;
import ru.spring.app.engine.entity.User;
import ru.spring.app.engine.entity.enums.ModerationStatus;
import ru.spring.app.engine.exceptions.AccessIsDeniedException;
import ru.spring.app.engine.exceptions.PostNotFoundException;
import ru.spring.app.engine.repository.*;

import java.security.Principal;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final UserRepository userRepository;
    private final TagRepository tagRepository;
    private final PostVotesRepository postVotesRepository;
    private final Tag2PostRepository tag2PostRepository;

    public PostService(PostRepository postRepository, UserRepository userRepository,TagRepository tagRepository,
                       PostVotesRepository postVotesRepository, Tag2PostRepository tag2PostRepository) {
        this.postRepository = postRepository;
        this.userRepository = userRepository;
        this.tagRepository = tagRepository;
        this.postVotesRepository = postVotesRepository;
        this.tag2PostRepository = tag2PostRepository;
    }

    public PostsResponse getPosts(Integer offset, Integer limit, String mode) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        Page<Post> page = switch (mode) {
            case "popular" -> postRepository.getPostsOrderByCommentCount(nextPage);
            case "best" -> postRepository.getPostsOrderByLikeCount(nextPage);
            case "early" -> postRepository.getOldPostsOrderByTime(nextPage);
            default -> postRepository.getPostsOrderByTime(nextPage);
        };
        return convertPagePostsToResponse(page);
    }

    public PostsResponse getPostsForModeration(Integer offset, Integer limit, String status) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        Page<Post> page = switch (status) {
            case "new" -> postRepository.getNewPosts(nextPage);
            case "accepted" -> postRepository.getAcceptedPosts(nextPage);
            case "declined" -> postRepository.getDeclinedPosts(nextPage);
            default -> postRepository.getPostsOrderByTime(nextPage);
        };
        return convertPagePostsToResponse(page);
    }

    public PostsResponse getUserPosts(Integer offset, Integer limit, String status, String email) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        Page<Post> page = switch (status) {
            case "inactive" -> postRepository.getInactivePostsByUser(nextPage, email);
            case "pending" -> postRepository.getPendingPostsByUser(nextPage, email);
            case "declined" -> postRepository.getDeclinedPostsByUser(nextPage, email);
            default -> postRepository.getPublishedPostsByUser(nextPage, email);
        };
        return convertPagePostsToResponse(page);
    }

    public PostsResponse getPostsByUserRequest(Integer offset, Integer limit, String query) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        PostsResponse postsResponse = new PostsResponse();
        Page<Post> postsPage =
                postRepository.searchInText(query, nextPage);
        postsResponse.setCount(postsPage.getTotalElements());
        postsResponse.setPosts(postsPage.get().map(this::convertPostToSingleResponse).collect(Collectors.toList()));
        return postsResponse;
    }

    public PostsResponse getPostsOnDay(Integer offset, Integer limit, LocalDateTime date) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        PostsResponse postsResponse = new PostsResponse();
        Page<Post> postsPage =
                postRepository.getPostsPerDay(date, nextPage);
        postsResponse.setCount(postsPage.getTotalElements());
        postsResponse.setPosts(postsPage.get().map(this::convertPostToSingleResponse).collect(Collectors.toList()));
        return postsResponse;
    }

    public PostsResponse getPostsByTag(Integer offset, Integer limit, String tag) {
        Pageable nextPage = PageRequest.of(offset / limit, limit);
        PostsResponse postsResponse = new PostsResponse();
        Page<Post> postsPage =
                postRepository.getPostsWithTag(tag, nextPage);
        postsResponse.setCount(postsPage.getTotalElements());
        postsResponse.setPosts(postsPage.get().map(this::convertPostToSingleResponse).collect(Collectors.toList()));
        return postsResponse;
    }

    public CalendarResponse getPostsCountInTheYear(Integer year) {
        CalendarResponse calendarResponse;
        if (year == 0) {
            calendarResponse = convertMapToResponse();
        } else {
            calendarResponse = convertMapToResponse(year);
        }
        return calendarResponse;
    }

    public CurrentPostResponse getPostById(Long id) throws PostNotFoundException {
        if (postRepository.findById(id).isPresent()) {
            postRepository.updatePostInfo(postRepository.getPostsById(id).getViewCount() + 1, id);
            return convertPostToCurrentPostResponse(postRepository.getPostsById(id));
        } else {
            throw new PostNotFoundException("post with id: " + id + " not found");
        }
    }

    public AddPostResponse addNewPost(AddPostRequest request, Principal principal) {
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

    public AddPostResponse updatePost(Long id, AddPostRequest request) {
        AddPostResponse response = new AddPostResponse();
        if (addPostErrors(request).isEmpty()) {
            response.setResult(true);
            savePostFromRequest(request, id);
        } else {
            response.setResult(false);
            response.setErrors(addPostErrors(request));
        }
        return response;
    }

    public StatisticsResponse getStatistics(String email) throws AccessIsDeniedException {
        if (userRepository.findByEmail(email).get().getIsModerator() == 1) {
            StatisticsResponse response = new StatisticsResponse();
            response.setPostsCount(postRepository.findAll().size());
            response.setViewCount(postRepository.getTotalViewCount());
            response.setLikesCount(postRepository.getTotalLikesCount());
            response.setDislikesCount(postRepository.getTotalDislikesCount());
            response.setFirstPublication(postRepository.getPostsOrderByTime().get(0).getTime()
                    .toEpochSecond(ZoneOffset.UTC));
            return response;
        } else {
            throw new AccessIsDeniedException("access to statistic is denied");
        }
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

    public Boolean addLike(Long postId, String email) {
        User currentUser = userRepository.findByEmail(email).get();
        if (postVotesRepository.findByUserId(currentUser.getId()).isPresent()) {
            if (postVotesRepository.findByUserId(currentUser.getId()).get().getValue() == -1) {
                postVotesRepository.changeDislikeToLike(currentUser.getId());
                return true;
            } else return postVotesRepository.findByUserId(currentUser.getId()).get().getValue() != 1;
        } else {
            postVotesRepository.addLike(postId, new Date(System.currentTimeMillis()), currentUser.getId());
            return true;
        }
    }

    public Boolean addDislike(Long postId, String email) {
        User currentUser = userRepository.findByEmail(email).get();
        if (postVotesRepository.findByUserId(currentUser.getId()).isPresent()) {
            if (postVotesRepository.findByUserId(currentUser.getId()).get().getValue() == 1) {
                postVotesRepository.changeLikeToDislike(currentUser.getId());
                return true;
            } else return postVotesRepository.findByUserId(currentUser.getId()).get().getValue() != -1;
        } else {
            postVotesRepository.addDislike(postId, new Date(System.currentTimeMillis()), currentUser.getId());
            return true;
        }
    }

    public Boolean moderatePost(Long id, String decision) {
        if (decision.equals("accept")) {
            postRepository.updatePostStatus(ModerationStatus.ACCEPTED.name(), id);
            return true;
        } else if (decision.equals("decline")) {
            postRepository.updatePostStatus("DECLINED", id);
            return true;
        }
        return false;
    }

    private List<AddPostError> addPostErrors(AddPostRequest request) {
        List<AddPostError> errors = new ArrayList<>();
        if (request.getTitle().isEmpty() || request.getTitle().length() < 5) {
            AddPostError titleError = new AddPostError("title", "Заголовок не установлен");
            errors.add(titleError);
        } else if (request.getText().isEmpty() || request.getText().length() < 50) {
            AddPostError textError = new AddPostError("text", "текс слишком короткий");
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
        postResponse.setTitle(post.getText());
        postResponse.setAnnounce(post.getText().substring(Math.min(0, 25)));
        postResponse.setLikeCount(postRepository.getVotesForPost(post.getId())
                .stream().filter(vote -> vote.getValue() == 1).count());
        postResponse.setDislikeCount(postRepository.getVotesForPost(post.getId())
                .stream().filter(vote -> vote.getValue() == -1).count());
        postResponse.setCommentCount(post.getPostComments().size());
        postResponse.setViewCount(post.getViewCount());
        postResponse.setUserResponse(userResponse);
        return postResponse;
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
        postResponse.setTitle(post.getText());
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
        postRepository.getCommentsForPost(postId).forEach(postComments -> {
            User currentUser = userRepository.getUsersById(postComments.getUserId());
            CommentResponse commentResponse = new CommentResponse();
            commentResponse.setId(postComments.getId());
            commentResponse.setText(postComments.getText());
            commentResponse.setTimestamp(postComments.getTime().toEpochSecond(ZoneOffset.UTC));
            commentResponse.setUser(new UserResponse(
                    currentUser.getId(),
                    currentUser.getName(),
                    currentUser.getPhoto()));
            responses.add(commentResponse);
        });
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

    private void savePostFromRequest(AddPostRequest request, Long userId) {
        Post post = new Post();
        post.setId(new Random().nextLong());
        post.setText(request.getText());
        post.setUserId(userId);
        post.setTime(setDateToPost(request));
        postRepository.save(post);

        request.getTags().forEach(el -> {
            if (tagRepository.getTagByName(el).getName().isEmpty()) {
                Tag tag = new Tag();
                tag.setName(el);
                tagRepository.save(tag);
            }
            tag2PostRepository
                    .save(new Tag2Post(new Random().nextLong(), post.getId(), tagRepository.getTagByName(el).getId()));
        });
    }

    private LocalDateTime setDateToPost(AddPostRequest request) {
        LocalDateTime date = LocalDateTime.now();
        if (LocalDateTime.now().isAfter(LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneId.systemDefault()))) {
            date = LocalDateTime.ofInstant(Instant.ofEpochMilli(request.getTimestamp()), ZoneId.systemDefault());
        }
        return date;
    }
}
