USE user_service;
CREATE TABLE IF NOT EXISTS `User` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userType ENUM('admin', 'shopowner', 'customer') NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    isHidden BOOLEAN DEFAULT FALSE);