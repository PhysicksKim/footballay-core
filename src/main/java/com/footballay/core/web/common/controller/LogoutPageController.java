package com.footballay.core.web.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LogoutPageController {

    @GetMapping("/logout")
    public String logoutPage() {
        return "logout"; // logout.html
    }
}
