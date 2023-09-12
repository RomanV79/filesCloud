package ru.vlasov.fileclouds.web.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class Breadcrumbs {
    private Map<String, String> segments = new HashMap<>();
}
