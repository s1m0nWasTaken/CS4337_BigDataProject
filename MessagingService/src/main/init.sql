CREATE DATABASE IF NOT EXISTS messaging_service;

USE messaging_service;

CREATE TABLE IF NOT EXISTS `ChatParticipants` (
	chatid INT AUTO_INCREMENT PRIMARY KEY,
	userid1 INT NOT NULL,
	userid2 INT NOT NULL
);

CREATE TABLE IF NOT EXISTS `Messages` (
    id INT AUTO_INCREMENT PRIMARY KEY,
	chatid INT NOT NULL,
	senderid INT NOT NULL,
	createdAt datetime NOT NULL,
	content TEXT,
	FOREIGN KEY (chatid) REFERENCES ChatParticipants(chatid)
);