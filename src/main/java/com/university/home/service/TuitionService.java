package com.university.home.service;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.GradeTotalDto;
import com.university.home.entity.BreakApp;
import com.university.home.entity.CollTuit;
import com.university.home.entity.Scholarship;
import com.university.home.entity.StuSch;
import com.university.home.entity.StuStat;
import com.university.home.entity.Student;
import com.university.home.entity.Tuition;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.CollTuitRepository;
import com.university.home.repository.ScholarshipRepository;
import com.university.home.repository.StuSchRepository;
import com.university.home.repository.TuitionRepository;

import jakarta.transaction.Transactional;

@Service
public class TuitionService {

	@Autowired
	ScholarshipRepository scholarshipRepository;
	@Autowired
	TuitionRepository tuitionRepository;
	@Autowired
	StudentService studentService;
	@Autowired
	StuStatService stuStatService;
	@Autowired
	BreakAppService breakAppService;
	@Autowired
	CollTuitRepository collTuitRepository;
	@Autowired
	StuSchRepository stuSchRepository;
	@Autowired
	StuSubService stuSubService;
	
	 public List<Tuition> tuitionList(Long studentId) {
	        return tuitionRepository.findByStudentId(studentId);
    }

    public List<Tuition> getStatusList(Long studentId, Boolean status) {
        return tuitionRepository.findByStudentIdAndStatus(studentId, status);
    }

