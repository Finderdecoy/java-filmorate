insert into GENRE (name) values
('Комедия'), ('Драма'), ('Мультфильм'), ('Триллер'), ('Документальный'), ('Боевик');
/*,('Аниме'), ('Биография'), ('Боевик'),
 ('Вестерн'), ('Военный'), ('Детектив'), ('Детский'), ('Документальный'), ('Драма'), ('История'), ('Комедия'),
  ('Концерт'), ('Короткометражка'), ('Криминал'), ('Мелодрама'), ('Музыка'), ('Мультфильм'), ('Мюзикл'),
   ('Приключения'), ('Семейный'), ('Спорт'), ('Триллер'), ('Ужасы'), ('Фантастика'), ('Фэнтези')*/

insert into RATES (name_rate) values
('G'), ('PG'), ('PG-13'), ('R'), ('NC-17');
insert into FILMS (name, description, release_date, duration,rates)
values (
    'Фунтик',
    'Приключения поросёнка Фунтика» — советский четырёх серийный мультфильм. ',
    '1986-02-03',
    40,
    1
);
insert into FILM_GENRE (film_id,genre_id)
values(1,1),(1,2);

insert into USERS
(EMAIL, LOGIN, NAME, BIRTHDAY)
values('Kameron_Parker@gmail.com', 'RSoL1EvfmT', 'Joan Cronin', '1995-07-02');
INSERT INTO USERS
(EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES('Garett49@gmail.com', 'l3vLMJHTfB', 'Kristopher Davis', '1974-01-19');
INSERT INTO USERS
(EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES('Agustin_Stiedemann45@gmail.com', 'u8AIlf1AhW', 'Wilbert Mosciski', '1977-05-26');
INSERT INTO USERS
(EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES('Elizabeth1@gmail.com', '1UOdnqvbrE', 'Angelica Schaefer', '1991-10-30');
INSERT INTO USERS
(EMAIL, LOGIN, NAME, BIRTHDAY)
VALUES('Joshuah.Grady@gmail.com', 'aDajHl36Mu', 'Maria Bayer', '1990-05-06');