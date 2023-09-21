package ru.vlasov.fileclouds.service;

import ru.vlasov.fileclouds.customException.UserExistException;
import ru.vlasov.fileclouds.user.AppUser;
import ru.vlasov.fileclouds.web.dto.UserDto;

public interface AppUserService {
    AppUser save(UserDto userDto) throws UserExistException;

    AppUser findAppUserByLogin(String login);

}
