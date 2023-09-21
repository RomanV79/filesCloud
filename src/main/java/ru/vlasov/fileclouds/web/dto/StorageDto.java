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
    private boolean isDir;
    private String name;
    private String parentDirPath;
    private FilePath filePath;
    private String lastModified;
    private Long size;

}
