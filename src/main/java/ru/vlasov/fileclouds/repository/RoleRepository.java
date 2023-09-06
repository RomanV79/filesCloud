package ru.vlasov.fileclouds.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.vlasov.fileclouds.user.Role;

public interface RoleRepository extends JpaRepository<Role, Long> {
    Role findRoleByName(String name);
}
