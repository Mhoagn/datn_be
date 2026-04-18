-- Script để sửa lỗi foreign key trong bảng refresh_tokens
-- Chạy script này trong MySQL Workbench hoặc command line

USE datn;

-- Xóa bảng cũ nếu tồn tại
DROP TABLE IF EXISTS refresh_tokens;

-- Bảng sẽ được Hibernate tạo lại tự động khi start ứng dụng
-- Hoặc bạn có thể tạo thủ công:

CREATE TABLE refresh_tokens (
    id BIGINT NOT NULL AUTO_INCREMENT,
    token VARCHAR(500) NOT NULL UNIQUE,
    user_id BIGINT NOT NULL,
    expires_at DATETIME NOT NULL,
    is_revoked TINYINT(1) DEFAULT 0 NOT NULL,
    created_at DATETIME NOT NULL,
    PRIMARY KEY (id),
    CONSTRAINT fk_refresh_token_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Tạo index cho tìm kiếm nhanh
CREATE INDEX idx_refresh_token ON refresh_tokens(token);
CREATE INDEX idx_user_id ON refresh_tokens(user_id);
