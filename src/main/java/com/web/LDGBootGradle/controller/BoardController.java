package com.web.LDGBootGradle.controller;

import com.google.gson.Gson;
import com.web.LDGBootGradle.model.*;
import com.web.LDGBootGradle.repository.BoardRepository;
import com.web.LDGBootGradle.repository.BoardlikeRepository;
import com.web.LDGBootGradle.repository.CommentRepository;
import com.web.LDGBootGradle.repository.FileRepository;
import com.web.LDGBootGradle.service.BoardService;
import com.web.LDGBootGradle.service.CommentService;
import com.web.LDGBootGradle.service.FileService;
import com.web.LDGBootGradle.service.UserService;
import com.web.LDGBootGradle.validator.BoardValidator;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeanWrapper;
import org.springframework.beans.BeanWrapperImpl;
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

import java.util.*;

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
    private BoardlikeRepository boardlikeRepository;

    @Autowired
    private UserService userService;

    private Board board;

    //  게시판 리스트
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

    // 게시글 보기
    @GetMapping("/view")
    public String view(Model model, @RequestParam(required = false) Long id, HttpServletRequest request,
                        HttpServletResponse response, Authentication authentication) {
        Board board = boardRepository.findById(id).orElse(null);

        // 게시판 수정, 삭제 시 이용자 본인여부 체크
        String selfcheck = "N";
        if (checkUser(authentication,board)){
            selfcheck =  "Y";
        }
        model.addAttribute("selfcheck",selfcheck);

        //조회수 =======================================================
        int flag = setview(request, board.getId(),response); //0이면 조회한적 없음, 1이면 조회한적 있음
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
        List<Comment> comments = commentRepository.findByBoardIdOrderByCommentIdDesc(id);
        if (!comments.isEmpty()){
            User commentUser;
            for (Comment com : comments){
                commentUser = userService.findById(com.getUserId()).orElse(null);
                com.setUsername(commentUser.getUsername());
                if (checkUser(authentication,com)){
                    com.setCommentselfcheck("Y");
                }else{
                    com.setCommentselfcheck("N");
                }
            }
            model.addAttribute("comments", comments);
        }
        // 좋아요 수
        List<Boardlike> boardlikelist = boardlikeRepository.findByLikeContainingBoardId(id);
        int boardlikenum = boardlikelist.size();
        model.addAttribute("boardlikenum",boardlikenum);

        //다음글 이전글 리스트
        Long nextboardId = boardService.findByNextObj(id);
        Board nextBoard = null;
        Board prevBoard = null;
        if (nextboardId!=null) {
            nextBoard = boardRepository.findById(nextboardId).orElse(null);
        }

        Long prevboardId = boardService.findByPrevObj(id);
        if (prevboardId!=null) {
            prevBoard = boardRepository.findById(prevboardId).orElse(null);
        }
        
        model.addAttribute("nextBoard",nextBoard);
        model.addAttribute("prevBoard",prevBoard);
        
        return "board/view";
    }

    // 게시글쓰기 및 수정
    @GetMapping("/form")
    public String form(Model model, @RequestParam(required = false) Long id, HttpServletRequest request, Authentication authentication) {
        if (id == null) {
            model.addAttribute("board", new Board());
            model.addAttribute("type", "new");
        } else {
            model.addAttribute("type", "modify");
            String imagepath = "/uploads/";
            Board board = boardRepository.findById(id).orElse(null);


            // 이용자 체크
            if (!checkUser(authentication,board)){
                return "redirect:/board/list";
            }

            model.addAttribute("board", board);
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

    // 게시글 작성
    @PostMapping("/form")
    public String postform(@Valid Board board, BindingResult result, Authentication authentication,
                           @RequestParam(value = "uploadFile", required = false) MultipartFile files,
                           @RequestParam(value = "type", required = false) String type) {
        boardValidator.validate(board, result);

        if (result.hasErrors()) {
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

        if(!type.equals("modify")){
            board.setViews(0l);
            board.setViews(0l);
        }else{
            List<Boardlike> boardlikelist = boardlikeRepository.findByLikeContainingBoardId(board.getId());
            Board existboard = boardRepository.findById(board.getId()).orElse(null);
            int boardlikenum = boardlikelist.size();

            // 게시판 좋아요 및 조회수 컬럼 변경
            Integer modifylike = boardlikenum;
            Long modifylikenum = Long.valueOf(modifylike.toString());

            board.setViews(existboard.getViews());
            board.setBoardlike(modifylikenum);
            System.out.println("board.getBoardlike() ::: "+board.getBoardlike());
        }

        String username = authentication.getName();
        boardService.save(username, board);

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

    // 댓글쓰기
    @PostMapping("/commentform")
    public String commentform(@Valid Comment comment, BindingResult result, Authentication authentication) {
        String username= authentication.getName();
        commentService.save(username,comment);
        Long boardId = comment.getBoardId();
        return "redirect:/board/view?id=" + boardId;
    }

    // 게시글 삭제
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable("id") Long id, Authentication authentication) {
        Board board = boardRepository.findById(id).orElse(null);

        // 이용자 체크
        if (!checkUser(authentication,board)){
            return "redirect:/board/list";
        }

        boardService.deleteBoard(id);
        return "redirect:/board/list";
    }

    // 댓글삭제
    @PostMapping("/commetdelete/{id}")
    public String commentdelete(@PathVariable("id") Long id,Long boardId, Authentication authentication) {
        // 이용자 체크
        Comment comment = commentRepository.findById(id).orElse(null);
        Long commentUserId = comment.getUserId();
        User commentuser = userService.findById(commentUserId).orElse(null);
        String commentUsername = commentuser.getUsername();
        comment.setUsername(commentUsername);
        if (!checkUser(authentication,comment)){
            return "redirect:/board/list";
        }

        commentService.deleteComment(id);
        return "redirect:/board/view?id=" + boardId;
    }


    @PostMapping("/commetModify/{id}")
    @ResponseBody
    public String commetModify(@PathVariable("id") Long id){
        Comment comment = commentRepository.findById(id).orElse(null);

        Gson gson = new Gson();
        String jsonString = gson.toJson(comment);
        return jsonString;
    }

    //좋아요 여부체크
    @PostMapping("/likecheck/{boardid}")
    @ResponseBody
    public String likecheck(@PathVariable("boardid") Long boardid,  Authentication authentication){
        Map<String, Integer> resultMap = new HashMap<String,Integer>();
        Boardlike boardlike = new Boardlike();
        resultMap.put("result",0);

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        boardlike = boardlikeRepository.findByLikeContainingUserIdContainingBoardId(user.getId(),boardid);
        System.out.println("boardlike ::: " + boardlike);
        if (boardlike==null){
            resultMap.put("result",0);
        }else{
            resultMap.put("result",1);
        }

        List<Boardlike> boardlikelist = boardlikeRepository.findByLikeContainingBoardId(boardid);
        int boardlikenum = boardlikelist.size();
        resultMap.put("boardlikenum", boardlikenum);

        Gson gson = new Gson();
        String jsonString = gson.toJson(resultMap);
        return jsonString;
    }

    //좋아요 체크
    @PostMapping("/like/{boardid}")
    @ResponseBody
    public String like(@PathVariable("boardid") Long boardid,  Authentication authentication){
        System.out.println("boardid ::: " + boardid);
        Boardlike boardlike = new Boardlike();

        Map<String, Integer> resultMap = new HashMap<String,Integer>();

        resultMap.put("result",0);

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        boardlike.setBoardId(boardid);
        boardlike.setUserId(user.getId());

        boardlikeRepository.save(boardlike);
        resultMap.put("result",1);

        List<Boardlike> boardlikelist = boardlikeRepository.findByLikeContainingBoardId(boardid);
        int boardlikenum = boardlikelist.size();
        resultMap.put("boardlikenum", boardlikenum);

        // 게시판 좋아요 컬럼 변경
        Board existBoard = boardRepository.findById(boardid).orElse(null);
        Integer modifylike = boardlikenum;
        Long modifylikenum = Long.valueOf(modifylike.toString());
        existBoard.setBoardlike(modifylikenum);
        boardRepository.save(existBoard);

        Gson gson = new Gson();
        String jsonString = gson.toJson(resultMap);
        return jsonString;
    }

    //좋아요 체크 해제
    @PostMapping("/unlike/{boardid}")
    @ResponseBody
    public String unlike(@PathVariable("boardid") Long boardid,  Authentication authentication){
        Boardlike boardlike = new Boardlike();

        Map<String, Integer> resultMap = new HashMap<String,Integer>();

        resultMap.put("result",0);

        String username = authentication.getName();
        User user = userService.findByUsername(username);

        boardlike = boardlikeRepository.findByLikeContainingUserIdContainingBoardId(user.getId(),boardid);

        boardlikeRepository.delete(boardlike);

        resultMap.put("result",1);

        List<Boardlike> boardlikelist = boardlikeRepository.findByLikeContainingBoardId(boardid);
        int boardlikenum = boardlikelist.size();
        resultMap.put("boardlikenum", boardlikenum);

        // 게시판 좋아요 컬럼 변경
        Board existBoard = boardRepository.findById(boardid).orElse(null);
        Integer modifylike = boardlikenum;
        Long modifylikenum = Long.valueOf(modifylike.toString());
        existBoard.setBoardlike(modifylikenum);
        boardRepository.save(existBoard);

        Gson gson = new Gson();
        String jsonString = gson.toJson(resultMap);
        return jsonString;
    }

    // boardViews쿠키가 있을때
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
                if(myCookie.getName().equals("boardViews")){
                    initflag=1;
                    String[] tempBoardNums = myCookie.getValue().split("/");
                    for(String tempBoardNum : tempBoardNums){
                        System.out.println("tempBoardNum ::: " + tempBoardNum);
                        if (tempBoardNum.equals(boardnum.toString())){
                            System.out.println("dupcheck!");
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

    // 게시글 이용자 체크
    private boolean checkUser(Authentication authentication, Board board){
        boolean result = false;
        String authUserName = authentication.getName();
        Long boardUserId = board.getUser().getId();
        User user = userService.findById(boardUserId).orElse(null);
        if (authUserName.equals(user.getUsername())){
            result = true;
        }else{
            result = false;
        }
        return result;
    }

    // 댓글 이용자 체크
    private boolean checkUser(Authentication authentication, Comment comment){
        boolean result = false;
        String authUserName = authentication.getName();
        String commentUserName = comment.getUsername();
        if (authUserName.equals(commentUserName)){
            result = true;
        }else{
            result = false;
        }
        return result;
    }



    public static void copyNonNullProperties(Object src, Object target) {
        BeanUtils.copyProperties(src, target, getNullPropertyNames(src));
    }
    public static String[] getNullPropertyNames (Object source) {
        final BeanWrapper src = new BeanWrapperImpl(source);
        java.beans.PropertyDescriptor[] pds = src.getPropertyDescriptors();
        Set<String> emptyNames = new HashSet<String>();
        for(java.beans.PropertyDescriptor pd : pds) {
            Object srcValue = src.getPropertyValue(pd.getName());
            if (srcValue == null) emptyNames.add(pd.getName());
        }
        String[] result = new String[emptyNames.size()];
        return emptyNames.toArray(result);
    }
}
