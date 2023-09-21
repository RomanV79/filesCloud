package ru.vlasov.fileclouds.web.dto;


import io.minio.messages.Item;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.Locale;

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

        if (paths.length < 2) {
            storageDto.setParentDirPath("");
        } else {
            storageDto.setParentDirPath(getParentDirPath(paths));
        }
        if (item.isDir()) {
            storageDto.setName(paths[paths.length - 1] + "/");
            storageDto.setLastModified("");
            storageDto.setSize(null);
        } else {
            storageDto.setName(paths[paths.length - 1]);

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            storageDto.setLastModified(item.lastModified().format(formatter));

            storageDto.getSize().setSize(item.size());
            storageDto.getSize().setHumanReadableSize(getSizeForHuman(item.size()));
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

    private static String getSizeForHuman(long size) {
        String hrSize;

        double b = size;
        double k = size / 1024.0;
        double m = ((size / 1024.0) / 1024.0);
        double g = (((size / 1024.0) / 1024.0) / 1024.0);
        double t = ((((size / 1024.0) / 1024.0) / 1024.0) / 1024.0);

        DecimalFormatSymbols symbols = new DecimalFormatSymbols(Locale.getDefault());
        symbols.setDecimalSeparator('.');
        DecimalFormat dec = new DecimalFormat("0.0", symbols);

        if (t > 1) {
            hrSize = dec.format(t).concat(" TB");
        } else if (g > 1) {
            hrSize = dec.format(g).concat(" GB");
        } else if (m > 1) {
            hrSize = dec.format(m).concat(" MB");
        } else if (k > 1) {
            hrSize = dec.format(k).concat(" KB");
        } else {
            hrSize = dec.format(b).concat(" Bytes");
        }

        return hrSize;
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

    private static String getParentDirPath(String[] paths) {
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < paths.length - 1; i++) {
            builder.append(paths[i]).append("/");
        }
        return builder.toString();
    }
}
