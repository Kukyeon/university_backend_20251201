package com.university.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.university.entity.StuSch;

public interface StuSchRepository extends JpaRepository<StuSch, Long> {

}
