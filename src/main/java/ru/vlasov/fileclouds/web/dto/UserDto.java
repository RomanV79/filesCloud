package ru.vlasov.fileclouds.web.dto;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UserDto {
    private String login;
    private String password;
}
