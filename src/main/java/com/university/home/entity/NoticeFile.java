package com.university.home.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "notice_file_tb")
@Getter @Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class NoticeFile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String originFilename;
    private String uuidFilename;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    private byte[] data; 
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notice_id")
    private Notice notice;
}

