package ru.vlasov.fileclouds.web.dto;


import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Map;

@Slf4j
@Component
public class Util {

    public static StorageDto convertItemToStorageDto(Item item) {
//        log.info("item.objectName() -> {}", item.objectName());
//        log.info("item.isDir() -> {}", item.isDir());
//        log.info("item.lastModified() -> {}", item.lastModified());


        StorageDto storageDto = new StorageDto();
        String[] fullPaths = item.objectName().split("/");
        log.info("fullPaths -> {}", Arrays.toString(fullPaths));
        String[] paths = Arrays.copyOfRange(fullPaths, 1, fullPaths.length);
        log.info("paths -> {}", Arrays.toString(paths));

        if (paths.length == 0) return null;

        if (item.isDir()) {
            storageDto.setName(paths[paths.length - 1] + "/");
        } else {
            storageDto.setName(paths[paths.length - 1]);
        }

        FilePath filePath = new FilePath();
        Breadcrumbs breadcrumbs = new Breadcrumbs();
        if (paths.length > 1) {
            for (int i = 0; i < paths.length - 1; i++) {
                filePath.getSegments().add(paths[i]);
            }

            for (int i = 0; i < paths.length - 1; i++) {
                StringBuilder builder = new StringBuilder();
                for (int j = 0; j <= i; j++) {
                    builder.append(paths[j]).append("/");
                }
                breadcrumbs.getSegments().put(paths[i], builder.toString());
            }
        }
        storageDto.setFilePath(filePath);
        storageDto.setBreadcrumbs(breadcrumbs);
        int i = 0;
        for (Map.Entry<String, String> pair:storageDto.getBreadcrumbs().getSegments().entrySet()) {
            log.info("key : value ==> {} : {}", pair.getKey(), pair.getValue());
        }

        storageDto.setDir(item.isDir());

        if (item.isDir()) {
            storageDto.setLastModified("");
        } else {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            storageDto.setLastModified(item.lastModified().format(formatter));
        }

        return storageDto;
    }
}
