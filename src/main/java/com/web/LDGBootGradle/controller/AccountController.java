package com.web.LDGBootGradle.controller;

import com.google.gson.Gson;
import com.web.LDGBootGradle.model.User;
import com.web.LDGBootGradle.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/account")
public class AccountController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String login(){
        return "account/login";
    }

    @GetMapping("/register")
    public String register(){
        return "account/register";
    }

    @PostMapping("/register")
    public String register(User user){
        userService.save(user);
        return "redirect:/urban";
    }

    @PostMapping("/dupcheck")
    @ResponseBody
    public String dupcheck(@RequestParam(value = "username", required = false) String username){
        User user = userService.findByUsername(username);
        Map<String,String> result = new HashMap<String,String>();
        if (user == null){
            result.put("result","Y");
        }else{
            result.put("result","N");
        }
        Gson gson = new Gson();
        String jsonString = gson.toJson(result);
        return jsonString;
    }
}
