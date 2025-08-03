package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.config.TestSecurityConfig;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.redirectedUrl;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.view;

@WebMvcTest(HomeController.class)
@Import(TestSecurityConfig.class)
class HomeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @WithMockUser(username = "usuario@ejemplo.com", roles = "USER")
    void testHomePage_WhenAuthenticated_ShouldRedirectToDashboard() throws Exception {
        // Ejecutar la petición y verificar la redirección
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void testHomePage_WhenNotAuthenticated_ShouldRedirectToLogin() throws Exception {
        // Ejecutar la petición sin autenticación y verificar la redirección
        mockMvc.perform(get("/"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @WithMockUser(username = "usuario@ejemplo.com", roles = "USER")
    void testLoginPage_WhenAuthenticated_ShouldRedirectToDashboard() throws Exception {
        // Ejecutar la petición y verificar la redirección
        mockMvc.perform(get("/login"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/dashboard"));
    }

    @Test
    void testLoginPage_WhenNotAuthenticated_ShouldShowLoginPage() throws Exception {
        // Ejecutar la petición sin autenticación y verificar que muestra la vista de login
        mockMvc.perform(get("/login")
                .with(csrf()))
                .andExpect(status().isOk())
                .andExpect(view().name("login"));
    }

    @Test
    @WithMockUser(username = "usuario@ejemplo.com", roles = "USER")
    void testDashboardPage_ShouldReturnDashboardView() throws Exception {
        // Este método requiere autenticación
        mockMvc.perform(get("/dashboard"))
                .andExpect(status().isOk())
                .andExpect(view().name("dashboard"));
    }

    @Test
    @WithMockUser(username = "usuario@ejemplo.com", roles = "USER")
    void testProtectedApi_ShouldReturnAuthenticatedUserName() throws Exception {
        // Ejecutar la petición y verificar la respuesta
        mockMvc.perform(get("/api/protected"))
                .andExpect(status().isOk())
                .andExpect(content().string("Estás autenticado como: usuario@ejemplo.com"));
    }
}
