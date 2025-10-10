-- Add index to optimize streak calculation queries
-- This index significantly improves performance when fetching check-ins ordered by creation date
-- Especially important for habits with long streaks (hundreds of days)
CREATE INDEX IF NOT EXISTS idx_check_ins_habit_created
ON check_ins(habit_id, created_at DESC);
