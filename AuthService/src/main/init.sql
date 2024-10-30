USE auth;
CREATE TABLE IF NOT EXISTS `Auth` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT NOT NULL,
    accessToken VARCHAR(255) NOT NULL,
    accessTokenExpiry TIMESTAMP,
    refreshToken VARCHAR(255) NOT NULL,
    refreshTokenExpiry TIMESTAMP,
    FOREIGN KEY (userId) REFERENCES Users(id)
);