package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notice_file_tb")
@Getter @Setter
@NoArgsConstructor
public class NoticeFile {
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id; // Integer -> Long

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;

    @Column(name = "origin_filename")
    private String originFilename;

    @Column(name = "uuid_filename")
    private String uuidFilename;
}