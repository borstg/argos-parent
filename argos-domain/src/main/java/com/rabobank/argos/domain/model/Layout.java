package com.rabobank.argos.domain.model;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
public class Layout {
    private List<String> authorizedKeyIds;
    private List<Step> steps;
}
