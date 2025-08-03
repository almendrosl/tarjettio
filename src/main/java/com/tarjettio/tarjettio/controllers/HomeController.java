package com.tarjettio.tarjettio.controllers;

import com.tarjettio.tarjettio.entities.Card;
import com.tarjettio.tarjettio.entities.Deck;
import com.tarjettio.tarjettio.services.CardService;
import com.tarjettio.tarjettio.services.DeckService;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.List;

import static com.tarjettio.tarjettio.utils.Credentials.getCurrentUserId;

@Controller
public class HomeController {

    private final DeckService deckService;
    private final CardService cardService;

    public HomeController(DeckService deckService, CardService cardService) {
        this.deckService = deckService;
        this.cardService = cardService;
    }

    @GetMapping("/")
    public String home(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "redirect:/login";
    }

    @GetMapping("/login")
    public String index(Authentication authentication) {
        if (authentication != null && authentication.isAuthenticated()) {
            return "redirect:/dashboard";
        }
        return "login"; // login.html
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {

        String userId = getCurrentUserId();

        // Obtener los mazos del usuario
        List<Deck> userDecks = deckService.findByUserId(userId);
        model.addAttribute("decks", userDecks);

        // Si hay mazos, obtener las tarjetas del primer mazo para mostrar como ejemplo
        if (!userDecks.isEmpty()) {
            List<Card> cards = cardService.findByDeckId(userDecks.get(0).getId());
            model.addAttribute("cards", cards);
        }

        return "dashboard";
    }

    @ResponseBody
    @GetMapping("/api/protected")
    public String protectedApi(Authentication authentication) {
        return "Estás autenticado como: " + authentication.getName();
    }
}
