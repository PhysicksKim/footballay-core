package com.gyechunsik.scoreboard.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

@Slf4j
@Controller
public class AdminController {

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminIndexPage(Authentication authentication) {
        log.info("auth details : {}", authentication.getDetails());
        log.info("auth isAuth : {}", authentication.isAuthenticated());
        log.info("auth role : {}", authentication.getAuthorities());
        log.info("auth toString : {}", authentication);
        return "admin/adminindex";
    }

}
