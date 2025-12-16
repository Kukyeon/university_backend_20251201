package com.university.home.service;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.CollegeDto;
import com.university.home.dto.DepartmentDto;
import com.university.home.dto.ProfessorDto;
import com.university.home.dto.StudentInfoForProfessor;
import com.university.home.dto.SubjectForProfessorDto;
import com.university.home.dto.UserUpdateDto;
import com.university.home.entity.College;
import com.university.home.entity.Department;
import com.university.home.entity.Professor;
import com.university.home.entity.StuSub;
import com.university.home.entity.StuSubDetail;
import com.university.home.entity.Student;
import com.university.home.entity.Subject;
import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.DepartmentRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.StuSubDetailRepository;
import com.university.home.repository.StuSubRepository;
import com.university.home.repository.StudentRepository;
import com.university.home.repository.SubjectRepository;

import jakarta.transaction.Transactional;

@Service
public class ProfessorService {

	@Autowired
	ProfessorRepository professorRepository;
	@Autowired
	UserService userService;
	@Autowired
	DepartmentRepository departmentRepository;
	@Autowired
	SubjectRepository subjectRepository;
	@Autowired
	StuSubRepository stuSubRepository;
	@Autowired
	StuSubDetailRepository stuSubDetailRepository;
	public ProfessorDto toDto(Professor professor) {
	    ProfessorDto dto = new ProfessorDto();
	    dto.setId(professor.getId());
	    dto.setName(professor.getName());
	    dto.setBirthDate(professor.getBirthDate());
	    dto.setGender(professor.getGender());
	    dto.setAddress(professor.getAddress());
	    dto.setTel(professor.getTel());
	    dto.setEmail(professor.getEmail());
	    dto.setHireDate(professor.getHireDate());

	    Department dep = professor.getDepartment();
	    if (dep != null) {
	        DepartmentDto depDto = new DepartmentDto();
	        depDto.setId(dep.getId());
	        depDto.setName(dep.getName());

	        College col = dep.getCollege();
	        if (col != null) {
	            CollegeDto colDto = new CollegeDto();
	            colDto.setId(col.getId());
	            colDto.setName(col.getName());
	            depDto.setCollege(colDto);
	        }

	        dto.setDepartment(depDto);
	    }

	    return dto;
	}
	@Transactional
	public ProfessorDto createProfessor(ProfessorDto dto) {
		Professor professor = new Professor();
		professor.setName(dto.getName());
		professor.setAddress(dto.getAddress());
		professor.setBirthDate(dto.getBirthDate());
		professor.setEmail(dto.getEmail());
		professor.setGender(dto.getGender());
		professor.setTel(dto.getTel());
		professor.setHireDate(LocalDate.now());
		//professor.setDepartment(dto.getDepartment());
		Department dept = departmentRepository.findById(dto.getDepartment().getId())
        .orElseThrow(() -> new RuntimeException("Department not found"));
		professor.setDepartment(dept);
		professorRepository.save(professor);
		
		User user =userService.createUser(professor.getId(), "professor");
		professor.setUser(user);
		professorRepository.save(professor);
		
		return toDto(professor);
	}
	@Transactional
	public ProfessorDto readProfessor(Long id) {
		Professor professor = professorRepository.findById(id)
				.orElseThrow(() -> new RuntimeException("Professor not found"));
		return toDto(professor);
	}

