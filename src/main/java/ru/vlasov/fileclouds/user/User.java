package ru.vlasov.fileclouds.user;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
@Entity
@Table(name = "users")
public class User {
    @Id
    @SequenceGenerator(name = "user_seq", sequenceName = "user_seq", allocationSize = 1, initialValue = 1001)
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "user_seq")
    @Column(name = "id", updatable = false, nullable = false)
    private Long id;

    @Column(name = "login", nullable = false, unique = true)
    @NotBlank
    @Size(max = 128)
    private String login;

    @Column(name = "password", nullable = false)
    @ToString.Exclude
    @NotBlank
    @Size(min = 6, max = 128)
    private String password;

}
