package com.university.home.service;

import com.university.home.repository.RoomRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import com.university.home.dto.CollegeDto;
import com.university.home.dto.RoomDto;
import com.university.home.entity.College;
import com.university.home.entity.Room;
import com.university.home.repository.CollegeRepository;

@Service
public class RoomService {

	@Autowired
    RoomRepository roomRepository;
	
	@Autowired
	CollegeRepository collegeRepository;
	private RoomDto toDto(Room room) {
	        RoomDto dto = new RoomDto();
	        dto.setId(room.getId());
	
	        CollegeDto collegeDto = new CollegeDto();
	        collegeDto.setId(room.getCollege().getId());
	        collegeDto.setName(room.getCollege().getName());
	        dto.setCollege(collegeDto);
	
	        return dto;
    }
	public Page<RoomDto> roomList(Pageable pageable) {
		 return roomRepository.findAll(pageable)
	                .map(this::toDto);
    }
	@Transactional
    public RoomDto createRoom(RoomDto dto) {
		 if(roomRepository.existsById(dto.getId())) {
		        throw new RuntimeException("이미 존재하는 강의실입니다.");
		    }
        College college = collegeRepository.findById(dto.getCollege().getId())
                .orElseThrow(() -> new RuntimeException("단과대학이 존재하지 않습니다."));

        Room room = new Room();
        room.setId(dto.getId());
        room.setCollege(college);

        roomRepository.save(room);
        return toDto(room);
    }

    @Transactional
    public void deleteRoom(String id) {
        Room room = roomRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("강의실이 존재하지 않습니다."));
        roomRepository.delete(room);
    }
}
