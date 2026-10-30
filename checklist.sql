CREATE DATABASE IF NOT EXISTS checklistdb;
USE checklistdb;

CREATE TABLE IF NOT EXISTS checklist (
    checklist_id INT AUTO_INCREMENT PRIMARY KEY,
    task VARCHAR(255) NOT NULL,
    completed BOOLEAN NOT NULL DEFAULT FALSE,
    recurring_parent_id INT DEFAULT NULL,
    due_date DATE DEFAULT NULL,
    created_date DATE NOT NULL,
    delete_after_days INT DEFAULT NULL,
    FOREIGN KEY (recurring_parent_id) REFERENCES checklist(checklist_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS checklist_repeat_days (
    checklist_id INT NOT NULL,
    day_of_week VARCHAR(10) NOT NULL,
    PRIMARY KEY (checklist_id, day_of_week),
    FOREIGN KEY (checklist_id) REFERENCES checklist(checklist_id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS setting (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    train_starting VARCHAR(255) NOT NULL,
    train_destination VARCHAR(255) NOT NULL,
    weather_place VARCHAR(255) NOT NULL
);