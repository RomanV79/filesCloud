package ru.vlasov.fileclouds.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class FilePath {

    private List<String> segments = new ArrayList<>();

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        for (String segment:segments) {
            builder.append(segment).append("/");
        }
        return builder.toString();
    }
}
