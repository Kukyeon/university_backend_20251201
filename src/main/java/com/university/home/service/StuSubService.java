package com.university.home.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import lombok.RequiredArgsConstructor;

import com.university.home.entity.PreStuSub;
import com.university.home.entity.StuSub;
import com.university.home.entity.Student; // Student 엔티티 import
import com.university.home.entity.Subject;
import com.university.home.repository.PreStuSubRepository;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.SubjectRepository;
import com.university.home.repository.StudentRepository; // Student 레포지토리 import

@Service
@RequiredArgsConstructor
public class StuSubService {

    private final SubjectRepository subjectRepository;
    private final PreStuSubRepository preStuSubRepository;
    private final StuSubRepository stuSubRepository;
    private final StudentRepository studentRepository; // 학생 조회를 위해 추가

    /**
     * 예비 수강 신청 -> 본 수강 신청 일괄 처리
     */
    @Transactional
    public void createStuSubByPreStuSub() {
        
        // 1. 예비 수강 신청 테이블의 모든 데이터를 가져옵니다.
        List<PreStuSub> allPreAppList = preStuSubRepository.findAll();

        // 2. 자바 Stream을 이용해 신청된 '과목 ID'만 중복 없이 추출
        List<Long> distinctSubjectIds = allPreAppList.stream()
                .map(pre -> pre.getId().getSubjectId())
                .distinct()
                .collect(Collectors.toList());

        // 3. 각 과목별로 순회
        for (Long subjectId : distinctSubjectIds) {
            
            // 강의 정보 조회 (Subject 객체 획득)
            Subject subject = subjectRepository.findById(subjectId).orElse(null);
            if (subject == null) continue;

            // 신청 인원 조회
            long applicantCount = preStuSubRepository.countByIdSubjectId(subjectId);

            // 4. (신청 인원 <= 정원) 조건 만족 시 자동 이관
            if (applicantCount <= subject.getCapacity()) {
                
                // 해당 과목을 신청한 예비 내역 리스트 가져오기
                List<PreStuSub> subjectPreList = preStuSubRepository.findByIdSubjectId(subjectId);
                
                for (PreStuSub pre : subjectPreList) {
                    
                    Long studentId = pre.getId().getStudentId();
                    
                    // 5. 중복 방지 (쿼리 메서드 이름 변경됨: ByStudent_Id...)
                    boolean exists = stuSubRepository.existsByStudent_IdAndSubject_Id(studentId, subjectId);
                    
                    if (!exists) {
                        // ★ 변경된 부분: ID가 아니라 객체를 채워 넣어야 함 ★
                        
                        // 5-1. 학생 엔티티 조회
                        Student student = studentRepository.findById(studentId).orElse(null);
                        
                        if (student != null) {
                            StuSub stuSub = new StuSub();
                            
                            // 외래키 객체 설정 (@ManyToOne 관계)
                            stuSub.setStudent(student); // 학생 객체 주입
                            stuSub.setSubject(subject); // 위에서 찾은 강의 객체 재활용 주입
                            
                            // 필요한 경우 기본값 설정
                            // stuSub.setGrade(null); 
                            // stuSub.setCompleteGrade(null);

                            // DB 저장 (id는 Auto Increment로 자동 생성됨)
                            stuSubRepository.save(stuSub);
                        }
                    }
                }
            }
        }
    }
}