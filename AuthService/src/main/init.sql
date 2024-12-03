USE auth-service;
CREATE TABLE IF NOT EXISTS `Auth` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    refreshToken VARCHAR(255) NOT NULL,
    expiryDate TIMESTAMP
);