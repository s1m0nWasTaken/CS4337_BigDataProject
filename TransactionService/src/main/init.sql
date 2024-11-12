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

#CREATE INDEX IF NOT EXISTS idx_transaction_sourceUserid ON Transaction (sourceUserid);
CREATE INDEX idx_transaction_amount ON Transaction (amount);
CREATE INDEX IF NOT EXISTS idx_transaction_transactionStatus ON Transaction (transactionStatus);
CREATE INDEX IF NOT EXISTS idx_transaction_timeStamp ON Transaction (timeStamp);
