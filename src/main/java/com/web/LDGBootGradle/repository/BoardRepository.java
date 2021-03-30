package com.web.LDGBootGradle.repository;

import com.web.LDGBootGradle.model.Board;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.util.List;

public interface BoardRepository extends JpaRepository<Board, Long> {

    List<Board> findByTitle(String title);

    List<Board> findByTitleOrContent(String title,String content);

    List<Board> findByTitleAndContent(String title,String content);

    List<Board> findByUserId(Long userId);

    /*
        기존 : findByTitleContainingOrContentContaining 
        OrderByIdDesc : id 컬럼 역순출력
     */
    Page<Board> findByTitleContainingOrContentContainingOrderByIdDesc(String title, String content, Pageable pageable);

    //다음글
    @Nullable
    @Query(value = "SELECT MIN(id) FROM board WHERE id > ?1", nativeQuery = true)
    Long findByNextObj(Long id);

    //이전글
    @Nullable
    @Query(value = "SELECT MAX(id) FROM board WHERE id < ?1", nativeQuery = true)
    Long findByPrevObj(Long id);

    //좋아요 rank
    @Nullable
    @Query(value = "SELECT *,RANK() OVER(ORDER BY boardlike DESC) 'rank' FROM board", nativeQuery = true)
    List<Board> findByRank();

}
