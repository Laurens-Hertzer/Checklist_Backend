USE checklistdb;

SET @completed_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'checklist' AND column_name = 'completed'
);
SET @sql = IF(@completed_exists = 0,
    'ALTER TABLE checklist ADD COLUMN completed BOOLEAN NOT NULL DEFAULT FALSE',
    'SELECT 1');
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @parent_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'checklist' AND column_name = 'recurring_parent_id'
);
SET @sql = IF(@parent_exists = 0,
    'ALTER TABLE checklist ADD COLUMN recurring_parent_id INT DEFAULT NULL',
    'SELECT 1');
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @created_date_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'checklist' AND column_name = 'created_date'
);
SET @sql = IF(@created_date_exists = 0,
    'ALTER TABLE checklist ADD COLUMN created_date DATE NULL',
    'SELECT 1');
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

UPDATE checklist SET created_date = COALESCE(due_date, CURRENT_DATE) WHERE created_date IS NULL;
ALTER TABLE checklist MODIFY COLUMN created_date DATE NOT NULL;

SET @delete_after_days_exists = (
    SELECT COUNT(*) FROM information_schema.columns
    WHERE table_schema = DATABASE() AND table_name = 'checklist' AND column_name = 'delete_after_days'
);
SET @sql = IF(@delete_after_days_exists = 0,
    'ALTER TABLE checklist ADD COLUMN delete_after_days INT DEFAULT NULL',
    'SELECT 1');
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

SET @parent_fk_exists = (
    SELECT COUNT(*) FROM information_schema.table_constraints
    WHERE constraint_schema = DATABASE()
      AND table_name = 'checklist'
      AND constraint_name = 'fk_checklist_recurring_parent'
);
SET @sql = IF(@parent_fk_exists = 0,
    'ALTER TABLE checklist ADD CONSTRAINT fk_checklist_recurring_parent FOREIGN KEY (recurring_parent_id) REFERENCES checklist(checklist_id) ON DELETE CASCADE',
    'SELECT 1');
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

CREATE TABLE IF NOT EXISTS checklist_repeat_days (
    checklist_id INT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    PRIMARY KEY (checklist_id, day_of_week),
    FOREIGN KEY (checklist_id) REFERENCES checklist(checklist_id) ON DELETE CASCADE
);

SET @occurrences_exist = (
    SELECT COUNT(*) FROM information_schema.tables
    WHERE table_schema = DATABASE() AND table_name = 'checklist_occurrence'
);
SET @sql = IF(@occurrences_exist > 0,
    'INSERT INTO checklist (task, due_date, created_date, completed, recurring_parent_id)
     SELECT checklist.task, checklist_occurrence.occurrence_date, checklist_occurrence.occurrence_date, checklist_occurrence.completed, checklist.checklist_id
     FROM checklist_occurrence
     JOIN checklist ON checklist.checklist_id = checklist_occurrence.checklist_id
     WHERE NOT EXISTS (
         SELECT 1 FROM checklist AS generated
         WHERE generated.recurring_parent_id = checklist.checklist_id
           AND generated.due_date = checklist_occurrence.occurrence_date
     )',
    'SELECT 1');
PREPARE migration_statement FROM @sql;
EXECUTE migration_statement;
DEALLOCATE PREPARE migration_statement;

DROP TABLE IF EXISTS checklist_occurrence;
