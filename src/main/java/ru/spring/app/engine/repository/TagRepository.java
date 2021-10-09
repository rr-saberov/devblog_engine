package ru.spring.app.engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.spring.app.engine.api.response.TagWithCount;
import ru.spring.app.engine.entity.Tag;

import java.util.List;
import java.util.Optional;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> getTagByName(String name);

    @Query(value = "SELECT * " +
            "FROM tags " +
            "JOIN tag2post ON tags.id = tag2post.tag_id " +
            "JOIN posts ON posts.id = tag2post.post_id " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= current_date " +
            "ORDER BY (SELECT DISTINCT COUNT (*))", nativeQuery = true)
    List<Tag> getTagsOrderByPopularity();

    @Query("SELECT new ru.spring.app.engine.api.response.TagWithCount(t.name, size(t.posts)) " +
            "FROM Tag AS t ORDER BY t.posts.size DESC")
    List<TagWithCount> getTagsWithCount();

    @Query(value = "SELECT COUNT (*) " +
            "FROM posts " +
            "JOIN tag2post ON posts.id = tag2post.post_id " +
            "JOIN tags ON tags.id = tag2post.tag_id " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= current_date AND name = :tag", nativeQuery = true)
    Long getPostsCountWithTag(@Param("tag") String tag);

    @Query(value = "SELECT COUNT (*) " +
            "FROM posts " +
            "WHERE is_active = 1 AND moderation_status = 'ACCEPTED' AND time <= current_date", nativeQuery = true)
    Long getPostsCount();

}
