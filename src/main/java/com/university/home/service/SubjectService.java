package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import com.university.home.dto.SubjectDto;
import com.university.home.dto.SyllabusDto;
import com.university.home.entity.Subject;
import com.university.home.entity.Syllabus;
import com.university.home.exception.CustomRestfullException;
import com.university.home.repository.DepartmentRepository;
import com.university.home.repository.ProfessorRepository;
import com.university.home.repository.RoomRepository;
import com.university.home.repository.SubjectRepository;
import com.university.home.repository.SyllabusRepository;

import jakarta.transaction.Transactional;

@Service
public class SubjectService {

	@Autowired
	SubjectRepository subjectRepository;
	@Autowired
	RoomRepository roomRepository;
	@Autowired
	ProfessorRepository professorRepository;
	@Autowired
	DepartmentRepository departmentRepository;
	@Autowired
	SyllabusRepository syllabusRepository;
	private void validateDuplicateSubject(SubjectDto dto, List<Subject> existed) {
	    if (existed.isEmpty()) return;

	    for (Subject s : existed) {
	        boolean conflict =
	                dto.getStartTime() < s.getEndTime() &&
	                dto.getEndTime() > s.getStartTime();

	        if (conflict)
	            throw new RuntimeException("해당 시간에 해당 강의실은 이미 사용 중입니다.");
	    }
	}
	public Page<SubjectDto> getSubjects(Pageable pageable) {
	    Page<Subject> subjects = subjectRepository.findAll(pageable);
	    return subjects.map(this::toDto);
	}

	private SubjectDto toDto(Subject subject) {
	    SubjectDto dto = new SubjectDto();
	    dto.setId(subject.getId());
	    dto.setName(subject.getName());
	    dto.setSubYear(subject.getSubYear());
	    dto.setSemester(subject.getSemester());
	    dto.setSubDay(subject.getSubDay());
	    dto.setStartTime(subject.getStartTime());
	    dto.setEndTime(subject.getEndTime());
	    dto.setGrades(subject.getGrades());
	    dto.setCapacity(subject.getCapacity());
	    dto.setType(subject.getType());
	    dto.setProfessorId(subject.getProfessor().getId());
	    dto.setRoomId(subject.getRoom().getId());
	    dto.setDeptId(subject.getDepartment().getId());
	    dto.setNumOfStudent(subject.getNumOfStudent());
	    dto.setTargetGrade(subject.getTargetGrade());
	    return dto;
	}

	@Transactional
	public SubjectDto createSubject(SubjectDto dto) {

	    List<Subject> existed = subjectRepository.findByRoom_IdAndSubDayAndSubYearAndSemester(
	            dto.getRoomId(), dto.getSubDay(), dto.getSubYear(), dto.getSemester()
	    );

	    validateDuplicateSubject(dto, existed);

	    Subject subject = new Subject();
	    subject.setName(dto.getName());
	    subject.setSubYear(dto.getSubYear());
	    subject.setSemester(dto.getSemester());
	    subject.setSubDay(dto.getSubDay());
	    subject.setStartTime(dto.getStartTime());
	    subject.setEndTime(dto.getEndTime());
	    subject.setGrades(dto.getGrades());
	    subject.setCapacity(dto.getCapacity());
	    subject.setType(dto.getType());
	    subject.setTargetGrade(dto.getTargetGrade());

	    subject.setProfessor(professorRepository.findById(dto.getProfessorId()).orElseThrow());
	    subject.setRoom(roomRepository.findById(dto.getRoomId()).orElseThrow());
	    subject.setDepartment(departmentRepository.findById(dto.getDeptId()).orElseThrow());

	    Subject saved = subjectRepository.save(subject);

	    Syllabus syllabus = new Syllabus();
	    syllabus.setSubject(saved);
	    syllabusRepository.save(syllabus);

	    return toDto(saved);
	}
	@Transactional
	public void deleteSubject(Long id) {
		Subject subject = subjectRepository.findById(id)
				.orElseThrow(() -> new CustomRestfullException("강의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
		if (subject.getNumOfStudent() != null && subject.getNumOfStudent() > 0) {
	        throw new CustomRestfullException(
	            "이미 수강 신청한 학생이 " + subject.getNumOfStudent() + "명 존재하여 삭제할 수 없습니다.", 
	            HttpStatus.BAD_REQUEST
	        );
	    }
		try {
	        subjectRepository.delete(subject);
	    } catch (Exception e) {
	        // 기타 DB 제약 조건 위반 시
	        throw new CustomRestfullException("다른 데이터와 연결되어 있어 삭제할 수 없습니다.", HttpStatus.CONFLICT);
	    }
		subjectRepository.delete(subject);

	}
	@Transactional
	public SubjectDto updateSubject(Long id, SubjectDto dto) {

	    Subject subject = subjectRepository.findById(id)
	    		.orElseThrow(() -> new CustomRestfullException("강의를 찾을 수 없습니다.", HttpStatus.NOT_FOUND));

	    dto.setSubYear(subject.getSubYear());
	    dto.setSemester(subject.getSemester());

	    List<Subject> existed = subjectRepository.findByRoom_IdAndSubDayAndSubYearAndSemester(
	            dto.getRoomId(), dto.getSubDay(), dto.getSubYear(), dto.getSemester()
	    );

	    existed.removeIf(s -> s.getId().equals(id));

	    validateDuplicateSubject(dto, existed);

	    subject.setName(dto.getName());
	    subject.setSubDay(dto.getSubDay());
	    subject.setStartTime(dto.getStartTime());
	    subject.setEndTime(dto.getEndTime());
	    subject.setGrades(dto.getGrades());
	    subject.setCapacity(dto.getCapacity());
	    subject.setType(dto.getType());
	    subject.setTargetGrade(dto.getTargetGrade());

	    subject.setProfessor(professorRepository.findById(dto.getProfessorId()).orElseThrow());
	    subject.setRoom(roomRepository.findById(dto.getRoomId()).orElseThrow());
	    subject.setDepartment(departmentRepository.findById(dto.getDeptId()).orElseThrow());

	    return toDto(subject);
	}
	
	@Transactional
    public void updateSyllabus(Long subjectId, Long loginUserId, SyllabusDto dto) {
        
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다."));

        if (subject.getProfessor() == null || !subject.getProfessor().getId().equals(loginUserId)) {
            throw new SecurityException("본인의 강의계획서만 수정할 수 있습니다.");
        }

        Syllabus syllabus = subject.getSyllabus();

        if (syllabus == null) {
            syllabus = new Syllabus();
            syllabus.setSubject(subject); 
        }

        syllabus.setOverview(dto.getOverview());
        syllabus.setObjective(dto.getObjective());
        syllabus.setTextbook(dto.getTextbook());
        syllabus.setProgram(dto.getProgram());

        syllabusRepository.save(syllabus);
    }


}
