package ru.yandex.practicum.filmorate.controllers.view;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import ru.yandex.practicum.filmorate.model.User;
import ru.yandex.practicum.filmorate.service.EventService;
import ru.yandex.practicum.filmorate.service.UserService;

import java.util.Collection;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
@Slf4j
public class UserViewController {

    private final UserService userService;
    private final EventService eventService;

    @GetMapping("/add")
    public String addUserForm(Model model) {
        model.addAttribute("user", new User());
        return "add-user";
    }

    @PostMapping("/add")
    public String addUser(User user) {
        log.info("user: {}", user);
        userService.addUser(user);
        return "redirect:/users";
    }

    @GetMapping()
    public String getUsers(Model model) {
        log.info("Getting users");
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "users";
    }

    @DeleteMapping("/{userId}")
    public void deleteUser(@PathVariable long userId) {
        log.info("Delete request to remove user with id {}", userId);
        userService.deleteUser(userId);
    }

    @GetMapping("/friends")
    public String addFriendsForm(Model model) {
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "friends";
    }

    @PostMapping("friends/add")
    public String addFriend( long userId,  long friendId) {
        log.info("Put request to add friend id: {} friendId: {}", userId, friendId);
        userService.addFriend(userId, friendId);
        return "redirect:/users";
    }

    @PostMapping("friends/delete")
    public String deleteFriend( long userId, long friendId) {
        log.info("Put request to delete friend id: {} friendId: {}", userId, friendId);
        userService.removeFriend(userId, friendId);
        return "redirect:/users";
    }

    @GetMapping("/friends/user")
    public String getFriendsListForm(Model model) {
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user-friends";
    }

    @PostMapping("/friends/user")
    public String getFriendsList(@RequestParam("selectedUserId") Long id, Model model) {
        log.info("Get request for all friends list for user: {}", id);
        Collection<User> friends = userService.getFriendsList(id);
        model.addAttribute("friends", friends);
        Collection<User> users = userService.getAllUsers();
        model.addAttribute("users", users);
        return "user-friends";
    }
}
