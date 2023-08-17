package com.zj.starter.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class IndexPageRest {

    @GetMapping("/")
    public String index() {
        return "index";
    }
}
