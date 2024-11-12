USE payment_service;

CREATE TABLE IF NOT EXISTS `User` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    userType ENUM('admin', 'shopowner', 'customer') NOT NULL,
    username VARCHAR(255) NOT NULL,
    email VARCHAR(255) NOT NULL,
    address VARCHAR(255),
    suspendedUntil DATETIME
);

CREATE TABLE IF NOT EXISTS `ShopItem` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shopId INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    stock INT NOT NULL,
    picture VARCHAR(255),
    description TEXT,
    isHidden BOOLEAN DEFAULT FALSE,
    canUpdate BOOLEAN DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS `Transaction` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sourceUserId INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transactionStatus ENUM('PENDING', 'COMPLETED', 'FAILED') NOT NULL,
    timeStamp DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    isRefunded BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sourceUserId) REFERENCES `User`(id) ON DELETE CASCADE
);

CREATE TABLE IF NOT EXISTS `Order` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderStatus ENUM('PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL,
    deliveryAddress VARCHAR(255) NOT NULL,
    shopItem INT,
    transactionId INT,
    price DECIMAL(10, 2) NOT NULL,
    FOREIGN KEY (transactionId) REFERENCES `Transaction`(id) ON DELETE SET NULL,
    FOREIGN KEY (shopItem) REFERENCES `ShopItem`(id) ON DELETE SET NULL
);
