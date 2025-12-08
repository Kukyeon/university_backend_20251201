package com.university.home.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.home.entity.Room;

public interface RoomRepository extends JpaRepository<Room, String> {

}
