USE rating_service;
CREATE TABLE IF NOT EXISTS ShopRating (
    id INT AUTO_INCREMENT PRIMARY KEY,
    shopid INT NOT NULL,
    userid INT NOT NULL,
    rating INT CHECK (rating >= 1 AND rating <= 5),
    message TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
