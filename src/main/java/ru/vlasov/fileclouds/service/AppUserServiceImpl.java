package ru.vlasov.fileclouds.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import ru.vlasov.fileclouds.customException.UserExistException;
import ru.vlasov.fileclouds.repository.AppUserRepository;
import ru.vlasov.fileclouds.repository.RoleRepository;
import ru.vlasov.fileclouds.user.AppUser;
import ru.vlasov.fileclouds.user.Role;
import ru.vlasov.fileclouds.web.dto.UserDto;

import java.util.Optional;
import java.util.Set;

@Service
public class AppUserServiceImpl implements AppUserService {

    private final AppUserRepository appUserRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    public AppUserServiceImpl(AppUserRepository appUserRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder) {
        this.appUserRepository = appUserRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
    }


    @Override
    public AppUser save(UserDto userDto) throws UserExistException {
        AppUser appUser = new AppUser();
        appUser.setLogin(userDto.getUsername());
        appUser.setPassword(passwordEncoder.encode(userDto.getPassword()));

        Role role = roleRepository.findRoleByName("ROLE_USER");
        if (role == null) {
            role = createNewRole();
        }
        appUser.setRoles(Set.of(role));
        try {
            appUserRepository.save(appUser);
        } catch (DataIntegrityViolationException e) {
            throw new UserExistException("User with this Login already exists");
        }

        return appUser;
    }

    private Role createNewRole() {
        Role role = new Role();
        role.setName("ROLE_USER");
        return roleRepository.save(role);
    }

    @Override
    public AppUser findAppUserByLogin(String login) {
        Optional<AppUser> appUserOptional = appUserRepository.findAppUserByLogin(login);
        if (appUserOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return appUserOptional.get();
    }
}
