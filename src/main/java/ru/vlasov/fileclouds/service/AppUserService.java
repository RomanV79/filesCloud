package ru.vlasov.fileclouds.service;

import ru.vlasov.fileclouds.customException.UserExistException;
import ru.vlasov.fileclouds.user.AppUser;
import ru.vlasov.fileclouds.web.dto.UserDto;

import java.util.List;

public interface AppUserService {
    void save(UserDto userDto) throws UserExistException;

    AppUser findAppUserByLogin(String login);

}
