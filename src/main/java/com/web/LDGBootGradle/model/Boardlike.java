package com.web.LDGBootGradle.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Data
@EntityListeners(AuditingEntityListener.class)
public class Boardlike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long boardlikeId;


    @Column(nullable = false)
    private Long boardId;

    @Column(nullable = false)
    private Long userId;
}
