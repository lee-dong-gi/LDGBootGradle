package com.web.LDGBootGradle.service;

import com.web.LDGBootGradle.model.Board;
import com.web.LDGBootGradle.model.Boardlike;
import com.web.LDGBootGradle.model.Comment;
import com.web.LDGBootGradle.model.User;
import com.web.LDGBootGradle.repository.BoardRepository;
import com.web.LDGBootGradle.repository.BoardlikeRepository;
import com.web.LDGBootGradle.repository.CommentRepository;
import com.web.LDGBootGradle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private BoardlikeRepository boardlikeRepository;

    @Transactional
    public Board save(String username, Board board){
        User user = userRepository.findByUsername(username);
        board.setUser(user);
        return boardRepository.save(board);
    }

    @Transactional
    public void deleteBoard(Long id) {

        List<Comment> commentlist = commentRepository.findByBoardIdOrderByCommentIdDesc(id);
        List<Boardlike> boardlikelist = boardlikeRepository.findByLikeContainingBoardId(id);
        
        // 해당 게시글에 등록된 댓글 삭제
        for (Comment comment : commentlist){
            commentRepository.delete(comment);
        }
        
        // 해당게시글에 추가된 좋아요 삭제 
        for (Boardlike boardlike : boardlikelist){
            boardlikeRepository.delete(boardlike);
        }

        boardRepository.deleteById(id);
    }

    public Long findByNextObj(Long id){ return boardRepository.findByNextObj(id); }
    public Long findByPrevObj(Long id){ return boardRepository.findByPrevObj(id); }

    public List<Board> findByRank(){ return boardRepository.findByRank(); }

}
