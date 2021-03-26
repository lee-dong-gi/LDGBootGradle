package com.web.LDGBootGradle.controller;

import com.web.LDGBootGradle.model.Board;
import com.web.LDGBootGradle.model.Comment;
import com.web.LDGBootGradle.model.UploadFile;
import com.web.LDGBootGradle.model.User;
import com.web.LDGBootGradle.repository.BoardRepository;
import com.web.LDGBootGradle.repository.CommentRepository;
import com.web.LDGBootGradle.repository.FileRepository;
import com.web.LDGBootGradle.service.BoardService;
import com.web.LDGBootGradle.service.CommentService;
import com.web.LDGBootGradle.service.FileService;
import com.web.LDGBootGradle.service.UserService;
import com.web.LDGBootGradle.validator.BoardValidator;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;
import java.io.File;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
@RequestMapping("/board")
public class BoardController {

    @Autowired
    private BoardRepository boardRepository;

    @Autowired
    private BoardService boardService;

    @Autowired
    private BoardValidator boardValidator;

    @Autowired
    private FileRepository fileRepository;

    @Autowired
    private FileService fileService;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private CommentService commentService;

    @Autowired
    private UserService userService;

    private Board board;

    @GetMapping("/list")
    public String list(Model model, @PageableDefault(size = 10) Pageable pageable,
                       @RequestParam(required = false, defaultValue = "") String searchText) {
        // Page<Board> boards = boardRepository.findAll(pageable);
        Page<Board> boards = boardRepository.findByTitleContainingOrContentContainingOrderByIdDesc(searchText, searchText, pageable);
        //=========================numbering===========================================================================
        int num = 0, result = 0, pagesize = 10;
        int totalElements = (int) boards.getTotalElements() + 1;
        int nowpage = pageable.getPageNumber() + 1;
        System.out.println("nowpage :: " + nowpage);
        System.out.println("totalElements :: " + (totalElements - 1));
        for (Board temp : boards) {
            num++;
            if (nowpage == 1) {
                result = totalElements - num;
            } else {
                result = totalElements - (((nowpage * pagesize) - pagesize) + num);
            }
            temp.setNum(result);
        }
        //====================================================================================================
        int startPage = Math.max(1, boards.getPageable().getPageNumber() - 4);
        int endPage = Math.min(boards.getTotalPages(), boards.getPageable().getPageNumber() + 4);
        model.addAttribute("startPage", startPage);
        model.addAttribute("endPage", endPage);
        model.addAttribute("boards", boards);
        return "board/list";
    }

