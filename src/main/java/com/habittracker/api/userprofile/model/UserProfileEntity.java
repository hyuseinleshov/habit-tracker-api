package com.habittracker.api.userprofile.model;

import com.habittracker.api.auth.model.UserEntity;
import jakarta.persistence.*;
import java.util.UUID;
import lombok.Getter;
import lombok.Setter;

@Entity
@Table(name = "user_profiles")
@Getter
@Setter
public class UserProfileEntity {

  @Id private UUID id;

  @OneToOne(fetch = FetchType.LAZY)
  @MapsId
  private UserEntity user;

  @Column(nullable = false)
  private String timezone;

  private String firstName;
  private String lastName;
  private Integer age;

  public UserProfileEntity(UserEntity user, String timezone, String firstName, String lastName, Integer age) {
    this.user = user;
    this.timezone = timezone;
    this.firstName = firstName;
    this.lastName = lastName;
    this.age = age;
  }

  public UserProfileEntity() {
  }
}
