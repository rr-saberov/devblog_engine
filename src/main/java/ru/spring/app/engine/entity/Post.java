package ru.spring.app.engine.entity;

import lombok.Data;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.hibernate.annotations.Where;
import ru.spring.app.engine.entity.enums.ModerationStatus;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@Table(name = "posts")
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "serial")
    private long id;

    @Column(nullable = false)
    private int isActive;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private ModerationStatus moderationStatus;

    private int moderatorId;

    @Column(nullable = false)
    private long userId;

    @Column(nullable = false)
    private LocalDateTime time;

    @Column(nullable = false, columnDefinition = "text")
    private String text;

    @Column(nullable = false)
    private int viewCount;

    @ManyToMany(cascade = CascadeType.ALL)
    @JoinTable(
            name = "tag2post",
            joinColumns = @JoinColumn(name = "post_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private List<Tag> tags;

    @OneToMany
    @JoinColumn(name = "postId")
    @LazyCollection(LazyCollectionOption.EXTRA)
    List<PostComments> postComments;

    @OneToMany
    @JoinColumn(name = "postId")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "value = 1")
    List<PostVotes> postVotesLikes;

    @OneToMany
    @JoinColumn(name = "postId")
    @LazyCollection(LazyCollectionOption.EXTRA)
    @Where(clause = "value = -1")
    List<PostVotes> postVotesDislikes;

}