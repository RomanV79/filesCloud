package ru.vlasov.fileclouds.repository;

import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.test.context.ContextConfiguration;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import ru.vlasov.fileclouds.FilesCloudApplication;
import ru.vlasov.fileclouds.customException.UserExistException;
import ru.vlasov.fileclouds.service.AppUserServiceImpl;
import ru.vlasov.fileclouds.user.AppUser;
import ru.vlasov.fileclouds.web.dto.UserDto;

import java.util.List;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ContextConfiguration(classes = FilesCloudApplication.class)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Testcontainers
public class AppUserRepositoryAndServiceTest {

    @Autowired
    private AppUserRepository userRepository;
    @Autowired
    private AppUserServiceImpl userService;

    @Container
    @ServiceConnection
    public static final MySQLContainer<?> mySQLContainer = new MySQLContainer<>("mysql:8")
            .withDatabaseName("testBase")
            .withUsername("test")
            .withPassword("test");

    @Test
    @Order(1)
    void testMySQLContainerIsRunning() {
        assertThat(mySQLContainer.isRunning()).isTrue();
    }

    @Test
    @Order(2)
    void clean_db_must_be_empty() {
        List<AppUser> users = userRepository.findAll();
        assertThat(users.isEmpty()).isTrue();
    }

    @Test
    @Order(10)
    void users_registered_in_db() {
        try {
            userService.save(new UserDto("Bob", "1234567"));
            userService.save(new UserDto("Kate", "wqwertyyu"));
            userService.save(new UserDto("John", "09876poiuy"));
        } catch (UserExistException e) {
            throw new RuntimeException(e);
        }

        List<AppUser> users = userRepository.findAll();
        assertThat(users.size() == 3).isTrue();
    }

    @Test
    @Order(20)
    void get_right_user_by_email() {
        AppUser appUserByLogin = userService.findAppUserByLogin("Kate");
        String username = "Kate";
        assertThat(appUserByLogin.getLogin().equals(username)).isTrue();
    }

    @Test
    @Order(30)
    void insert_existed_user() {
        assertThrows(UserExistException.class,
        () -> {
            userService.save(new UserDto("Bob", "298923f29fh"));
        });
    }
}