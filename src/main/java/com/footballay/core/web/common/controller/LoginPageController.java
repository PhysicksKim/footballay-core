package com.footballay.core.web.common.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import jakarta.servlet.http.HttpServletRequest;

@Controller
public class LoginPageController {

    @GetMapping("/login")
    public String loginPage(HttpServletRequest request, Model model) {
        // 이미 붙어온 after 파라미터를 그대로 뷰로 넘겨줌
        String after = request.getParameter("after");
        model.addAttribute("after", after);
        return "login"; // src/main/resources/templates/login.html (Thymeleaf 기준)
    }

}