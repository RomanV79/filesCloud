package ru.vlasov.fileclouds.web.dto;

import lombok.*;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.PrimitiveIterator;

@Getter
@Setter
@NoArgsConstructor
@ToString
public class StorageDto {
    private String name;
    private FilePath filePath;
    private Breadcrumbs breadcrumbs;
    private boolean isDir;
    private String lastModified;

}
