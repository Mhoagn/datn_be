-- Script để sửa kiểu dữ liệu từ BIGINT UNSIGNED sang BIGINT
-- Chạy script này trong MySQL Workbench hoặc command line
-- Lý do: Java Long tương thích với BIGINT (signed), không phải BIGINT UNSIGNED

USE datn;

-- Tạm thời disable foreign key checks
SET FOREIGN_KEY_CHECKS = 0;

-- Xóa bảng refresh_tokens nếu tồn tại (sẽ tạo lại sau)
DROP TABLE IF EXISTS refresh_tokens;

-- Sửa kiểu dữ liệu của bảng Users
ALTER TABLE users MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;

-- Sửa kiểu dữ liệu các foreign key references tới Users
ALTER TABLE groups MODIFY COLUMN created_by BIGINT NOT NULL;
ALTER TABLE group_members MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE group_join_requests MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE group_join_requests MODIFY COLUMN reviewed_by BIGINT NULL;
ALTER TABLE posts MODIFY COLUMN author_id BIGINT NOT NULL;
ALTER TABLE post_comments MODIFY COLUMN author_id BIGINT NOT NULL;
ALTER TABLE post_likes MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE conversations MODIFY COLUMN user1_id BIGINT NOT NULL;
ALTER TABLE conversations MODIFY COLUMN user2_id BIGINT NOT NULL;
ALTER TABLE messages MODIFY COLUMN sender_id BIGINT NOT NULL;
ALTER TABLE meetings MODIFY COLUMN created_by BIGINT NOT NULL;
ALTER TABLE meeting_participants MODIFY COLUMN user_id BIGINT NOT NULL;
ALTER TABLE meeting_messages MODIFY COLUMN author_id BIGINT NOT NULL;

-- Sửa kiểu dữ liệu các ID khác
ALTER TABLE groups MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE group_members MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE group_members MODIFY COLUMN group_id BIGINT NOT NULL;
ALTER TABLE group_join_requests MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE group_join_requests MODIFY COLUMN group_id BIGINT NOT NULL;
ALTER TABLE posts MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE posts MODIFY COLUMN group_id BIGINT NOT NULL;
ALTER TABLE post_comments MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE post_comments MODIFY COLUMN post_id BIGINT NOT NULL;
ALTER TABLE post_comments MODIFY COLUMN parent_comment_id BIGINT NULL;
ALTER TABLE post_likes MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE post_likes MODIFY COLUMN post_id BIGINT NOT NULL;
ALTER TABLE conversations MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE messages MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE messages MODIFY COLUMN conversation_id BIGINT NOT NULL;
ALTER TABLE meetings MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE meetings MODIFY COLUMN group_id BIGINT NOT NULL;
ALTER TABLE meeting_participants MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE meeting_participants MODIFY COLUMN meeting_id BIGINT NOT NULL;
ALTER TABLE meeting_messages MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE meeting_messages MODIFY COLUMN meeting_id BIGINT NOT NULL;
ALTER TABLE meeting_records MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE meeting_records MODIFY COLUMN meeting_id BIGINT NOT NULL;
ALTER TABLE meeting_summary MODIFY COLUMN id BIGINT NOT NULL AUTO_INCREMENT;
ALTER TABLE meeting_summary MODIFY COLUMN meeting_record_id BIGINT NOT NULL;

-- Bật lại foreign key checks
SET FOREIGN_KEY_CHECKS = 1;

-- Hibernate sẽ tự động tạo bảng refresh_tokens khi start ứng dụng
