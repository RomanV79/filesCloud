package ru.vlasov.fileclouds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlasov.fileclouds.user.AppUser;

import java.util.Optional;

public interface AppUserRepository extends JpaRepository<AppUser, Long> {
    Optional<AppUser> findAppUserByLogin(String login);
}
