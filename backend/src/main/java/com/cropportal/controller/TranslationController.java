package com.cropportal.controller;

import com.cropportal.dto.TranslationRequest;
import com.cropportal.dto.TranslationResponse;
import com.cropportal.service.TranslationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/translation")
@RequiredArgsConstructor
public class TranslationController {
    private final TranslationService translationService;

    @PostMapping
    public TranslationResponse translate(@Valid @RequestBody TranslationRequest request) {
        return new TranslationResponse(translationService.translate(request.texts(), request.language()));
    }
}
