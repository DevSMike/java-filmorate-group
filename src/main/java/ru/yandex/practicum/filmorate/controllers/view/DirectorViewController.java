package ru.yandex.practicum.filmorate.controllers.view;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.model.Director;
import ru.yandex.practicum.filmorate.service.DirectorService;

import java.util.Collection;

@Controller
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/directors")
public class DirectorViewController {

    private final DirectorService directorService;

    @GetMapping("/add")
    public String addFilmForm(Model model) {
        model.addAttribute("director", new Director());
        return "add-director";
    }

    @PostMapping("/add")
    public String addDirector(Director director) {
        log.info("director: {}", director);
        directorService.create(director);
        return "redirect:/films";
    }

    @GetMapping()
    public String getAllDirectors(Model model) {
        Collection<Director> directors = directorService.getAll();
        model.addAttribute("directors", directors);
        return "directors";
    }
}
