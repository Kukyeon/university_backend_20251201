package com.university.home.dto;

import java.util.ArrayList;
import java.util.List;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class GeminiRequestDto {
	private List<Content> contents = new ArrayList<>();
	
	// 생성자: 문자열 하나만 넣으면 알아서 포맷 맞춰줌
    public GeminiRequestDto(String text) {
        Part part = new Part(text);
        Content content = new Content();
        content.getParts().add(part);
        this.contents.add(content);
    }
    
    @Data
    public static class Content {
        private List<Part> parts = new ArrayList<>();
    }

    @Data
    public static class Part {
        private String text;
        public Part(String text) { this.text = text; }
    }
}
