package ru.vlasov.fileclouds.web.dto;


import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.vlasov.fileclouds.repository.dto.TreeObjectMinio;

import java.time.format.DateTimeFormatter;
import java.util.Arrays;

@Slf4j
@Component
public class Util {

    public static StorageDto convertItemToStorageDto(Item item) {

        // если объект не директория но его имя заканчивается на "/" - значит это объект призрак созданный для пустой директории
        if (!item.isDir() && item.objectName().endsWith("/")) return null;

        StorageDto storageDto = new StorageDto();
        String[] fullPaths = item.objectName().split("/");
        String[] paths = Arrays.copyOfRange(fullPaths, 1, fullPaths.length);

        if (paths.length == 0) return null;

        if (item.isDir()) {
            storageDto.setName(paths[paths.length - 1] + "/");
            storageDto.setLastModified("");
            storageDto.setSize(0L);
        } else {
            storageDto.setName(paths[paths.length - 1]);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            storageDto.setLastModified(item.lastModified().format(formatter));

            storageDto.setSize(item.size());
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
                breadcrumbs.getLinkSegments().put(paths[i], builder.toString());
            }
        }
        storageDto.setFilePath(filePath);
        storageDto.setDir(item.isDir());

        return storageDto;
    }

    public static TreeObjectMinio convertItemToTreeObjectMinio(Item item) {
        TreeObjectMinio object = new TreeObjectMinio();

        object.setDir(item.isDir());
        object.setParent(getParent(item.objectName()));
        object.setName(getName(item.objectName()));



        return object;
    }

    public static Breadcrumbs getBreadcrumbs(String path) {
        if (path.isEmpty()) {
            return null;
        }
        String[] paths = path.split("/");
        Breadcrumbs breadcrumbs = new Breadcrumbs();
        for (int i = 0; i < paths.length; i++) {
            breadcrumbs.getListSegments().add(paths[i]);
            StringBuilder builder = new StringBuilder();
            for (int j = 0; j <= i; j++) {
                builder.append(paths[j]).append("/");
            }
            breadcrumbs.getLinkSegments().put(paths[i], builder.toString());
        }

        return breadcrumbs;
    }

    private static String getName(String path) {
        String[] element = path.split("/");
        return element[element.length - 1];
    }

    private static String getParent(String path) {
        String[] element = path.split("/");
        return element[element.length - 2];
    }
}
