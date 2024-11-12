USE transaction_service;

CREATE TABLE IF NOT EXISTS `Transaction` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sourceUserid INT NOT NULL,
    amount DECIMAL(10, 2) NOT NULL,
    transactionStatus ENUM('PENDING', 'COMPLETED', 'FAILED', 'REFUNDED', 'CANCELED') NOT NULL,
    timeStamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    isRefunded BOOLEAN DEFAULT FALSE,
    FOREIGN KEY (sourceUserid) REFERENCES user_service.User(id)
);
