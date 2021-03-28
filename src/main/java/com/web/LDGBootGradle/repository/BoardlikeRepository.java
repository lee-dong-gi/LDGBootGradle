package com.web.LDGBootGradle.repository;

import com.web.LDGBootGradle.model.Board;
import com.web.LDGBootGradle.model.Boardlike;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.lang.Nullable;

import java.util.List;

public interface BoardlikeRepository extends JpaRepository<Boardlike, Long> {
    @Nullable
    @Query(value = "SELECT * FROM boardlike " +
            "WHERE board_id=?1", nativeQuery = true)
    List<Boardlike> findByLikeContainingBoardId(Long boardId);

    //다음글
    @Nullable
    @Query(value = "SELECT * FROM boardlike " +
            "WHERE user_id=?1 " +
            "AND board_id=?2", nativeQuery = true)
    Boardlike findByLikeContainingUserIdContainingBoardId(Long userId, Long boardId);
}