    @GetMapping("/view")
    public String view(Model model, @RequestParam(required = false) Long id, HttpServletRequest request,
                        HttpServletResponse response) {
        if (id == null) {
            model.addAttribute("board", new Board());
        } else {
            Board board = boardRepository.findById(id).orElse(null);

            //조회수 =======================================================
            int flag = setview(request, board.getId(),response);
            if (flag == 0){
                Long views = board.getViews();
                views = views + 1;
                board.setViews(views);
                boardRepository.save(board);
            }
            //=======================================================

            model.addAttribute("board", board);

            //첨부파일
            if (!("").equals(board.getUploadFileId()) & board.getUploadFileId() != null) {
                String basicpath = request.getRequestURL().toString().replace(request.getRequestURI(), "");
                String filepath = basicpath + "/download/board/" + board.getUploadFileId();

                UploadFile uploadFile = fileService.getFile(board.getUploadFileId());
                String imagepath = "/download/show/";
                String fileDownLoadUri = uploadFile.getFileDownloadUri();
                String fileType = uploadFile.getFileType();

                imagepath = imagepath + uploadFile.getUploadFileId();
                System.out.println("imagepath ::::: " + imagepath);

                model.addAttribute("filepath", filepath);
                model.addAttribute("imagepath", imagepath);
                model.addAttribute("filename", uploadFile.getFileName());
            } else {
                model.addAttribute("imagepath", "/images/noimage.jpg");
            }

            // 댓글
            List<Comment> comments = commentRepository.findByBoardId(id);
            if (!comments.isEmpty()){
                User commentUser;
                for (Comment com : comments){
                    commentUser = userService.findById(com.getUserId()).orElse(null);
                    com.setUsername(commentUser.getUsername());
                }
                model.addAttribute("comments", comments);
            }

        }
        return "board/view";
    }

    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id, HttpServletRequest request) {
        if (id == null) {
            model.addAttribute("board", new Board());
        } else {
            String imagepath = "/uploads/";
            Board board = boardRepository.findById(id).orElse(null);
            model.addAttribute("board", board);
            System.out.println(board.getUploadFileId());
            if (!("").equals(board.getUploadFileId()) & board.getUploadFileId() != null) {
                String basicpath = request.getRequestURL().toString().replace(request.getRequestURI(), "");
                String filepath = basicpath + "/download/board/" + board.getUploadFileId();

                UploadFile uploadFile = fileService.getFile(board.getUploadFileId());
                String fileDownLoadUri = uploadFile.getFileDownloadUri();
                String fileType = uploadFile.getFileType();

                String[] strarr;
                strarr = fileDownLoadUri.split(imagepath);
                fileDownLoadUri = strarr[1];

                imagepath = imagepath + fileDownLoadUri;

                System.out.println("imagepath ::::: " + imagepath);

                model.addAttribute("filepath", filepath);
                model.addAttribute("imagepath", imagepath);
                model.addAttribute("filename", uploadFile.getFileName());
            } else {
                imagepath = "";
                model.addAttribute("imagepath", imagepath);
            }
        }

        return "board/form";
    }

    @PostMapping("/form")
    public String postform(@Valid Board board, BindingResult result, Authentication authentication, @RequestParam(value = "uploadFile", required = false) MultipartFile files) {

        System.out.println(result.toString());
        boardValidator.validate(board, result);

        if (result.hasErrors()) {
            System.out.println(result.toString());
            return "board/form";
        }

        if (files != null) {
            System.out.println("files :::::::::" + files.toString());
            List<UploadFile> UploadFileId = fileupload(files);
            int listsize = UploadFileId.size() - 1;
            if (listsize < 0 || UploadFileId.isEmpty()) {
                board.setUploadFileId(UploadFileId.get(0).getUploadFileId());
            } else {
                board.setUploadFileId(UploadFileId.get(listsize).getUploadFileId());
            }
        }

        String username = authentication.getName();
        boardService.save(username, board);

        //boardRepository.save(board);
        return "redirect:/board/list";
    }

    //파일 업로드
    List<UploadFile> fileupload(MultipartFile files) {
        UploadFile uploadFile = new UploadFile();
        System.out.println("fileupload start");
        List<UploadFile> uploadFileList = new ArrayList<UploadFile>();
        try {

            String baseDir = "D:/intelij/spring/LDGBootGradle/src/main/resources/static/uploads";


            Long size = files.getSize();
            String fileType = files.getContentType();
            String fileName = files.getOriginalFilename();

            String[] fileTypeArr;
            String extention;
            fileTypeArr = fileType.split("/");

            if (fileTypeArr[1].equals("jpeg")) {
                extention = "jpg";
            } else {
                extention = fileTypeArr[1];
            }

            String modifileName = UUID.randomUUID().toString() + "." + extention;

            String filePath = baseDir + "/" + modifileName;

            files.transferTo(new File(filePath));

            System.out.println(modifileName);
            System.out.println(filePath);
            System.out.println(fileName);
            System.out.println(size);
            System.out.println(fileType);
            System.out.println("uploadFile setting start");

            uploadFile.setFileName(fileName);
            uploadFile.setFileDownloadUri(filePath);
            uploadFile.setFileType(fileType);
            uploadFile.setSize(size);

            fileService.save(uploadFile);

            uploadFileList = fileRepository.findAll();
            return uploadFileList;
        } catch (Exception e) {
            e.printStackTrace();
            return uploadFileList;
        }
    }

    @PostMapping("/commentform")
    public String commentform(@Valid Comment comment, BindingResult result, Authentication authentication) {
        String username= authentication.getName();
        commentService.save(username,comment);
        Long boardId = comment.getBoardId();
        return "redirect:/board/view?id=" + boardId;
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id) {
        boardService.deleteBoard(id);
        return "redirect:/board/list";
    }

    @PostMapping("/commetdelete/{id}")
    public String commentdelete(@PathVariable("id") Long id,Long boardId) {
        commentService.deleteComment(id);
        return "redirect:/board/view?id=" + boardId;
    }

    //boardViews쿠키가 있을때
    private void setCookies(HttpServletRequest request, Long boardnum, HttpServletResponse response, Cookie[] cookies){
        System.out.println("start setCookies");
        String temp = "";
        for (Cookie cookie : cookies){
            if(cookie.getName().equals("boardViews")){ // boardViews라는 이름의 쿠키가 있으면 값을 갱신함
                temp = cookie.getValue();
            }
        }
        temp = temp + "/" + boardnum.toString();
        Cookie myCookie = new Cookie("boardViews", temp);
        myCookie.setMaxAge(10000000); // 쿠키의 expiration 타임을 0으로 하여 없앤다.
        myCookie.setPath("/"); // 모든 경로에서 삭제 됬음을 알린다.
        response.addCookie(myCookie);
        System.out.println("setCookies ::: " + myCookie.toString());
    }

    //쿠키가 아예없으면
    private void initCookies (HttpServletRequest request, Long boardnum, HttpServletResponse response){
        System.out.println("start initCookies");
        Cookie myCookie = new Cookie("boardViews", boardnum.toString());
        myCookie.setMaxAge(10000000); // 쿠키의 expiration 타임을 0으로 하여 없앤다.
        myCookie.setPath("/"); // 모든 경로에서 삭제 됬음을 알린다.
        response.addCookie(myCookie);
        System.out.println("initCookies ::: " + myCookie.toString());
    }
    
    //특정 게시글 조회이력 확인 및 쿠키세팅
    private int setview(HttpServletRequest request, Long boardnum, HttpServletResponse response){
        int flag = 0; //0이면 조회한적 없음, 1이면 조회한적있음
        int initflag = 0; // 0이면 boardViews쿠키 있음, 1이면 boardViews쿠키 없음
        Cookie[] myCookies = request.getCookies();
        if (!myCookies.equals(null) || (myCookies.length)!=0){
            for (Cookie myCookie : myCookies){
                System.out.println("myCookie.getName() ::: " + myCookie.getName());
                System.out.println("myCookie.getValue() ::: " + myCookie.getValue());
                if(myCookie.getName().equals("boardViews")){
                    initflag=1;
                    String[] tempBoardNums = myCookie.getValue().split("/");
                    for(String tempBoardNum : tempBoardNums){
                        System.out.println("tempBoardNum ::: " + tempBoardNum);
                        if (tempBoardNum.equals(boardnum.toString())){
                            System.out.println("중복체크 걸림");
                            flag = 1;
                            return flag;
                        }
                    }
                }
            }
            if (flag==0 & initflag==1){ setCookies(request, boardnum, response, myCookies);
                System.out.println("flag ::: " + flag);
                return flag;
            }
            else{
                initCookies(request, boardnum, response);
                System.out.println("flag ::: " + flag);
                return flag;
            }
        }else{
            initCookies(request, boardnum, response);
            return flag;
        }
    }


}
