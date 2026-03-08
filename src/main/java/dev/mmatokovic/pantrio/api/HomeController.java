package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.storage.StorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {
    private final StorageService service;

    public HomeController(StorageService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home(Model model) {
        model.addAttribute("storages", service.listStorages(Pageable.unpaged()));
        return "index";
    }
}
