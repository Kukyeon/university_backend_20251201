package com.university.home.service;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.university.home.dto.SubjectDto;
import com.university.home.dto.SyllabusDto;
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
	
	@Transactional
    public void updateSyllabus(Long subjectId, Long loginUserId, SyllabusDto dto) {
        
        // 1. 과목 조회
        Subject subject = subjectRepository.findById(subjectId)
                .orElseThrow(() -> new IllegalArgumentException("존재하지 않는 과목입니다."));

        // 2. [권한 검증] 과목의 담당 교수 ID와 로그인한 유저 ID가 일치하는지 확인
        // 담당 교수가 없거나, ID가 다르면 예외 발생 -> Controller에서 403 처리
        if (subject.getProfessor() == null || !subject.getProfessor().getId().equals(loginUserId)) {
            throw new SecurityException("본인의 강의계획서만 수정할 수 있습니다.");
        }

        // 3. Syllabus 객체 조회 (없으면 생성)
        Syllabus syllabus = subject.getSyllabus();

        if (syllabus == null) {
            syllabus = new Syllabus();
            syllabus.setSubject(subject); // @MapsId 사용 시 관계 설정 중요
            // subject.setSyllabus(syllabus); // 양방향 관계일 경우 필요
        }

        // 4. 내용 업데이트 (DTO -> Entity)
        syllabus.setOverview(dto.getOverview());
        syllabus.setObjective(dto.getObjective());
        syllabus.setTextbook(dto.getTextbook());
        syllabus.setProgram(dto.getProgram());

        // 5. 저장
        syllabusRepository.save(syllabus);
    }


}
