package com.web.LDGBootGradle.controller;

import com.web.LDGBootGradle.model.Board;
import com.web.LDGBootGradle.service.BoardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @Autowired
    BoardService boardService;

    @GetMapping
    public String index(Model model){
        List<Board> rankboard = boardService.findByRank();
        model.addAttribute("rankboard",rankboard);
        return "urbanindex";
    }

    @GetMapping("/urban")
    public String urbanindex(Model model){
        List<Board> rankboard = boardService.findByRank();
        model.addAttribute("rankboard",rankboard);
        return "urbanindex";
    }
}
