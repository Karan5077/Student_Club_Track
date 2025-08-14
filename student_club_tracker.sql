-- Delete old database if it exists
DROP DATABASE IF EXISTS student_club_tracker;

-- Create new database
CREATE DATABASE student_club_tracker;
USE student_club_tracker;

-- Create clubs table
CREATE TABLE clubs (
    club_id INT AUTO_INCREMENT PRIMARY KEY,
    club_name VARCHAR(255) NOT NULL
);

-- Create members table
CREATE TABLE members (
    member_id INT AUTO_INCREMENT PRIMARY KEY,
    club_id INT,
    member_name VARCHAR(255) NOT NULL,
    role VARCHAR(255),
    FOREIGN KEY (club_id) REFERENCES clubs(club_id)
);

-- Create achievements table
CREATE TABLE achievements (
    achievement_id INT AUTO_INCREMENT PRIMARY KEY,
    member_id INT,
    club_id INT,
    achievement_title VARCHAR(255),
    description TEXT,
    FOREIGN KEY (member_id) REFERENCES members(member_id),
    FOREIGN KEY (club_id) REFERENCES clubs(club_id)
);
