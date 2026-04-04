package com.toyboyz.fileconversion.index.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.Arrays;
import java.util.List;

@Controller
public class IndexController {

    @GetMapping("/convert")
    public String convertPage(Model model) {
        List<String> formats = Arrays.asList("PDF", "JPG", "PNG", "DOCX");
        model.addAttribute("formats", formats);
        return "convert";
    }


}