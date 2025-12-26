package com.university.home.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.List;

@Data
@AllArgsConstructor
public class GeminiRequestDto {

    private List<Content> contents;

    @Data
    @AllArgsConstructor
    public static class Content {
        private List<Part> parts;
    }

    @Data
    @AllArgsConstructor
    public static class Part {
        private String text;
    }

    public static GeminiRequestDto of(String message) {
        return new GeminiRequestDto(
                List.of(new Content(
                        List.of(new Part(message))
                ))
        );
    }
}
