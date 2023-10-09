package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import ru.yandex.practicum.filmorate.validator.UserNameConstraint;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Past;
import javax.validation.constraints.Pattern;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Data
@UserNameConstraint
@NoArgsConstructor
@AllArgsConstructor
public class User {
    private long id;
    @NotBlank(message = "Email can't be null or empty")
    @Email(message = "Incorrect email")
    private String email;
    @Pattern(regexp = "\\S+", message = "No whitespaces allowed")
    private String login;
    private String name;
    @Past(message = "Birthday must be in past")
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate birthday;
    private Map<Long, String> friends = new HashMap<>();


    public User(String email, String login, LocalDate birthday) {
        this.email = email;
        this.login = login;
        this.birthday = birthday;
    }

}