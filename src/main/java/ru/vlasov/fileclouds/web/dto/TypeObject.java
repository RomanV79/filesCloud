package ru.vlasov.fileclouds.web.dto;

import lombok.Getter;

@Getter
public enum TypeObject {
    DIRECTORY("D"),
    FILES("F");

    TypeObject(String title) {
        this.title = title;
    }

    private String title;
}
