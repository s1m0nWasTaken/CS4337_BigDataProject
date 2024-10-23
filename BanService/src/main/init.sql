USE ban_service;
CREATE TABLE IF NOT EXISTS `UserBan` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT,
    suspendedUntil DATE
);

CREATE TABLE IF NOT EXISTS `ShopBan` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shopId INT,
    suspendedUntil DATE
);