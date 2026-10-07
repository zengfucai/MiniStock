DROP DATABASE IF EXISTS ministock;

CREATE DATABASE ministock
    DEFAULT CHARACTER SET utf8mb4
    COLLATE utf8mb4_unicode_ci;

USE ministock;

CREATE TABLE users (
                       id BIGINT PRIMARY KEY AUTO_INCREMENT,
                       username VARCHAR(50) NOT NULL UNIQUE,
                       password VARCHAR(255) NOT NULL,
                       role VARCHAR(20) NOT NULL DEFAULT 'USER',
                       status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
                       created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE accounts (
                          id BIGINT PRIMARY KEY AUTO_INCREMENT,
                          user_id BIGINT NOT NULL UNIQUE,
                          balance DECIMAL(18,2) NOT NULL DEFAULT 0.00,
                          CONSTRAINT fk_account_user
                              FOREIGN KEY (user_id) REFERENCES users(id)
);

CREATE TABLE stocks (
                        id BIGINT PRIMARY KEY AUTO_INCREMENT,
                        code VARCHAR(20) NOT NULL UNIQUE,
                        name VARCHAR(100) NOT NULL,
                        price DECIMAL(18,2) NOT NULL,
                        change_rate DECIMAL(10,2) NOT NULL DEFAULT 0.00,
                        status VARCHAR(20) NOT NULL DEFAULT 'NORMAL',
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE positions (
                           id BIGINT PRIMARY KEY AUTO_INCREMENT,
                           user_id BIGINT NOT NULL,
                           stock_id BIGINT NOT NULL,
                           quantity INT NOT NULL DEFAULT 0,
                           average_price DECIMAL(18,2) NOT NULL DEFAULT 0.00,
                           UNIQUE KEY uk_user_stock (user_id, stock_id),
                           CONSTRAINT fk_position_user
                               FOREIGN KEY (user_id) REFERENCES users(id),
                           CONSTRAINT fk_position_stock
                               FOREIGN KEY (stock_id) REFERENCES stocks(id)
);

CREATE TABLE trade_applications (
                                    id BIGINT PRIMARY KEY AUTO_INCREMENT,
                                    user_id BIGINT NOT NULL,
                                    stock_id BIGINT NOT NULL,
                                    type VARCHAR(20) NOT NULL,
                                    quantity INT NOT NULL,
                                    price DECIMAL(18,2) NOT NULL,
                                    amount DECIMAL(18,2) NOT NULL,
                                    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
                                    reason VARCHAR(255),
                                    created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                    reviewed_at DATETIME,
                                    CONSTRAINT fk_application_user
                                        FOREIGN KEY (user_id) REFERENCES users(id),
                                    CONSTRAINT fk_application_stock
                                        FOREIGN KEY (stock_id) REFERENCES stocks(id)
);

CREATE TABLE trade_records (
                               id BIGINT PRIMARY KEY AUTO_INCREMENT,
                               user_id BIGINT NOT NULL,
                               stock_id BIGINT NOT NULL,
                               type VARCHAR(20) NOT NULL,
                               quantity INT NOT NULL,
                               price DECIMAL(18,2) NOT NULL,
                               amount DECIMAL(18,2) NOT NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                               CONSTRAINT fk_trade_user
                                   FOREIGN KEY (user_id) REFERENCES users(id),
                               CONSTRAINT fk_trade_stock
                                   FOREIGN KEY (stock_id) REFERENCES stocks(id)
);

CREATE TABLE fund_records (
                              id BIGINT PRIMARY KEY AUTO_INCREMENT,
                              user_id BIGINT NOT NULL,
                              type VARCHAR(30) NOT NULL,
                              amount DECIMAL(18,2) NOT NULL,
                              balance_after DECIMAL(18,2) NOT NULL,
                              description VARCHAR(255),
                              created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                              CONSTRAINT fk_fund_user
                                  FOREIGN KEY (user_id) REFERENCES users(id)
);

INSERT INTO users
(username, password, role, status)
VALUES
    ('admin', 'admin123', 'ADMIN', 'NORMAL');

INSERT INTO accounts
(user_id, balance)
SELECT id, 0
FROM users
WHERE username = 'admin';

INSERT INTO stocks
(code, name, price, change_rate, status)
VALUES
    ('ST001', '华夏科技', 25.68, 2.35, 'NORMAL'),
    ('ST002', '东方能源', 18.42, -1.27, 'NORMAL'),
    ('ST003', '中华银行', 12.36, 0.82, 'NORMAL'),
    ('ST004', '未来汽车', 36.80, 3.16, 'NORMAL'),
    ('ST005', '星海通信', 42.15, -0.68, 'NORMAL');