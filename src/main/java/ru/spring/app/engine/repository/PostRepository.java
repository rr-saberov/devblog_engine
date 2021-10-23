package ru.spring.app.engine.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.spring.app.engine.entity.Post;
import ru.spring.app.engine.entity.PostComments;
import ru.spring.app.engine.entity.PostVotes;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Post getPostById(@Param("id") Long id);

    Optional<Post> getPostByText(String text);

    @Query("SELECT p FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_TIMESTAMP " +
            "ORDER BY p.time DESC")
    List<Post> getPosts();

    @Query("SELECT p FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_TIMESTAMP " +
            "ORDER BY p.postComments.size DESC")
    Page<Post> getPostsOrderByCommentCount(Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_TIMESTAMP " +
            "ORDER BY p.postVotesLikes.size DESC")
    Page<Post> getPostsOrderByLikeCount(Pageable pageable);

    @Query("FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_TIMESTAMP " +
            "ORDER BY p.time DESC")
    Page<Post> getPostsOrderByTime(Pageable pageable);

    @Query("FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_TIMESTAMP " +
            "ORDER BY p.time")
    Page<Post> getOldPostsOrderByTime(Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_TIMESTAMP " +
            "AND p.text LIKE %:query% " +
            "ORDER BY p.time DESC")
    Page<Post> searchInText(@Param("query") String query, Pageable pageable);

    @Query("SELECT p FROM Post p " +
            "JOIN Tag2Post tp ON p.id = tp.postId " +
            "JOIN Tag t ON t.id = tp.tagId " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_DATE AND t.name = :tag")
    Page<Post> getPostsWithTag(@Param("tag") String tag, Pageable nextPage);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'NEW' " +
            "ORDER BY p.time DESC")
    Page<Post> getNewPosts(Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' " +
            "ORDER BY p.time DESC")
    Page<Post> getAcceptedPosts(Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'DECLINED' " +
            "ORDER BY p.time DESC")
    Page<Post> getDeclinedPosts(Pageable pageable);

    @Query("SELECT p FROM  Post p " +
            "JOIN User u ON p.userId = u.id " +
            "WHERE p.isActive = -1 AND u.email = :email")
    Page<Post> getInactivePostsByUser(Pageable pageable, @Param("email") String email);

    @Query("SELECT p FROM Post p " +
            "JOIN User u ON p.userId = u.id " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'NEW' AND u.email = :email")
    Page<Post> getPendingPostsByUser(Pageable pageable, @Param("email") String email);

    @Query("SELECT p FROM Post p " +
            "JOIN User u ON p.userId = u.id " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'DECLINED' AND u.email = :email")
    Page<Post> getDeclinedPostsByUser(Pageable pageable, @Param("email") String email);

    @Query("SELECT p FROM Post p " +
            "JOIN User u ON p.userId = u.id " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND u.email = :email")
    Page<Post> getPublishedPostsByUser(Pageable pageable, @Param("email") String email);

    @Query("SELECT pv FROM PostVotes pv WHERE pv.postId = :id")
    List<PostVotes> getVotesForPost(@Param("id") Long id);

    @Query("SELECT DISTINCT u.name " +
            "FROM User u LEFT JOIN Post p ON p.userId = u.id WHERE u.id = :id")
    String getNameFromPost(@Param("id") Long id);

    @Query("SELECT pc FROM PostComments pc WHERE pc.postId = :id")
    List<PostComments> getCommentsForPost(@Param("id") Long id);

    @Query(value = "SELECT to_char(time, 'YYYY-MM-dd') as date, CAST (COUNT(*) AS varchar(255)) as amount_posts " +
            "FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= CURRENT_TIMESTAMP " +
            "GROUP BY date " +
            "ORDER BY amount_posts DESC", nativeQuery = true)
    List<Map<String, String>> getPostsCountInYear();

    @Query(value = "SELECT to_char(time, 'YYYY-MM-dd') as date, CAST (COUNT(*) AS varchar(255)) as amount_posts " +
            "FROM posts " +
            "WHERE EXTRACT(YEAR from time) = :year AND moderation_status = 'ACCEPTED' AND time <= CURRENT_TIMESTAMP " +
            "GROUP BY date " +
            "ORDER BY amount_posts DESC", nativeQuery = true)
    List<Map<String, String>> getPostsInYear(@Param("year") Integer year);


    @Query(value = "SELECT CAST (date_part('year', time) AS INTEGER) as year FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= CURRENT_TIMESTAMP " +
            "GROUP BY year ORDER BY year",
            nativeQuery = true)
    List<Integer> getYears();

    @Transactional
    @Modifying
    @Query("UPDATE Post p set p.viewCount = :view_count WHERE p.id = :id")
    void updatePostInfo(@Param("view_count") Integer viewCount, @Param("id") Long postId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO posts (is_active, moderation_status, moderator_id, title, text, time, user_id, view_count) " +
            "VALUES (:is_active, 'NEW', -1, :title, :text, :time , :id, 0)", nativeQuery = true)
    void savePost(@Param("is_active") Integer isActive, @Param("title") String title, @Param("text") String text,
                  @Param("time") LocalDateTime time, @Param("id") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.isActive = :is_active, p.title = :title,  p.text = :text, p.time = :time WHERE p.id = :id")
    void updatePost(@Param("is_active") Integer isActive, @Param("title") String title,
                    @Param("text") String text, @Param("time") LocalDateTime time, @Param("id") long id);

    @Transactional
    @Modifying
    @Query(value = "UPDATE posts set moderation_status = CAST(:moderation_status AS mod_status) WHERE id = :id",
            nativeQuery = true)
    void updatePostStatus(@Param("moderation_status") String moderationStatus, @Param("id") Long postId);

    @Query("SELECT SUM (p.viewCount) FROM Post p")
    long getTotalViewCount();

    @Query("SELECT SUM (p.viewCount) FROM Post p WHERE p.userId = :id")
    long getViewCountOnUserPosts(@Param("id") Long id);

    @Query("SELECT COUNT (pv.id) FROM PostVotes pv WHERE pv.value = 1")
    long getTotalLikesCount();

    @Query("SELECT COUNT (pv.id) FROM PostVotes pv WHERE pv.value = 1 AND pv.userId = :id")
    long getLikesCountForUserPosts(@Param("id") Long id);

    @Query("SELECT COUNT (pv.id) FROM PostVotes pv WHERE pv.value = -1")
    long getTotalDislikesCount();

    @Query("SELECT COUNT (pv.id) FROM PostVotes pv WHERE pv.value = -1 AND pv.userId = :id")
    long getDislikesCountForUserPosts(@Param("id") Long id);

    @Query("SELECT p FROM Post p ORDER BY p.time")
    List<Post> getPostsOrderByTime();

    @Query("SELECT p FROM Post p WHERE p.userId = :id ORDER BY p.time")
    List<Post> getUsersPostsOrderByTime(@Param("id") Long id);

}
