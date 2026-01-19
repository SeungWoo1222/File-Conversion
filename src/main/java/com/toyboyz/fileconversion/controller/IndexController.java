package com.toyboyz.fileconversion.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class IndexController {

    @GetMapping("/test")
    public String test(Model model){
        model.addAttribute("data", "성공");
        return "index";
    }

    @GetMapping("/convert")
    public String convertPage(Model model) {
        List<String> formats = Arrays.asList("PDF", "JPG", "PNG", "DOCX");
        model.addAttribute("formats", formats);
        return "convert";
    }
}