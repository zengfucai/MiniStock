package com.ministock.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
public class HomeController {

    @GetMapping("/")
    public Map<String, Object> home() {

        Map<String, Object> result =
                new HashMap<>();

        result.put("success", true);
        result.put("name", "MiniStock");
        result.put("message", "MiniStock后端运行正常");

        return result;
    }
}