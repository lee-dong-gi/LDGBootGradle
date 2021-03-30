package com.web.LDGBootGradle.service;

import com.web.LDGBootGradle.model.Board;
import com.web.LDGBootGradle.model.User;
import com.web.LDGBootGradle.repository.BoardRepository;
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

    @Transactional
    public Board save(String username, Board board){
        User user = userRepository.findByUsername(username);
        board.setUser(user);
        return boardRepository.save(board);
    }

    @Transactional
    public void deleteBoard(Long id) {
        boardRepository.deleteById(id);
    }

    public Long findByNextObj(Long id){ return boardRepository.findByNextObj(id); }
    public Long findByPrevObj(Long id){ return boardRepository.findByPrevObj(id); }

    public List<Board> findByRank(){ return boardRepository.findByRank(); }
}
