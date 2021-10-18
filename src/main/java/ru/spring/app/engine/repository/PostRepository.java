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
import ru.spring.app.engine.entity.enums.ModerationStatus;

import javax.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Repository
public interface PostRepository extends JpaRepository<Post, Long> {

    Post getPostsById(@Param("id") Long id);

    Post getPostByText(String text);

    @Query(value =
            "SELECT * FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= CURRENT_DATE " +
            "AND EXTRACT (DAY from time) = :day " +
            "AND EXTRACT (MONTH from time) = :month " +
            "AND EXTRACT (YEAR from time) = :year " +
            "ORDER BY time DESC ", nativeQuery = true)
    Page<Post> getPostsPerDay(@Param("day") Integer day, @Param("month") Integer month,
                              @Param("year") Integer year, Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_DATE " +
            "ORDER BY p.postComments.size DESC")
    Page<Post> getPostsOrderByCommentCount(Pageable pageable);

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_DATE " +
            "ORDER BY p.postVotesLikes.size DESC")
    Page<Post> getPostsOrderByLikeCount(Pageable pageable);

    @Query("FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_DATE " +
            "ORDER BY p.time DESC")
    Page<Post> getPostsOrderByTime(Pageable pageable);

    @Query("FROM Post p " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= CURRENT_DATE " +
            "ORDER BY p.time")
    Page<Post> getOldPostsOrderByTime(Pageable pageable);

    @Query(value = "SELECT * " +
            "FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= current_date " +
            "AND text LIKE %:query% " +
            "ORDER BY time DESC", nativeQuery = true)
    Page<Post> searchInText(@Param("query") String query, Pageable pageable);

    @Query(value = "SELECT * FROM posts " +
            "JOIN tag2post ON posts.id = tag2post.post_id " +
            "JOIN tags ON tags.id = tag2post.tag_id " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' " +
            "AND time <= current_date AND name = :tag", nativeQuery = true)
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

    @Query(value = "SELECT * FROM posts " +
            "LEFT JOIN users on posts.user_id = users.id " +
            "WHERE is_active = -1 and email = :email", nativeQuery = true)
    Page<Post> getInactivePostsByUser(Pageable pageable, @Param("email") String email);

    @Query(value = "SELECT * FROM posts " +
            "LEFT JOIN users on posts.user_id = users.id " +
            "WHERE is_active = 1 AND moderation_status = 'NEW' AND email = :email", nativeQuery = true)
    Page<Post> getPendingPostsByUser(Pageable pageable, @Param("email") String email);

    @Query(value = "SELECT * FROM posts\n" +
            "LEFT JOIN users on posts.user_id = users.id " +
            "WHERE is_active = 1 AND moderation_status = 'DECLINED' AND email = :email", nativeQuery = true)
    Page<Post> getDeclinedPostsByUser(Pageable pageable, @Param("email") String email);

    @Query(value = "SELECT * FROM posts " +
            "LEFT JOIN users on posts.user_id = users.id " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND email = :email", nativeQuery = true)
    Page<Post> getPublishedPostsByUser(Pageable pageable, @Param("email") String email);

    @Query("SELECT pv " +
            "FROM PostVotes pv " +
            "WHERE pv.postId = :id")
    List<PostVotes> getVotesForPost(@Param("id") Long id);

    @Query(value = "SELECT DISTINCT name " +
            "FROM users " +
            "LEFT JOIN posts on posts.user_id = users.id " +
            "WHERE users.id = :id ", nativeQuery = true)
    String getNameFromPost(@Param("id") Long id);

    @Query("SELECT pc " +
            "FROM PostComments pc " +
            "WHERE pc.postId = :id")
    List<PostComments> getCommentsForPost(@Param("id") Long id);

    @Query(value = "SELECT CAST ((time) AS VARCHAR(255)) as date, CAST (COUNT(*) AS varchar(255)) as amount_posts " +
            "FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= current_date " +
            "GROUP BY date " +
            "ORDER BY amount_posts DESC", nativeQuery = true)
    List<Map<String, String>> getPostsCountInYear();

    @Query(value = "SELECT CAST ((time) AS VARCHAR(255)) as date, CAST (COUNT(*) AS varchar(255)) as amount_posts " +
            "FROM posts " +
            "WHERE EXTRACT(YEAR from time) = :year  AND moderation_status = 'ACCEPTED' AND time <= current_date " +
            "GROUP BY date " +
            "ORDER BY amount_posts DESC", nativeQuery = true)
    List<Map<String, String>> getPostsInYear(@Param("year") Integer year);


    @Query(value = "SELECT CAST (date_part('year', time) AS INTEGER) as year " +
            "FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= current_date " +
            "GROUP BY year " +
            "ORDER BY year", nativeQuery = true)
    List<Integer> getYears();

    @Transactional
    @Modifying
    @Query("UPDATE Post p set p.viewCount = :view_count WHERE p.id = :id")
    void updatePostInfo(@Param("view_count") Integer viewCount, @Param("id") Long postId);

    @Transactional
    @Modifying
    @Query(value = "INSERT INTO posts (is_active, moderation_status, moderator_id, text, time, user_id, view_count) " +
            "VALUES (:is_active, 'NEW', -1, :text, :time , :id, 0)", nativeQuery = true)
    void savePost(@Param("is_active") Integer isActive, @Param("text") String text,
                  @Param("time") LocalDateTime time, @Param("id") Long userId);

    @Transactional
    @Modifying
    @Query("UPDATE Post p SET p.isActive = :is_active, p.text = :text, p.time = :time")
    void updatePost(@Param("is_active") Integer isActive, @Param("text") String text,
                    @Param("time") LocalDateTime time);

    @Transactional
    @Modifying
    @Query(value = "UPDATE posts set (moderation_status = :moderation_status)::mod_status WHERE id = :id", nativeQuery = true)
    void updatePostStatus(@Param("moderation_status") String moderationStatus, @Param("id") Long postId);

    @Query(value = "SELECT SUM (view_count) FROM posts", nativeQuery = true)
    long getTotalViewCount();

    @Query(value = "SELECT SUM (view_count) FROM posts WHERE user_id = :id", nativeQuery = true)
    long getViewCountOnUserPosts(@Param("id") Long id);

    @Query(value = "SELECT COUNT(id) " +
            "FROM post_votes " +
            "WHERE value = 1", nativeQuery = true)
    long getTotalLikesCount();

    @Query(value = "SELECT COUNT(id) " +
            "FROM post_votes " +
            "WHERE value = 1 AND user_id = :id", nativeQuery = true)
    long getLikesCountForUserPosts(@Param("id") Long id);

    @Query(value = "SELECT COUNT(id) " +
            "FROM post_votes " +
            "WHERE value = -1", nativeQuery = true)
    long getTotalDislikesCount();

    @Query(value = "SELECT COUNT(id) " +
            "FROM post_votes " +
            "WHERE value = -1 AND user_id = :id", nativeQuery = true)
    long getDislikesCountForUserPosts(@Param("id") Long id);

    @Query("SELECT p " +
            "FROM Post p " +
            "ORDER BY p.time")
    List<Post> getPostsOrderByTime();

    @Query("SELECT p " +
            "FROM Post p " +
            "WHERE p.userId = :id " +
            "ORDER BY p.time")
    List<Post> getUsersPostsOrderByTime(@Param("id") Long id);

}
