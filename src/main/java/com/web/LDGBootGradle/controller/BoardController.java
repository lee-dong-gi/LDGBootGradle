package com.web.LDGBootGradle.controller;

import com.web.LDGBootGradle.model.Board;
import com.web.LDGBootGradle.repository.BoardRepository;
import com.web.LDGBootGradle.service.BoardService;
import com.web.LDGBootGradle.validator.BoardValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import javax.persistence.OrderBy;
import javax.validation.Valid;
import java.util.ArrayList;
import java.util.List;

@Controller
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardValidator boardValidator;
    private Board board;

    @GetMapping("/list")
    public String list(Model model,@PageableDefault(size = 10) Pageable pageable,
                       @RequestParam(required = false, defaultValue = "") String searchText){
        // Page<Board> boards = boardRepository.findAll(pageable);
        Page<Board> boards = boardRepository.findByTitleContainingOrContentContainingOrderByIdDesc(searchText, searchText, pageable);
        //=========================numbering===========================================================================
        int num = 0, result = 0, pagesize = 10;
        int totalElements = (int)boards.getTotalElements()+1;
        int nowpage = pageable.getPageNumber()+1;
        System.out.println("nowpage :: "+nowpage);
        System.out.println("totalElements :: "+(totalElements-1));
        for (Board temp : boards){
            num++;
            if (nowpage==1){
                result=totalElements-num;
            }else{
                result = totalElements-(((nowpage*pagesize)-pagesize)+num);
            }
            temp.setNum(result);
        }
        //====================================================================================================
        int startPage = Math.max(1 , boards.getPageable().getPageNumber() - 4);
        int endPage = Math.min(boards.getTotalPages() , boards.getPageable().getPageNumber() + 4);
        model.addAttribute("startPage",startPage);
        model.addAttribute("endPage",endPage);
        model.addAttribute("boards",boards);
        return "board/list";
    }

    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id){
        if(id==null) {
            model.addAttribute("board", new Board());
        }else{
            Board board = boardRepository.findById(id).orElse(null);
            model.addAttribute("board", board);
        }
        return "board/form";
    }

    @PostMapping("/form")
    public String postform(@Valid Board board, BindingResult result, Authentication authentication) {

        System.out.println(result.toString());
        boardValidator.validate(board, result);

        if (result.hasErrors()) {
            System.out.println(result.toString());
            return "board/form";
        }
        String username= authentication.getName();
        boardService.save(username, board);
        //boardRepository.save(board);
        return "redirect:/board/list";
    }
}
