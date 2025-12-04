package com.university.home.controller;

import java.util.List;



import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.university.home.dto.EvaluationDto;
import com.university.home.dto.MyEvaluationDto;
import com.university.home.dto.PrincipalDto;
import com.university.home.dto.QuestionDto;
import com.university.home.exception.CustomRestfullException;
import com.university.home.service.EvaluationService;
import com.university.home.service.QuestionService;
import com.university.home.utils.Define;

import jakarta.servlet.http.HttpSession;
import lombok.RequiredArgsConstructor;

@Controller
@RequestMapping("/api/evaluation")
@RequiredArgsConstructor
public class EvaluationController {

    private final HttpSession session;
    private final EvaluationService evaluationService;
    private final QuestionService questionService;

    @GetMapping("")
    public String evaluation(Model model, @RequestParam Long subjectId) {
        QuestionDto dto = questionService.getQuestions();
        model.addAttribute("subjectId", subjectId);
        model.addAttribute("dto", dto);
        return "evaluation/evaluation";
    }

    @PostMapping("/write/{subjectId}")
    public String evaluationProc(@PathVariable Long subjectId, EvaluationDto evaluationDto, Model model) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        evaluationDto.setStudentId(principal.getId());
        evaluationDto.setSubjectId(subjectId);

        if (evaluationDto.getAnswer1() == null) throw new CustomRestfullException("1번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        if (evaluationDto.getAnswer2() == null) throw new CustomRestfullException("2번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        if (evaluationDto.getAnswer3() == null) throw new CustomRestfullException("3번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        if (evaluationDto.getAnswer4() == null) throw new CustomRestfullException("4번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        if (evaluationDto.getAnswer5() == null) throw new CustomRestfullException("5번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        if (evaluationDto.getAnswer6() == null) throw new CustomRestfullException("6번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);
        if (evaluationDto.getAnswer7() == null) throw new CustomRestfullException("7번 질문에 답 해주세요", HttpStatus.BAD_REQUEST);

        evaluationService.createEvaluation(evaluationDto);
        model.addAttribute("type", 1);
        return "evaluation/evaluation";
    }

    @GetMapping("/read")
    public String readEvaluation(Model model) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        List<MyEvaluationDto> subjectNameList = evaluationService.getEvaluationsByProfessorId(principal.getId());
        model.addAttribute("subjectNameList", subjectNameList);
        model.addAttribute("evaluations", subjectNameList);
        return "evaluation/myEvaluation";
    }

    @PostMapping("/read")
    public String readEvaluationBySubject(Model model, @RequestParam String subjectName) {
        PrincipalDto principal = (PrincipalDto) session.getAttribute(Define.PRINCIPAL);
        if (principal == null) throw new CustomRestfullException("로그인이 필요합니다.", HttpStatus.UNAUTHORIZED);

        List<MyEvaluationDto> eval = evaluationService.getEvaluationsByProfessorAndSubject(principal.getId(), subjectName);
        model.addAttribute("subjectNameList", eval);
        model.addAttribute("evaluations", eval);
        return "evaluation/myEvaluation";
    }
}
