package ru.spring.app.engine.entity;

import lombok.Data;
import lombok.NoArgsConstructor;
import ru.spring.app.engine.entity.enums.Role;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Table;
import java.util.Date;

@Data
@Entity
@NoArgsConstructor
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "serial")
    private long id;

    @Column(nullable = false)
    private int isModerator;

    @Column(nullable = false)
    private Date regTime;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, columnDefinition = "text")
    private String email;

    @Column(nullable = false, columnDefinition = "text")
    private String password;

    private String code;

    @Column(columnDefinition = "text")
    private String photo;

    public Role getRole() {
        return isModerator == 1 ? Role.MODERATOR : Role.USER;
    }
}
