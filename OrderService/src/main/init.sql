USE shop_service;

CREATE TABLE IF NOT EXISTS `Orders` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderStatus ENUM('PROCESSING', 'OUT_FOR_DELIVERY', 'DELIVERED', 'CANCELLED') NOT NULL DEFAULT 'PROCESSING',
    deliveryAddress VARCHAR(255) NOT NULL,
    shopItemid INT DEFAULT NULL,
    transactionid INT NOT NULL,
    price DOUBLE(10, 2) NOT NULL,
    orderDate DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (shopItemid) REFERENCES ShopItem(id) ON DELETE SET NULL,
    INDEX idx_orders_orderStatus (orderStatus),
    INDEX idx_orders_deliveryAddress (deliveryAddress),
    INDEX idx_orders_shopItemid (shopItemid),
    INDEX idx_orders_transactionid (transactionid),
    INDEX idx_orders_price (price),
    INDEX idx_orders_orderDate (orderDate)
);
