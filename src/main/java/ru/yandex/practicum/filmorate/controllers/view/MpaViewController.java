package ru.yandex.practicum.filmorate.controllers.view;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.service.MpaService;

import java.util.Collection;

@Slf4j
@Controller
@RequestMapping("/mpa")
public class MpaViewController {

    private final MpaService mpaService;

    public MpaViewController(MpaService mpaService) {
        this.mpaService = mpaService;
    }

    @GetMapping()
    public String getAllMpa(Model model) {
        log.info("Post request for getAllMpa");
        Collection<Mpa> ratings = mpaService.getAll();
        model.addAttribute("mpaList", ratings);
        return "mpa";
    }
}
