package ru.vlasov.fileclouds.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class StorageDto {
    private String name;
    private FilePath filePath;
    private boolean isDir;
    private String lastModified;
    private Long size;

}
