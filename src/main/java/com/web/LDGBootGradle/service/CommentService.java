package com.web.LDGBootGradle.service;

import com.web.LDGBootGradle.model.Comment;
import com.web.LDGBootGradle.model.User;
import com.web.LDGBootGradle.repository.CommentRepository;
import com.web.LDGBootGradle.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Comment save(String username, Comment comment){
        User user = userRepository.findByUsername(username);
        comment.setUserId(user.getId());
        return commentRepository.save(comment);
    }

    @Transactional
    public void deleteComment(Long id) {
        commentRepository.deleteById(id);
    }
}
