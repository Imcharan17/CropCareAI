package com.cropportal.dto;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.util.List;

public record TranslationRequest(
        @NotNull
        @Pattern(regexp = "hi|te", message = "Language must be hi or te")
        String language,
        @NotEmpty
        @Size(max = 20)
        List<@NotNull @Size(max = 4000) String> texts
) {}
