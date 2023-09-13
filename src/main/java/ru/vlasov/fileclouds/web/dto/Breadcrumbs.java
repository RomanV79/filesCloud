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
public class Breadcrumbs {
    private List<String> listSegments = new ArrayList<>();
    private Map<String, String> linkSegments = new HashMap<>();
}
