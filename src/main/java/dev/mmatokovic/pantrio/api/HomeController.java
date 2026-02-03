package dev.mmatokovic.pantrio.api;

import dev.mmatokovic.pantrio.app.storage.StorageService;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class HomeController {
    private final StorageService service;

    public HomeController(StorageService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String home() {
        return "index";
    }

    @GetMapping(value = "/htmx/storages", produces = MediaType.TEXT_HTML_VALUE)
    public String listStorages(Model model) {
        model.addAttribute("storages", service.listStorages(Pageable.unpaged()));
        return "fragments/list-storages :: list";
    }

    @GetMapping("/htmx/storages/create")
    public String createStorage(Model model) {
        model.addAttribute("storage");
        return "fragments/create-storage :: create";
    }

    @GetMapping("/htmx/storages/{id}/edit")
    public String edit(@PathVariable Long id, Model model) {
        model.addAttribute("storage", service.getStorage(id));
        return "fragments/storage-edit :: editForm";
    }
}
