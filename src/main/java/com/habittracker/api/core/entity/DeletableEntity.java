package com.habittracker.api.core.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import jakarta.persistence.Transient;

import java.time.Instant;

@MappedSuperclass
public class DeletableEntity extends BaseEntity {

    @Column
    private Instant deletedAt;

    @Transient
    public boolean isDeleted() {
        return deletedAt != null;
    }
}
