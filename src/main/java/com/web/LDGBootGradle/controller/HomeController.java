package com.web.LDGBootGradle.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping
    public String index(){
        return "urbanindex";
    }

    @GetMapping("/urban")
    public String urbanindex(){
        return "urbanindex";
    }
}
