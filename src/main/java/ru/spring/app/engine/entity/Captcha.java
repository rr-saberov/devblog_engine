package ru.spring.app.engine.entity;

import lombok.Data;
import lombok.NoArgsConstructor;

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
@Table(name = "captcha_codes")
public class Captcha {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false, columnDefinition = "serial")
    private long id;

    @Column(nullable = false)
    private Date time;

    @Column(nullable = false)
    private String code;

    @Column(nullable = false)
    private String secretCode;

    public Captcha(Date time, String code, String secretCode) {
        this.time = time;
        this.code = code;
        this.secretCode = secretCode;
    }
}
