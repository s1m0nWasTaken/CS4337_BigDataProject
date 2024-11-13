USE payment_service;

CREATE TABLE IF NOT EXISTS `Transaction` (
    id INT AUTO_INCREMENT PRIMARY KEY,
    sourceUserId INT NOT NULL,
    amount DOUBLE(10, 2) NOT NULL,
    transactionStatus ENUM('PENDING', 'SUCCESS', 'FAILED') NOT NULL,
    timeStamp DATETIME NOT NULL,
    isRefunded BOOLEAN DEFAULT FALSE
    );
