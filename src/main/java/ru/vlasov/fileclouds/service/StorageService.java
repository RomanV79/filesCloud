package ru.vlasov.fileclouds.service;

import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import ru.vlasov.fileclouds.config.security.AppUserDetails;
import ru.vlasov.fileclouds.customException.BrokenFileException;
import ru.vlasov.fileclouds.customException.EmptyFolderException;
import ru.vlasov.fileclouds.customException.StorageErrorException;
import ru.vlasov.fileclouds.repository.MinioRepository;
import ru.vlasov.fileclouds.web.dto.StorageDto;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class StorageService {

    private final MinioRepository minioRepository;

    @Autowired
    public StorageService(MinioRepository minioRepository) {
        this.minioRepository = minioRepository;
    }

    public void uploadFile(String path, MultipartFile multipartFile) throws BrokenFileException, StorageErrorException {
        String fullPath = getRootUserFolder() + path;
        minioRepository.uploadFile(fullPath, multipartFile);
    }

    public void createDirectory(String path) throws StorageErrorException {
        String fullPath = getRootUserFolder() + path;
        minioRepository.createDirectory(fullPath);
    }

    public void createRootUserDirectory(String rootDirectory) throws StorageErrorException {
        minioRepository.createDirectory(rootDirectory);
    }

    public List<StorageDto> getFilesAndDirsForCurrentPath(String directory) throws StorageErrorException, EmptyFolderException {
        String fullPath = getRootUserFolder() + directory;
        List<StorageDto> dtoList = minioRepository.getAllObjectListFromDirIncludeInternal(fullPath, false);
        if (isDirEmpty(dtoList)) throw new EmptyFolderException("Empty");

        return sortIsDirThenLastModified(dtoList);
    }

    @NotNull
    private static List<StorageDto> sortIsDirThenLastModified(List<StorageDto> storageDtoList) {
        return storageDtoList.stream().sorted(Comparator.comparing(StorageDto::isDir).reversed().thenComparing(StorageDto::getLastModified)).collect(Collectors.toList());
    }

    public void delete(String path) throws StorageErrorException {
        if (path == null) path = "";
        String fullPath = getRootUserFolder() + path;
        if (!isDir(path)) {
            minioRepository.delete(fullPath);
        } else {

            List<StorageDto> dtoList = minioRepository.getAllObjectListFromDirIncludeInternal(fullPath, true);
            if (!dtoList.isEmpty()) {
                for (StorageDto element:dtoList) {
                        String elementPath = getRootUserFolder() + element.getParentDirPath() + element.getName();
                        minioRepository.delete(elementPath);
                }
            }
            minioRepository.delete(fullPath);
        }
    }

    public void rename(String oldName, String newName, String path) throws StorageErrorException {
        if (!isDir(oldName)) {
            newName = checkAndMakeNameWithPostfix(newName, oldName);
            minioRepository.copy(getRootUserFolder() + path + newName, getRootUserFolder() + path + oldName);
            minioRepository.delete(getRootUserFolder() + path + oldName);
        } else {
            newName = checkAndMakeNameWithSlash(newName);

            String destPath = getRootUserFolder() + path + newName;
            String sourcePath = getRootUserFolder() + path + oldName;
            List<StorageDto> dtoList = minioRepository.getAllObjectListFromDirIncludeInternal(sourcePath, true);
            minioRepository.createDirectory(destPath);
            minioRepository.delete(sourcePath);
            if (!isDirEmpty(dtoList)) {
                for (StorageDto element : dtoList) {
                        String elementSourcePath = getRootUserFolder() + element.getParentDirPath() + element.getName();
                        String elementDestPath = elementSourcePath.replace(path + oldName, path + newName);
                        if (element.isDir()) {
                            minioRepository.createDirectory(elementDestPath);
                        } else {
                            minioRepository.copy(elementDestPath, elementSourcePath);
                        }
                        minioRepository.delete(elementSourcePath);
                }
            }
        }
    }

    public void uploadDirectory(String path, MultipartFile[] multipartFiles) throws BrokenFileException, StorageErrorException {
        Set<String> paths = new HashSet<>();
        for (MultipartFile file : multipartFiles) {
            if (Objects.requireNonNull(file.getOriginalFilename()).contains("/")) {
                String[] elements = file.getOriginalFilename().split("/");
                StringBuilder stringBuilder = new StringBuilder();
                for (int i = 0; i < elements.length - 1; i++) {
                    stringBuilder.append(elements[i]).append("/");
                    paths.add(stringBuilder.toString());
                }
            }
            minioRepository.uploadFile(getRootUserFolder() + path, file);
        }

        if (!paths.isEmpty()) {
            for (String element : paths) {
                minioRepository.createDirectory(getRootUserFolder() + element);
            }
        }
    }

    public InputStream download(String fullPath) throws StorageErrorException {
        return minioRepository.downloadFile(getRootUserFolder() + fullPath);
    }

    public void downloadZip(ZipOutputStream zipOut, String path) throws StorageErrorException, IOException {
        List<StorageDto> dtoList = minioRepository.getAllObjectListFromDirIncludeInternal(getRootUserFolder() + path, true);
        for (StorageDto element:dtoList) {
            if (!element.isDir()) {
                String currentName = (element.getParentDirPath() + element.getName()).substring(path.length());
                log.info("currentName -> {}", currentName);
                zipOut.putNextEntry(new ZipEntry(currentName));
                try (InputStream fis = minioRepository.downloadFile(getRootUserFolder() + element.getParentDirPath() + element.getName())) {
                    byte[] buffer = new byte[1024];
                    int len;
                    while ((len = fis.read(buffer)) > 0) {
                        zipOut.write(buffer, 0, len);
                    }
                }
                zipOut.closeEntry();
            }
        }
    }

    public List<StorageDto> getFilesAndDirectoriesForQuery(String query) throws StorageErrorException, EmptyFolderException {
        List<StorageDto> sortedList = new ArrayList<>();
        List<StorageDto> dtoList = minioRepository.getAllObjectListFromDirIncludeInternal(getRootUserFolder(), true);
        if (isDirEmpty(dtoList)) throw new EmptyFolderException("Empty");

        for (StorageDto element : dtoList) {
            if (element.getName().toLowerCase().contains(query.toLowerCase())) {
                sortedList.add(element);
            }
        }
        if (sortedList.isEmpty()) throw new EmptyFolderException("Empty");

        return sortIsDirThenLastModified(sortedList);
    }

    private static boolean isDir(String name) {
        return name.endsWith("/");
    }

    private static boolean isDirEmpty(List<StorageDto> itemList) {
        return itemList == null;
    }

    private String getRootUserFolder() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        AppUserDetails appUserDetails = (AppUserDetails) authentication.getPrincipal();
        return "user-" + appUserDetails.getAppUser().getId() + "-files/";
    }

    private String checkAndMakeNameWithPostfix(String newName, String oldName) {
        String[] oldNameSplit = oldName.split("\\.");
        String oldPostfix = oldNameSplit[oldNameSplit.length - 1];

        String[] newNameSplit = newName.split("\\.");
        String newPostfix = newNameSplit[newNameSplit.length - 1];

        if (!newPostfix.equals(oldPostfix)) {
            newName = newName + "." + oldPostfix;
        }

        return newName;
    }

    private String checkAndMakeNameWithSlash(String name) {
        return name.endsWith("/") ? name : name + "/";
    }
}