    public Tuition getSemester(Long studentId ,Long tuiYear ,Long semester) {
        return tuitionRepository.findByStudentIdAndTuiYearAndSemester(studentId, tuiYear, semester)
                .orElseThrow(() -> new CustomRestfullException("등록금 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
    }
    @Transactional
    public void updateStatus(Long studentId) {
    	Long currentyear = (long)LocalDate.now().getYear();
    	Long currentSemester =(LocalDate.now().getMonthValue() <= 6) ? 1L : 2L;
    	
    	Tuition tuition = tuitionRepository.findByStudentIdAndTuiYearAndSemester(studentId, currentyear, currentSemester)
    			.orElseThrow(() -> new CustomRestfullException("등록금 내역이 존재하지 않습니다.", HttpStatus.NOT_FOUND));
    	if (Optional.ofNullable(tuition.getStatus()).orElse(false)) {
    	    throw new CustomRestfullException("이미 납부된 상태입니다.", HttpStatus.BAD_REQUEST);
    	}
    	tuition.setStatus(true);
    	
    	StuStat stuStat = stuStatService.getCurrentStatus(studentId);
    	if ("휴학".equals(stuStat.getStatus())) {
			stuStatService.updateStatus(stuStat.getStudent(),"재학", null);
		}
    	
    }
    private int getCurrentSemester() {
        return (LocalDate.now().getMonthValue() <= 6) ? 1 : 2;
    }
    public Long getTuitionAmount(Long studentId) {
        Student student = studentService.getStudentByIdEntity(studentId);
        Long collegeId = student.getDepartment().getCollege().getId();

        CollTuit collTuit = collTuitRepository.findById(collegeId)
                .orElseThrow(() -> new RuntimeException("등록금 정보 없음"));

        return collTuit.getAmount();
    }
    @Transactional
    public int createTuition(Long studentId) {
    	Student student = studentService.getStudentByIdEntity(studentId);
    	
    	StuStat stuStat = stuStatService.getCurrentStatus(studentId);
    	 
    	if (stuStat.getStatus().equals("졸업") || stuStat.getStatus().equals("자퇴")) {
			return 0;
		}
    	
    	List<BreakApp> breakApps = breakAppService.getByStudent(studentId);
    	int currentYear = LocalDate.now().getYear();
    	int currentSemester = getCurrentSemester();
    	for (BreakApp b : breakApps) {
			if (b.getStatus().equals("승인")) {
				if(b.getToYear() > LocalDate.now().getYear()) return 0;
				if(b.getToYear() == LocalDate.now().getYear() && b.getToSemester() >= getCurrentSemester()) return 0;
			}
		}
    	 if (tuitionRepository.findByStudentIdAndTuiYearAndSemester(studentId, (long)currentYear, (long)currentSemester).isPresent()) {
    	        return 0;
    	    }

    	 Long collegeId = student.getDepartment().getCollege().getId();
    	 CollTuit collTuit = collTuitRepository.findById(collegeId)
    	            .orElseThrow(() -> new RuntimeException("등록금 정보 없음"));
    	 Long tuiAmount = collTuit.getAmount();
    	 
    	// 직전 학기 성적 리스트를 가져옵니다.
    	    List<GradeTotalDto> gradeHistory = stuSubService.readGradeInquiryList(studentId);
    	    
    	    if (!gradeHistory.isEmpty()) {
    	        // 가장 최근 학기 성적 DTO (마지막 요소)
    	        GradeTotalDto latestGrade = gradeHistory.get(gradeHistory.size() - 1);
    	        double gpa = latestGrade.getAverageScore();
    	        
    	        Integer determinedSchType = null;
    	        if (gpa >= 4.2) {
    	            determinedSchType = 1; // 성적우수 A
    	        } else if (gpa >= 3.7) {
    	            determinedSchType = 2; // 성적우수 B
    	        }

    	        // 조건에 맞는 장학금이 있다면 StuSch 테이블에 먼저 저장
    	        if (determinedSchType != null) {
    	            // 해당 유형의 Scholarship 엔티티 조회 (id가 1, 2라고 가정하거나 type명으로 조회)
    	            Scholarship scholarship = scholarshipRepository.findById(Long.valueOf(determinedSchType)).orElse(null);
    	            
    	            if (scholarship != null) {
    	                StuSch newStuSch = new StuSch();
    	                newStuSch.setStudent(student);
    	                newStuSch.setScholarshipType(scholarship);
    	                newStuSch.setSchYear((long)currentYear);
    	                newStuSch.setSemester((long)currentSemester);
    	                stuSchRepository.save(newStuSch); // 장학금 수혜 내역 저장
    	            }
    	        }
    	    }
    	 
    	 List<StuSch> stuSchs = stuSchRepository.findByStudentIdAndSchYearAndSemester(studentId, (long) currentYear, (long) currentSemester);
    	 Long schAmount = 0L;
    	 Scholarship schType = null;
    	 
    	 if (!stuSchs.isEmpty()) {
    		    // 여러 장학금이 있을 경우 가장 큰 장학금 사용
		    StuSch bestSch = stuSchs.stream()
		            .max((a, b) -> Long.compare(
		                    a.getScholarshipType() != null ? a.getScholarshipType().getMaxAmount() : 0,
		                    b.getScholarshipType() != null ? b.getScholarshipType().getMaxAmount() : 0))
		            .get();

		    if (bestSch.getScholarshipType() != null) {
		        schType = bestSch.getScholarshipType();
		        schAmount = Math.min(bestSch.getScholarshipType().getMaxAmount(), tuiAmount); // 등록금보다 크면 등록금 한도로
		    }
		}
    	 System.out.println("stuSchs.size() = " + stuSchs.size());
    	 if (schType != null) {
    	     System.out.println("schType = " + schType.getType() + ", schAmount = " + schAmount);
    	 } else {
    	     System.out.println("schType is null");
    	 }

    	 Tuition tuition = new Tuition();
	    tuition.setStudent(student);
	    tuition.setTuiYear((long) currentYear);
	    tuition.setSemester((long) currentSemester);
	    tuition.setTuiAmount(tuiAmount);
	    tuition.setScholarshipType(schType);
	    tuition.setSchAmount(schAmount);
	    if (tuiAmount - schAmount <= 0) {
	        tuition.setStatus(true); // 자동 납부 완료 처리
	    } else {
	        tuition.setStatus(false); // 미납 상태
	    } // 기본 납부 상태 false

	    // 8. 저장
	    tuitionRepository.save(tuition);
	    
	    return 1;
    }
}
