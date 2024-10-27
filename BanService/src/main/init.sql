USE ban_service;
CREATE TABLE IF NOT EXISTS `UserBan` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userId INT,
    suspendedUntil DATE
);

CREATE INDEX user_ban_date_index
ON `UserBan` (suspendedUntil);

CREATE TABLE IF NOT EXISTS `ShopBan` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shopId INT,
    suspendedUntil DATE
);

CREATE INDEX shop_ban_date_index
ON `ShopBan` (suspendedUntil);