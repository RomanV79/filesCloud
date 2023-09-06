package ru.vlasov.fileclouds.SecurityConfig;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import ru.vlasov.fileclouds.repository.AppUserRepository;
import ru.vlasov.fileclouds.user.AppUser;

import java.util.Optional;

@Service
public class AppUserDetailsService implements UserDetailsService {

    private final AppUserRepository appUserRepository;

    public AppUserDetailsService(AppUserRepository appUserRepository) {
        this.appUserRepository = appUserRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Optional<AppUser> appUserOptional = appUserRepository.findAppUserByLogin(username);
        if (appUserOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found");
        }
        return new AppUserDetails(appUserOptional.get());
    }


}
