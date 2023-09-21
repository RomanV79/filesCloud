package ru.vlasov.fileclouds.repository.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class MinioDto {
    boolean isDir;
    String name;
    String parent;
    String fullPath;


}
