package com.web.LDGBootGradle.repository;

import com.web.LDGBootGradle.model.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import javax.persistence.OrderBy;
import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByTitle(String title);

    List<Board> findByTitleOrContent(String title,String content);

    List<Board> findByTitleAndContent(String title,String content);

    /*
        기존 : findByTitleContainingOrContentContaining 
        OrderByIdDesc : id 컬럼 역순출력
     */
    Page<Board> findByTitleContainingOrContentContainingOrderByIdDesc(String title, String content, Pageable pageable);

}
