package ru.spring.app.engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import ru.spring.app.engine.api.response.TagWithCount;
import ru.spring.app.engine.entity.Tag;

import java.util.List;
import java.util.Optional;
import java.util.Set;

@Repository
public interface TagRepository extends JpaRepository<Tag, Long> {

    Optional<Tag> getTagByName(String name);

    List<Tag> getTagsByName(String name);

    @Query("SELECT new ru.spring.app.engine.api.response.TagWithCount(t.name, size(t.posts)) " +
            "FROM Tag AS t JOIN Tag2Post tp ON tp.tagId = t.id JOIN Post p ON tp.postId = p.id " +
            "WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' ORDER BY t.posts.size DESC")
    Set<TagWithCount> getTagsWithCount();

    @Query("SELECT count (p) " +
            "FROM Post p WHERE p.isActive = 1 AND p.moderationStatus = 'ACCEPTED' AND p.time <= current_date")
    Long getPostsCount();

}
