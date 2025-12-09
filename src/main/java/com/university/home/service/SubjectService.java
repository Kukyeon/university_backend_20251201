package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.university.home.dto.SubjectDto;
import com.university.home.entity.Subject;
import com.university.home.entity.Syllabus;
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
	public List<SubjectDto> getSubjects() {
	    List<Subject> subjects = subjectRepository.findAll();
	    return subjects.stream().map(this::toDto).toList();
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

	    // 연관관계 매핑
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
	            .orElseThrow(() -> new RuntimeException("강의 없음"));
		subjectRepository.delete(subject);

	}
	@Transactional
	public SubjectDto updateSubject(Long id, SubjectDto dto) {

	    Subject subject = subjectRepository.findById(id)
	            .orElseThrow(() -> new RuntimeException("강의 없음"));

	    // 연도/학기는 변경 불가 (기존 로직 유지)
	    dto.setSubYear(subject.getSubYear());
	    dto.setSemester(subject.getSemester());

	    // 중복 체크
	    List<Subject> existed = subjectRepository.findByRoom_IdAndSubDayAndSubYearAndSemester(
	            dto.getRoomId(), dto.getSubDay(), dto.getSubYear(), dto.getSemester()
	    );

	    // 본인 제외
	    existed.removeIf(s -> s.getId().equals(id));

	    validateDuplicateSubject(dto, existed);

	    subject.setName(dto.getName());
	    subject.setSubDay(dto.getSubDay());
	    subject.setStartTime(dto.getStartTime());
	    subject.setEndTime(dto.getEndTime());
	    subject.setGrades(dto.getGrades());
	    subject.setCapacity(dto.getCapacity());
	    subject.setType(dto.getType());

	    subject.setProfessor(professorRepository.findById(dto.getProfessorId()).orElseThrow());
	    subject.setRoom(roomRepository.findById(dto.getRoomId()).orElseThrow());
	    subject.setDepartment(departmentRepository.findById(dto.getDeptId()).orElseThrow());

	    return toDto(subject);
	}


}
