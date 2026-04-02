package com.nv.ecommerce.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/user/test")
    public String userAccess() {
        return "User access granted";
    }

    @GetMapping("/admin/test")
    public String adminAccess() {
        return "Admin access granted";
    }
}
