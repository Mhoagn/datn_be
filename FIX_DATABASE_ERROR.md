# Fix Database Error: "Data truncated for column 'ai_model'"

## 🔴 Vấn đề

Khi trigger xử lý AI, backend gặp lỗi:
```
Data truncated for column 'ai_model' at row 1
```

## 🎯 Nguyên nhân

MySQL ENUM có thể gây conflict với JPA/Hibernate khi:
- Database schema không khớp với entity definition
- MySQL version khác nhau xử lý ENUM khác nhau
- Character set/collation issues

## ✅ Giải pháp

### Bước 1: Chạy Migration Script

Mở MySQL client và chạy file `migrate-enum-to-varchar.sql`:

```bash
# Trong MySQL terminal
mysql -u root -p your_database_name < migrate-enum-to-varchar.sql
```

Hoặc copy-paste nội dung sau vào MySQL Workbench/phpMyAdmin:

```sql
-- Migration Script
ALTER TABLE `Meeting_Transcripts` 
MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING';

ALTER TABLE `Meeting_Summary_Candidates` 
MODIFY COLUMN `ai_model` VARCHAR(20) NOT NULL DEFAULT 'QWEN';

ALTER TABLE `Meeting_Summary_Candidates` 
MODIFY COLUMN `status` VARCHAR(20) NOT NULL DEFAULT 'PENDING';
```

### Bước 2: Restart Backend

```bash
# Stop backend
Ctrl+C

# Start lại backend
./mvnw spring-boot:run
# hoặc
mvn spring-boot:run
```

### Bước 3: Kiểm tra

1. Thử trigger xử lý AI lại từ frontend
2. Kiểm tra log backend - không còn lỗi "Data truncated"
3. Kiểm tra AI Service có được gọi không

## 🔍 Verify Changes

Sau khi chạy migration, kiểm tra schema:

```sql
-- Kiểm tra Meeting_Transcripts
DESCRIBE `Meeting_Transcripts`;

-- Kiểm tra Meeting_Summary_Candidates  
DESCRIBE `Meeting_Summary_Candidates`;
```

Output mong đợi:
- `ai_model`: VARCHAR(20)
- `status`: VARCHAR(20)

## 📝 Thay đổi trong Code

Đã cập nhật 2 entity files:
1. `MeetingSummaryCandidate.java` - Thay ENUM definition bằng VARCHAR(20)
2. `MeetingTranscript.java` - Thay ENUM definition bằng VARCHAR(20)

Thay đổi:
```java
// CŨ (có thể gây lỗi):
@Column(nullable = false, columnDefinition = "ENUM('QWEN') DEFAULT 'QWEN'")

// MỚI (an toàn hơn):
@Column(nullable = false, length = 20)
```

## 🚀 Sau khi Fix

1. Backend sẽ có thể tạo records trong `Meeting_Summary_Candidates`
2. `processRecordedVideo()` sẽ chạy thành công
3. AI Service sẽ được gọi để xử lý video
4. Frontend sẽ nhận được status PROCESSING và tự động polling

## ❓ Troubleshooting

### Vẫn còn lỗi sau khi chạy migration?

1. **Xóa các records lỗi cũ:**
```sql
DELETE FROM `Meeting_Summary_Candidates` WHERE id IN (
  SELECT id FROM (
    SELECT id FROM `Meeting_Summary_Candidates` WHERE ai_model = ''
  ) AS tmp
);
```

2. **Reset auto_increment nếu cần:**
```sql
ALTER TABLE `Meeting_Summary_Candidates` AUTO_INCREMENT = 1;
```

3. **Kiểm tra database character set:**
```sql
SHOW CREATE TABLE `Meeting_Summary_Candidates`;
```

### AI Service không được gọi?

Sau khi fix database error:
1. Kiểm tra AI Service có đang chạy không: `http://localhost:8000/health`
2. Kiểm tra backend log có gọi đến AI service không
3. Kiểm tra network giữa backend và AI service
