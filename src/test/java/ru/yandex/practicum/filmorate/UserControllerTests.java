package ru.yandex.practicum.filmorate;

import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import ru.yandex.practicum.filmorate.controller.UserController;
import ru.yandex.practicum.filmorate.exception.ValidationException;
import ru.yandex.practicum.filmorate.model.User;

import java.time.LocalDate;
import java.util.Collection;

import static org.junit.jupiter.api.Assertions.*;

class UserControllerTests {

    private UserController userController;
    private static Validator validator;

    @BeforeEach
    void beforeEach() {
        userController = new UserController();
        ValidatorFactory factory = Validation.buildDefaultValidatorFactory();
        validator = factory.getValidator();
    }

    @Test
    void testPostUserIsValid() {
        User user = createValidUser();

        User createdUser = userController.addUser(user);

        assertNotNull(createdUser.getId(), "ID пользователя не должен быть null");
        assertEquals(1L, createdUser.getId(), "Первый ID должен быть равен 1");
        assertEquals(1, userController.getUsers().size(), "В списке должен быть 1 пользователь");
    }

    @Test
    void testPostExceptionWhenEmailNotContainSymbol() {
        User user = createValidUser();
        user.setEmail("username.ru"); // Содержит символ @
        assertFalse(validator.validate(user).isEmpty(), "Не должно пройти валидацию, валидатор не пуст.");
    }

    @Test
    void testPostUserWhenLoginContainSpace() {
        User user = createValidUser();
        user.setLogin("vladimir lenin"); // Содержит пробел

        assertFalse(validator.validate(user).isEmpty(), "Не должно пройти валидацию, валидатор не пуст.");
    }

    @Test
    void testPostUserWhenLoginIsBlack() {
        User user = createValidUser();
        user.setLogin("   "); // Пустая строка

        assertFalse(validator.validate(user).isEmpty(), "Не должно пройти валидацию, валидатор не пуст.");
    }

    @Test
    void testPostWhenNameIsEmptyOrNull() {
        User user = createValidUser();
        user.setName(""); // Пустое имя

        User createdUser = userController.addUser(user);

        assertEquals(user.getLogin(), createdUser.getName(), "Имя должно совпадать с логином, если оно пустое");
        user.setName(null);

        createdUser = userController.addUser(user);
        assertEquals(user.getLogin(), createdUser.getName(), "Имя должно совпадать с логином, если оно пустое");
    }

    @Test
    void testPostUserWhenBirthdayInFuture() {
        User user = createValidUser();
        user.setBirthday(LocalDate.now().plusDays(1));

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userController.addUser(user)
        );
        assertEquals("Дата рождения не может быть больше текущей даты", exception.getMessage());
    }


    @Test
    void testSuccessUpdateWhenUserExistsAndValid() {
        User originalUser = userController.addUser(createValidUser());

        User updatedData = new User();
        updatedData.setId(originalUser.getId());
        updatedData.setLogin("newLogin");
        updatedData.setName("Новое Имя");
        updatedData.setEmail("new@yandex.ru");
        updatedData.setBirthday(LocalDate.of(2000, 5, 5));

        User result = userController.editingUser(updatedData);

        assertEquals("Новое Имя", result.getName());
        assertEquals("newLogin", result.getLogin());
        assertEquals(1, userController.getUsers().size());
    }

    @Test
    void testPutUserWhenIdIsNull() {
        User user = createValidUser();
        user.setId(null);

        ValidationException exception = assertThrows(ValidationException.class, () ->
                userController.editingUser(user)
        );
        assertEquals("Id должен быть указан", exception.getMessage());
    }

    @Test
    void testPutUserWhenUserDoesNotExist() {
        User user = createValidUser();
        user.setId(999L);

        RuntimeException exception = assertThrows(RuntimeException.class, () ->
                userController.editingUser(user)
        );
        assertEquals("Пользователь id = 999 не найден.", exception.getMessage());
    }


    @Test
    void testGetUserWhenNoUsersAdded() {
        Collection<User> users = userController.getUsers();
        assertTrue(users.isEmpty(), "Изначально список пользователей должен быть пуст");
    }

    private User createValidUser() {
        User user = new User();
        user.setLogin("Loginov");
        user.setName("Иван");
        user.setEmail("user@yandex.ru");
        user.setBirthday(LocalDate.of(1995, 12, 28));
        return user;
    }
}
