package com.habittracker.api.habit.service.impl;

import com.habittracker.api.habit.exception.HabitAlreadyDeletedException;
import com.habittracker.api.habit.model.HabitEntity;
import com.habittracker.api.habit.service.InternalHabitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

import static com.habittracker.api.habit.constants.HabitConstants.HABIT_NOT_NULL_MESSAGE;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class InternalHabitServiceImpl implements InternalHabitService {

    @Override
    @PreAuthorize("#habit.id.equals(authentication.principal.id)")
    public void softDelete(HabitEntity habit) {
        if(habit == null) {
            throw new IllegalArgumentException(HABIT_NOT_NULL_MESSAGE);
        }
        if(habit.isDeleted()) {
            throw new HabitAlreadyDeletedException();
        }
        log.debug("Delete habit with id: {}", habit.getId());
        habit.setDeletedAt(Instant.now());
    }
}
