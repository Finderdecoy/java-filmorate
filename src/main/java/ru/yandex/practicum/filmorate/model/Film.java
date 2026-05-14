package ru.yandex.practicum.filmorate.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.NonNull;

import java.time.Duration;
import java.time.LocalDate;

/**
 * Film.
 */
@NoArgsConstructor
@Data
public class Film {
    private Long id;
    @NonNull
    private String name;
    private String description;
    private LocalDate releaseDate;
    @JsonFormat(shape = JsonFormat.Shape.NUMBER_INT, pattern = "MINUTES")
    private Duration duration;
}
