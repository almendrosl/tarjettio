package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.dto.TokenDto;
import com.tarjettio.tarjettio.entities.User;
import com.tarjettio.tarjettio.services.JwtService;
import com.tarjettio.tarjettio.services.UserService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final JwtService jwtService;
    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(JwtService jwtService, UserService userService, SecurityContextRepository securityContextRepository) {
        this.jwtService = jwtService;
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/login-con-token")
    public ResponseEntity<?> loginWithToken(@RequestBody TokenDto tokenDto, HttpServletRequest request, HttpServletResponse response) {
        String token = tokenDto.getToken();

        if (token == null || !jwtService.isTokenValid(token)) {
            logger.warn("Intento de login con token inválido o nulo.");
            return ResponseEntity.status(401).body(Map.of("error", "Token inválido o expirado."));
        }

        try {
            String userId = jwtService.extractUserId(token);
            Claims claims = jwtService.extractAllClaims(token);
            String email = claims.get("email", String.class);
            String fullName;
            if (claims.get("user_metadata", Map.class) != null) {
                fullName = (String) claims.get("user_metadata", Map.class).get("full_name");
            } else {
                fullName = "Usuario";
            }

            User appUser = userService.findById(userId).orElseGet(() -> {
                User newUser = new User();
                newUser.setId(userId);
                newUser.setEmail(email);
                newUser.setFirstName(fullName);
                return userService.saveUser(newUser);
            });

            UserDetails userDetails = org.springframework.security.core.userdetails.User
                    .withUsername(appUser.getEmail())
                    .password("")
                    .authorities(new SimpleGrantedAuthority("ROLE_USER"))
                    .build();

            Authentication authentication = new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
            );

            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(authentication);

            SecurityContextHolder.setContext(context);

            securityContextRepository.saveContext(context, request, response);

            logger.info("Usuario {} autenticado exitosamente y sesión creada.", appUser.getEmail());
            return ResponseEntity.ok().body(Map.of("message", "Login en backend exit"));

        } catch (Exception e) {
            return ResponseEntity.status(500).body("Error en el servidor durante la autenticación.");
        }
    }
}