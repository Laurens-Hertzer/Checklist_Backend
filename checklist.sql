CREATE DATABASE IF NOT EXISTS checklistdb;
USE checklistdb;

CREATE TABLE IF NOT EXISTS checklist (
    checklist_id INT AUTO_INCREMENT PRIMARY KEY,
    task VARCHAR(255) NOT NULL,
    completed BOOLEAN DEFAULT FALSE,
    due_date DATE DEFAULT NULL
);

CREATE TABLE IF NOT EXISTS setting (
    setting_id INT AUTO_INCREMENT PRIMARY KEY,
    train_starting VARCHAR(255) NOT NULL,
    train_destination VARCHAR(255) NOT NULL,
    weather_place VARCHAR(255) NOT NULL
);