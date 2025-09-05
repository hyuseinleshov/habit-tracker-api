package com.habittracker.api.habit.service;

import com.habittracker.api.habit.model.HabitEntity;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InternalHabitServiceImpl implements InternalHabitService {

    @Override
    @PreAuthorize("#habit.id.equals(authentication.principal.id)")
    public void softDelete(HabitEntity habit) {
        log.debug("Delete habit with id: {}", habit.getId());
        habit.setDeletedAt(Instant.now());
    }
}
