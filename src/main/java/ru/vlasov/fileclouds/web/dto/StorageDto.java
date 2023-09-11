package ru.vlasov.fileclouds.web.dto;

import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class StorageDto {
    private String name;
    private String path;
//    private TypeObject type;
    private boolean isDir;
    private LocalDateTime localDateTime;

}
