package ru.vlasov.fileclouds.repository.dto;

import lombok.*;

@Setter
@Getter
@NoArgsConstructor
@ToString
@AllArgsConstructor
public class TreeObjectMinio {
    boolean isDir;
    String name;
    String parent;
    String pathBeforeObj;
    String pathFromCurrentParent;
    String fullPathFromBucket;


}
