package com.university.home.controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.annotation.AuthenticationPrincipal; 
import com.university.home.dto.PrincipalDto;
import com.university.home.dto.EvaluationDto;
import com.university.home.dto.MyEvaluationDto;
import com.university.home.dto.PrincipalDto;
import com.university.home.dto.QuestionDto;
import com.university.home.entity.Evaluation; 
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.CustomUserDetails;
import com.university.home.service.EvaluationService;
import com.university.home.service.QuestionService;
import com.university.home.utils.Define;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

// ⭐️ @RestController 사용
@RestController
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

//    private final HttpSession session;
    private final EvaluationService evaluationService;
    private final QuestionService questionService;

    // 평가 문항 조회
    @GetMapping("/questions")
    public ResponseEntity<QuestionDto> getEvaluationQuestions() {
        QuestionDto dto = questionService.getQuestions();
        return new ResponseEntity<>(dto, HttpStatus.OK);
    }
    
    // 평가 등록
    @PostMapping("/write/{subjectId}")
    public ResponseEntity<?> evaluationProc(
    		@PathVariable("subjectId") Long subjectId, 
    		@RequestBody @Valid EvaluationDto evaluationDto,
    		@AuthenticationPrincipal  CustomUserDetails loginUser) {
    	if (loginUser == null) {
            throw new CustomRestfullException("인증 정보가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }
    	 evaluationDto.setStudentId(loginUser.getUser().getId());
         evaluationDto.setSubjectId(subjectId);

        if (evaluationDto.getAnswer1() == null || evaluationDto.getAnswer2() == null ||
            evaluationDto.getAnswer3() == null || evaluationDto.getAnswer4() == null ||
            evaluationDto.getAnswer5() == null || evaluationDto.getAnswer6() == null ||
            evaluationDto.getAnswer7() == null) {
            throw new CustomRestfullException("모든 질문에 답 해주세요.", HttpStatus.BAD_REQUEST);
        }

        evaluationService.createEvaluation(evaluationDto);
        return new ResponseEntity<>(HttpStatus.CREATED); 
    }

    //교수 기준 전체 강의 평가 조회
    @GetMapping("/professor")
    public ResponseEntity<List<EvaluationDto>> getEvaluationByProfessor(
            @AuthenticationPrincipal CustomUserDetails loginUser) {

        if (loginUser == null) {
            throw new CustomRestfullException("인증 정보가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        Long professorId = loginUser.getUser().getId();
        List<EvaluationDto> evaluations = evaluationService.getEvaluationsByProfessorId(professorId);

        return ResponseEntity.ok(evaluations);
    }
    @GetMapping("/professor/subjects")
    public List<String> getProfessorSubjectList(@AuthenticationPrincipal CustomUserDetails loginUser) {
        Long professorId = loginUser.getUser().getId();
        return evaluationService.getSubjectsByProfessor(professorId);
    }

    //교수 기준 과목별 강의 평가 조회
    @GetMapping("/subject/{subjectName}")
    public ResponseEntity<List<EvaluationDto>> getEvaluationBySubject(@PathVariable("subjectName") String subjectName, @AuthenticationPrincipal CustomUserDetails loginUser) {
    	 if (loginUser == null) {
             throw new CustomRestfullException("인증 정보가 유효하지 않습니다.", HttpStatus.UNAUTHORIZED);
         }

         Long professorId = loginUser.getUser().getId();
         List<EvaluationDto> evaluations = evaluationService.getEvaluationsByProfessorAndSubject(professorId, subjectName);

         return ResponseEntity.ok(evaluations);
    }
    // 단일 평가 상세 조회 (평가 ID 기준)
    @GetMapping("/{id}")

    public ResponseEntity<Evaluation> getEvaluationDetail(@PathVariable Long id) {
        Evaluation evaluation = evaluationService.getEvaluationById(id);
        return ResponseEntity.ok(evaluation);
    }
}