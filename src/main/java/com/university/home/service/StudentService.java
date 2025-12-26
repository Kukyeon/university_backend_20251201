package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.BreakAppDto;
import com.university.home.dto.CollegeDto;
import com.university.home.dto.DepartmentDto;
import com.university.home.dto.StuStatDto;
import com.university.home.dto.StudentDto;
import com.university.home.dto.StudentStatDto;
import com.university.home.entity.College;
import com.university.home.entity.Department;
import com.university.home.entity.Professor;
import com.university.home.entity.StuStat;
import com.university.home.entity.Student;
import com.university.home.entity.User;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.BreakAppRepository;
import com.university.home.repository.DepartmentRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.StudentRepository;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class StudentService {

	@Autowired
	StudentRepository studentRepository;
	@Autowired
	UserService userService;
	@Autowired
	DepartmentRepository departmentRepository;
	@Autowired
	StuStatService stuStatService;
	@Autowired
	private BreakAppRepository breakAppRepository;
	@Autowired
	private ProfessorRepository professorRepository;
	
	@Transactional
	public StudentDto createStudentWithStatus(StudentDto dto) {
	    Student student = createStudent(dto); 
	    stuStatService.createFirstStatus(student); 
	    return toDto(student);
	}
	public StudentDto toDto(Student student) {
	    StudentDto dto = new StudentDto();
	    dto.setId(student.getId());
	    dto.setName(student.getName());
	    dto.setBirthDate(student.getBirthDate());
	    dto.setGender(student.getGender());
	    dto.setEntranceDate(student.getEntranceDate());
	    dto.setAddress(student.getAddress());
	    dto.setTel(student.getTel());
	    dto.setEmail(student.getEmail());
	    dto.setGrade(student.getGrade());
	    dto.setSemester(student.getSemester());
	  
	    Department dep = student.getDepartment();
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
	 // 학적 상태
	    StuStat currentStatus = stuStatService.getCurrentStatus(student.getId());
	    if (currentStatus != null) {
	        StuStatDto statDto = new StuStatDto();
	        statDto.setId(currentStatus.getId());
	        statDto.setStatus(currentStatus.getStatus());
	        statDto.setFromDate(currentStatus.getFromDate());
	        statDto.setToDate(currentStatus.getToDate());
	        statDto.setBreakAppId(currentStatus.getBreakAppId());
	        dto.setCurrentStatus(statDto);
	    }
	    List<StudentStatDto> statList = stuStatService.getStatusList(student.getId())
	            .stream()
	            .map(s -> {
	                StudentStatDto stat = new StudentStatDto();
	                stat.setId(s.getId());
	                stat.setStatus(s.getStatus());
	                stat.setFromDate(s.getFromDate());
	                stat.setToDate(s.getToDate());
	                stat.setBreakAppId(s.getBreakAppId());
	                return stat;
	            })
	            .toList();
	        dto.setStatList(statList);
	        List<BreakAppDto> breakDtos = breakAppRepository.findByStudentId(student.getId())
	                .stream()
	                .map(b -> {
	                    BreakAppDto bDto = new BreakAppDto();
	                    bDto.setId(b.getId());
	                    bDto.setStatus(b.getStatus());
	                    bDto.setType(b.getType());
	                    bDto.setFromYear(b.getFromYear());
	                    bDto.setFromSemester(b.getFromSemester());
	                    bDto.setToYear(b.getToYear());
	                    bDto.setToSemester(b.getToSemester());
	                    bDto.setAppDate(b.getAppDate());
	                    return bDto;
	                })
	                .toList();

	            dto.setBreakApps(breakDtos);
	        
	    return dto;
	}
	@Transactional
	public Student createStudent(StudentDto dto) {
		Student student = new Student();
		student.setName(dto.getName());
		student.setAddress(dto.getAddress());
		student.setBirthDate(dto.getBirthDate());
		student.setEmail(dto.getEmail());
		student.setGender(dto.getGender());
		student.setTel(dto.getTel());
		student.setEntranceDate(dto.getEntranceDate());
		Department dept = departmentRepository.findById(dto.getDepartment().getId())
				 .orElseThrow(() -> new CustomRestfullException(
	                        "학과 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));
		student.setDepartment(dept);
		studentRepository.save(student);
		
		User user = userService.createUser(student.getId(), "student");
		student.setUser(user);
		studentRepository.save(student);
		return student;
	}
	
	@Transactional
	public StudentDto readStudent(Long id) {
		Student student =  studentRepository.findById(id)
				.orElseThrow(() -> new CustomRestfullException(
                        "학생 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND));
		return toDto(student);
	}

	@Transactional
	public List<Student> getAllStudents() {
		return studentRepository.findAll();
	}
	@Transactional
	public boolean checkExistsForPasswordReset(Long id, String name, String email) {
		return studentRepository.existsByIdAndNameAndEmail(id, name, email);
	}
	@Transactional
	public Long findByNameEmail(String name, String email) {
		return studentRepository.findByNameAndEmail(name, email)
				.map(Student::getId)
				.orElseThrow(() -> new CustomRestfullException(
                        "학생 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND));
	}
	// 전체 학생 조회
	public Page<StudentDto> getStudents(Pageable pageable) {
		 return studentRepository.findAll(pageable)
	                .map(this::toDto);
	}
	// 학과별 학생 조회
	public Page<StudentDto> getStudentsByDep(Long deptId,Pageable pageable) {
		Department dept = departmentRepository.findById(deptId)
				 .orElseThrow(() -> new CustomRestfullException(
	                        "학과 정보를 찾을 수 없습니다.", HttpStatus.BAD_REQUEST));
		  return studentRepository.findByDepartment(dept, pageable)
	                .map(this::toDto);
	}
	// 학번 학생 조회
	public StudentDto getStudentById(Long studentId) {
		Student student = studentRepository.findById(studentId)
				.orElseThrow(() -> new CustomRestfullException(
                        "학생 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND));
        return toDto(student);
	}
	// StudentService
	public Student getStudentByIdEntity(Long studentId) {
	    return studentRepository.findById(studentId)
	    		.orElseThrow(() -> new CustomRestfullException(
                        "학생 정보가 존재하지 않습니다.", HttpStatus.NOT_FOUND));
	}

	@Transactional
	public int updateStudentGradeAndSemesters() {
		List<Student> students = studentRepository.findAll();
		int count = 0;
	    for (Student student : students) {
	    	StuStat currentStatus = stuStatService.getCurrentStatus(student.getId());
	        if (currentStatus != null && "휴학".equals(currentStatus.getStatus())) {
	            continue; // 휴학생이면 건너뛰기
	        }
	        int grade = student.getGrade().intValue();
	        int semester = student.getSemester().intValue();

	        switch (grade) {
            case 1:
                if (semester == 1) student.setSemester(Long.valueOf(2));
                else { student.setGrade(Long.valueOf(2)); student.setSemester(Long.valueOf(1)); }
                break;
            case 2:
                if (semester == 1) student.setSemester(Long.valueOf(2));
                else { student.setGrade(Long.valueOf(3)); student.setSemester(Long.valueOf(1)); }
                break;
            case 3:
                if (semester == 1) student.setSemester(Long.valueOf(2));
                else { student.setGrade(Long.valueOf(4)); student.setSemester(Long.valueOf(1)); }
                break;
            case 4:
                if (semester == 1) student.setSemester(Long.valueOf(2));
	                break;
	        }
	        count++;
	    }
	    return count;
	}
	
	public String getProfessorName(Long professorId) {
	    return professorRepository.findById(professorId)
	            .map(Professor::getName)
	            .orElse("Unknown Professor");
	}
	public String getStudentName(Long studentId) {
	    return studentRepository.findById(studentId)
	            .map(Student::getName)
	            .orElse("Unknown Student");
	}
}
