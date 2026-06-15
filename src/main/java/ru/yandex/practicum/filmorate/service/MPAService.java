package ru.yandex.practicum.filmorate.service;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.filmorate.exception.NotFoundException;
import ru.yandex.practicum.filmorate.model.Mpa;
import ru.yandex.practicum.filmorate.storage.mapper.MPAMapper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class MPAService {
    private final JdbcTemplate jdbc;

    public List<Mpa> getAllRate() {
        return jdbc.query("SELECT * FROM rates;", new MPAMapper());
    }

    public Mpa getRateById(Long id) {
        return jdbc.query("SELECT * FROM rates WHERE id = ?;", new MPAMapper(), id)
                .stream().findFirst().orElseThrow(() -> new NotFoundException("Рейтинг с id = " + id + " не найден."));
    }
}
