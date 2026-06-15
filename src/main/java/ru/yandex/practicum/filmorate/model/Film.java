package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

@NoArgsConstructor
@Data
public class Film {
    private Long id;
    @NotBlank
    private String name;
    @Size(max = 200)
    private String description;
    private LocalDate releaseDate;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT, pattern = "MINUTES")
    private Duration duration;
    private Mpa mpa;
    private int rate;
    private Set<Genre> genres = new TreeSet<>(Comparator.comparingLong(Genre::getId));
    private Set<Long> likeList = new HashSet<>();
}
