package ru.yandex.practicum.filmorate.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import javax.validation.constraints.AssertTrue;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;
import java.util.TreeSet;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Film {
    public static final LocalDate START_DATE = LocalDate.of(1895, 12, 28);
    private Long id;
    @NotBlank(message = "Name can't be null or empty")
    private String name;
    @Size(max = 200, message = "Description must be no more than 200 symbols")
    private String description;
    @DateTimeFormat(pattern = "yyyy-MM-dd")
    private LocalDate releaseDate;
    @Min(value = 1L, message = "The duration must be positive")
    private Integer duration;
    private Set<Long> likes = new HashSet<>();
    private Set<Genre> genres = new TreeSet<>(Comparator.comparingInt(Genre::getId));
    private Mpa mpa;
    private int rate;
    private Set<Director> directors = new HashSet<>();

    @AssertTrue(message = "releaseDate is before 1895.12.28")
    public boolean isReleaseDateAfter() {
        return releaseDate.isAfter(START_DATE);
    }


    public Film(String name, String description, LocalDate releaseDate, Integer duration) {
        this.name = name;
        this.description = description;
        this.releaseDate = releaseDate;
        this.duration = duration;
    }

}
