USE shop_service;
CREATE TABLE IF NOT EXISTS `Shop` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shopOwnerid INT NOT NULL,
    shopName VARCHAR(255) NOT NULL,
    imageData VARCHAR(255) NOT NULL,
    description VARCHAR(255),
    shopType ENUM('CLOTHING', 'ELECTRONICS', 'FOOD', 'BOOKS', 'TOYS', 'OTHER') NOT NULL,
    shopEmail VARCHAR(255) NOT NULL);

CREATE TABLE IF NOT EXISTS `ShopItem` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shopid INT NOT NULL,
    price DOUBLE(10, 2),
    itemName VARCHAR(255),
    stock INT NOT NULL,
    picture VARCHAR(255),
    description VARCHAR(255),
    isHidden BOOL DEFAULT FALSE,
    canUpdate BOOL DEFAULT TRUE);