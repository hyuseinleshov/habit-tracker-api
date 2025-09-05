package com.habittracker.api.habit.service;

import com.habittracker.api.habit.model.HabitEntity;

public interface InternalHabitService {

    void softDelete(HabitEntity habitEntity);
}
