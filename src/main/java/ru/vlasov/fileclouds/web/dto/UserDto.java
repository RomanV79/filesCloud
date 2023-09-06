package ru.vlasov.fileclouds.web.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class UserDto {
    @NotEmpty
    private String username;
    @NotEmpty(message = "Password should not be empty")
    private String password;
}