	@Transactional
	public List<Professor> getAllList () {
		return professorRepository.findAll();
	}
	@Transactional
	public boolean checkExistsForPasswordReset(Long id, String name, String email) {
		return professorRepository.existsByIdAndNameAndEmail(id, name, email);
	}
	@Transactional
	public Long findByNameEmail(String name, String email) {
		return professorRepository.findByNameAndEmail(name, email)
				.map(Professor::getId)
				.orElseThrow(() -> new RuntimeException("Professor not found"));
	}
	// 전체 교수 조회
	public Page<ProfessorDto> getProfessors(Pageable pageable) {
		return professorRepository.findAll(pageable)
                .map(this::toDto);
	}
	// 학과별 교수 조회
	public Page<ProfessorDto> getProfessorsByDep(Long deptId,Pageable pageable) {
		Department dept = departmentRepository.findById(deptId)
                .orElseThrow(() -> new RuntimeException("Department not found"));
		 return professorRepository.findByDepartment(dept, pageable)
	                .map(this::toDto);
	}
	// id 교수 조회
	public ProfessorDto getProfessorById(Long professorId) {
		Professor professor = professorRepository.findById(professorId)
                .orElseThrow(() -> new RuntimeException("Professor not found"));
        return toDto(professor);
    }
//	===============내 강의 조희 서비스!@!!@! ============
	public SubjectForProfessorDto toDto(Subject subject) {
	    SubjectForProfessorDto dto = new SubjectForProfessorDto();
	    dto.setId(subject.getId());
	    dto.setName(subject.getName());
	    dto.setSubDay(subject.getSubDay());
	    dto.setStartTime(subject.getStartTime());
	    dto.setEndTime(subject.getEndTime());
	    dto.setRoomId(subject.getRoom().getId());
	    return dto;
	}
	public StudentInfoForProfessor toDto(StuSubDetail detail) {
	    StudentInfoForProfessor dto = new StudentInfoForProfessor();
	    dto.setStuSubId(detail.getStuSub().getId());
	    dto.setStudentId(detail.getStudent().getId());
	    dto.setStudentName(detail.getStudent().getName());
	    dto.setDeptName(detail.getStudent().getDepartment().getName());
	    
	    dto.setAbsent(detail.getAbsent());
	    dto.setLateness(detail.getLateness());
	    dto.setHomework(detail.getHomework());
	    dto.setMidExam(detail.getMidExam());
	    dto.setFinalExam(detail.getFinalExam());
	    dto.setConvertedMark(detail.getConvertedMark());
	    
	    return dto;
	}
	public List<SubjectForProfessorDto> selectSubjectsByProfessor(Long professorId,Long subYear, Long semester) {
	    List<Subject> subjects;
	    
	    if (subYear != null && semester != null) {
	        subjects = subjectRepository.findByProfessor_IdAndSubYearAndSemester(professorId, subYear, semester);
	    } else {
	        subjects = subjectRepository.findByProfessor_Id(professorId);
	    }
	    
	    return subjects.stream()
	            .map(this::toDto)
	            .toList();
	}
	public List<StudentInfoForProfessor> selectStudentBySubject(Long subjectId){
		
		List<StuSubDetail> details = stuSubDetailRepository.findBySubject_Id(subjectId);
	
		return details.stream()
                .map(this::toDto)
                .toList();
	}

	@Transactional
	public StudentInfoForProfessor updateStudentGrade(Long stuSubId, StudentInfoForProfessor dto) {
	    StuSubDetail detail = stuSubDetailRepository.findById(stuSubId)
	            .orElseThrow(() -> new IllegalArgumentException("해당 학생 성적 정보가 없습니다."));
	    
	    detail.setAbsent(dto.getAbsent());
	    detail.setLateness(dto.getLateness());
	    detail.setHomework(dto.getHomework());
	    detail.setMidExam(dto.getMidExam());
	    detail.setFinalExam(dto.getFinalExam());

	    String grade;
	    Long converted;
	    // 결석 5회 이상이면 무조건 F
	    if (dto.getAbsent() != null && dto.getAbsent() >= 5) {
	        grade = "F";
	        converted = 0L; // 환산점수도 0 처리
	    } else {
	        // 환산점수 계산
	        converted = calculateConvertedMark(dto.getHomework(), dto.getMidExam(), dto.getFinalExam());
	        detail.setConvertedMark(converted);

	        // 등급 결정
	        grade = calculateGrade(converted);
	    }

	    detail.setConvertedMark(converted); // detail에 환산점수 저장
	    StuSub stuSub = detail.getStuSub();
	    stuSub.setGrade(grade);             // 문자 학점 저장
	    stuSub.setCompleteGrade(converted); // 숫자 점수 저장
	    stuSubRepository.save(stuSub);
	    stuSubDetailRepository.save(detail);
	    return toDto(detail);
	}

	private Long calculateConvertedMark(Long homework, Long midExam, Long finalExam) {
	    double hw = homework == null ? 0 : homework;
	    double mid = midExam == null ? 0 : midExam;
	    double fin = finalExam == null ? 0 : finalExam;

	    double total = hw * 0.1 + mid * 0.4 + fin * 0.5;
	    return Math.round(total);
	}

	private String calculateGrade(Long convertedMark) {
	    if (convertedMark == null) return "F";
	    if (convertedMark >= 90) return "A+";
	    if (convertedMark >= 85) return "A0";
	    if (convertedMark >= 80) return "B+";
	    if (convertedMark >= 75) return "B0";
	    if (convertedMark >= 70) return "C+";
	    if (convertedMark >= 65) return "C0";
	    if (convertedMark >= 60) return "D+";
	    if (convertedMark >= 50) return "D0";
	    return "F";
	}
	@Autowired
	StudentRepository studentRepository;
	
	public List<ProfessorDto> getProfessorsByStudentDepartment(Long studentId) {

        Student student = studentRepository.findById(studentId)
            .orElseThrow(() ->
                new CustomRestfullException("학생 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND)
            );

        if (student.getDepartment() == null) {
            throw new CustomRestfullException("학생의 학과 정보가 없습니다.", HttpStatus.BAD_REQUEST);
        }

        Long departmentId = student.getDepartment().getId();

        return professorRepository.findByDepartment_Id(departmentId)
                .stream()
                .map(this::toDto)
                .toList();
    }
}
