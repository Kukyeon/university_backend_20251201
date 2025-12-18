package com.university.home.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.university.home.dto.ScheduleFormDto;
import com.university.home.entity.Schedule;
import com.university.home.entity.Staff;
import com.university.home.repository.ScheduleRepository;
import com.university.home.repository.StaffRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ScheduleService {

    private final ScheduleRepository scheduleRepository;
    private final StaffRepository staffRepository;

    public List<Schedule> getAllSchedules() {
        return scheduleRepository.findAll();
    }

    public Schedule getSchedule(Long id) {
        return scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
    }

    @Transactional
    public Schedule createSchedule(Long staffId, ScheduleFormDto dto) {
        Staff staff = staffRepository.findById(staffId)
                .orElseThrow(() -> new RuntimeException("직원을 찾을 수 없습니다."));
        Schedule schedule = new Schedule();
        schedule.setStaff(staff);
        schedule.setStartDay(dto.getStartDay());
        schedule.setEndDay(dto.getEndDay());
        schedule.setInformation(dto.getInformation());
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public Schedule updateSchedule(Long id, ScheduleFormDto dto) {
        Schedule schedule = scheduleRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("일정을 찾을 수 없습니다."));
        schedule.setStartDay(dto.getStartDay());
        schedule.setEndDay(dto.getEndDay());
        schedule.setInformation(dto.getInformation());
        return scheduleRepository.save(schedule);
    }

    @Transactional
    public void deleteSchedule(Long id, Long staffId) {
        scheduleRepository.deleteByIdAndStaff_Id(id, staffId);
    }

    public List<Schedule> getSchedulesByMonth(int month) {
        return scheduleRepository.findByMonth(month);
    }
}
