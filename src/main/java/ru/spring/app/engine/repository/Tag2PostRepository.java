package ru.spring.app.engine.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.spring.app.engine.entity.Tag2Post;

@Repository
public interface Tag2PostRepository extends JpaRepository<Tag2Post, Long> {

}
