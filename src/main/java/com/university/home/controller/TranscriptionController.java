//package com.university.home.controller;
//
//import com.university.home.service.TranscriptionService;
//import lombok.RequiredArgsConstructor;
//
//import java.util.Map;
//
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//@RestController
//@RequestMapping("/api/transcribe")
//@RequiredArgsConstructor
//public class TranscriptionController {
//
//    private final TranscriptionService transcriptionService;
//
//    @PostMapping("/start")
//    public ResponseEntity<?> startTranscription(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("scheduleId") Long scheduleId
//    ) {
//        try {
//
//            // === Null 체크 추가 ===
//            if (file == null || file.isEmpty()) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "error", "Invalid file",
//                        "message", "업로드된 파일이 없습니다."
//                ));
//            }
//
//            if (scheduleId == null) {
//                return ResponseEntity.badRequest().body(Map.of(
//                        "error", "Invalid scheduleId",
//                        "message", "scheduleId 값이 필요합니다."
//                ));
//            }
//
//            // === 실제 작업 ===
//            String jobName = transcriptionService.startTranscriptionJob(file, scheduleId);
//            return ResponseEntity.ok(Map.of("jobName", jobName));
//
//        } catch (Exception e) {
//            return ResponseEntity.status(500).body(Map.of(
//                    "error", "Transcription start failed",
//                    "message", e.getMessage()
//            ));
//        }
//    }
//}
