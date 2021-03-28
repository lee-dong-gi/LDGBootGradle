package com.web.LDGBootGradle.repository;

import com.web.LDGBootGradle.model.Comment;
import com.web.LDGBootGradle.model.UploadFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByBoardIdOrderByCommentIdDesc(Long id);


}
