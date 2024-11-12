USE order_service;

CREATE TABLE Order (
    id INT AUTO_INCREMENT PRIMARY KEY,
    orderStatus ENUM('PENDING', 'SHIPPED', 'DELIVERED', 'CANCELLED') NOT NULL,
    deliveryAddress VARCHAR(255) NOT NULL,
    shopItemid INT NOT NULL,
    transactionid INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

    FOREIGN KEY (shopItemid) REFERENCES ShopItem(id) ON DELETE CASCADE
);

CREATE INDEX idx_order_orderStatus ON Orders(orderStatus);
CREATE INDEX idx_order_shopItemid ON Orders(shopItemid);
CREATE INDEX idx_order_transactionid ON Orders(transactionid);