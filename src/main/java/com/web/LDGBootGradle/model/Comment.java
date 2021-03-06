package com.web.LDGBootGradle.model;

import lombok.Data;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Entity
@EntityListeners(AuditingEntityListener.class)
public class Comment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long commentId;

    @Column(nullable = false)
    private String commentContent;

    @Column(nullable = false)
    private Long userId;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime writeDate;

    @LastModifiedDate
    private LocalDateTime modifyDate;

    @Column(nullable = false)
    private Long boardId;

    @Transient
    private String username;
    
    //댓글 수정, 삭제 시 본인여부 확인 
    @Transient
    private String commentselfcheck;
}
