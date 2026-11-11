package com.gympro.member_service.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "members")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String userEmail;   // links to the auth-service user

    private String name;
    private String phone;
    private Integer age;
}