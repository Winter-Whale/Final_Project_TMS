package com.project.models;


import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.validation.constraints.Email;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Data
@ToString(exclude = {"security"})
@EqualsAndHashCode(exclude = {"security"})
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    @Column(name = "full_name")
    private String fullName;
    @Column(name = "phone")
    private String phone;
    @Email
    private  String email;

    private int age;

    @Column(updatable = false)
    private LocalDateTime created;
    private LocalDateTime updated;
    @OneToOne(cascade = CascadeType.ALL, mappedBy = "user")
    private Security security;
}






