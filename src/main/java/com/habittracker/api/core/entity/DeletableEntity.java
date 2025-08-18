package com.habittracker.api.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;
import lombok.Getter;
import lombok.Setter;

import java.time.Instant;

@MappedSuperclass
@Getter
@Setter
public class DeletableEntity extends BaseEntity {

    @Column
    private Instant deletedAt;

    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
