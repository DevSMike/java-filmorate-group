package ru.yandex.practicum.filmorate.controllers.view;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeBase {

    @GetMapping("/")
    public String home() {
        return "home";
    }
}
