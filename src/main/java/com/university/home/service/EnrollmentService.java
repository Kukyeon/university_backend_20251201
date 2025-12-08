package com.university.home.service;

import com.university.home.entity.StuSub;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.SubjectRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final StuSubRepository stuSubRepository;
    private final SubjectRepository subjectRepository;
    private final StudentRepository studentRepository;

    @Transactional
    public void enroll(Long studentId, Long subjectId) {
        // 1. 정보 조회
        Student student = studentRepository.findById(studentId)
                .orElseThrow(() -> new IllegalArgumentException("학생 정보가 없습니다."));
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("강의 정보가 없습니다."));

        // 2. 중복 체크
        if (stuSubRepository.existsByStudentIdAndSubjectId(studentId, subjectId)) {
            throw new IllegalStateException("이미 수강신청한 강의입니다.");
        }

        // 3. 정원 체크
        if (subject.getNumOfStudent() >= subject.getCapacity()) {
            throw new IllegalStateException("정원이 초과되었습니다.");
        }

        // 4. [수정됨] 최대 학점 체크 (Java 로직으로 변경)
        // (1) 이번 학기 수강 내역을 리스트로 다 가져옵니다.
        List<StuSub> mySubjects = stuSubRepository.findByStudentIdAndSubjectSubYearAndSubjectSemester(
                studentId, 2025L, 1L); // 연도, 학기는 상황에 맞게 변경
        
     // (2) 자바 스트림으로 학점 합계 계산
        int currentCredits = mySubjects.stream()
                .mapToInt(ss -> ss.getSubject().getGrades() != null ? ss.getSubject().getGrades().intValue() : 0)
                .sum();

        if (currentCredits + subject.getGrades().intValue() > 18) { // 여기도 비교할 때 .intValue() 추가
            throw new IllegalStateException("신청 가능한 최대 학점(18학점)을 초과했습니다.");
        }

        // 5. 저장
        StuSub newEnrollment = new StuSub();
        newEnrollment.setStudent(student);
        newEnrollment.setSubject(subject);
        stuSubRepository.save(newEnrollment);

        // 6. 인원 증가
        subject.setNumOfStudent(subject.getNumOfStudent() + 1);
    }

    // 취소 로직은 기존과 동일...
    @Transactional
    public void cancel(Long studentId, Long subjectId) {
        StuSub enrollment = stuSubRepository.findByStudentIdAndSubjectId(studentId, subjectId)
                .orElseThrow(() -> new IllegalArgumentException("수강 내역이 없습니다."));
        
        Subject subject = enrollment.getSubject();
        subject.setNumOfStudent(subject.getNumOfStudent() - 1);
        
        stuSubRepository.delete(enrollment);
    }
}