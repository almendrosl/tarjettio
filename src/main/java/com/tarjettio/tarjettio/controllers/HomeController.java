package com.tarjettio.tarjettio.controllers;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class HomeController {

    @GetMapping("/login")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "login"; // login.html
    }

    @GetMapping("/dashboard")
    public String home() {
        return "dashboard"; // dashboard.html (protegida)
    }

    @ResponseBody
    @GetMapping("/api/protected")
    public String protectedApi(Authentication authentication) {
        return "Estás autenticado como: " + authentication.getName();
    }
}
