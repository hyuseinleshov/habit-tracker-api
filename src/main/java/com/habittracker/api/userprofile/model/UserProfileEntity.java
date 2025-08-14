package com.habittracker.api.userprofile.model;

import com.habittracker.api.auth.model.UserEntity;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
public class UserProfileEntity {

    @Id
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    private UserEntity user;
    @Column(nullable = false)
    private String timezone;
    private String firstName;
    private String lastName;
    private Integer age;
}
