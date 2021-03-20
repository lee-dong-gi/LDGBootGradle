package com.web.LDGBootGradle.service;

import com.web.LDGBootGradle.model.Board;
import com.web.LDGBootGradle.model.User;
import com.web.LDGBootGradle.repository.BoardRepository;
import com.web.LDGBootGradle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoardService {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private UserRepository userRepository;

    public Board save(String username, Board board){
        User user = userRepository.findByUsername(username);
        board.setUser(user);
        return boardRepository.save(board);
    }
}