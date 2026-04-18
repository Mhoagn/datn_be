-- =============================================
-- Database Schema for Video Call Platform
-- Similar to Microsoft Teams
-- Fixed and Optimized Version v4
-- =============================================

CREATE TABLE `Users`(
    `id`                   BIGINT                        NOT NULL AUTO_INCREMENT,
    `email`                VARCHAR(255)                  NOT NULL,
    `password`             VARCHAR(255)                  NOT NULL,
    `fullname`             VARCHAR(255)                  NOT NULL,
    `birthday`             DATE                          NULL,
    `avatar_url`           VARCHAR(500)                  NULL,
    `cloudinary_avatar_id` VARCHAR(255)                  NULL,
    `online_status`        ENUM('ONLINE', 'OFFLINE')     NOT NULL DEFAULT 'OFFLINE',
    `last_seen_at`         TIMESTAMP                     NULL,
    `is_active`            TINYINT(1)                    NOT NULL DEFAULT 1,
    `created_at`           TIMESTAMP                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           TIMESTAMP                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_email`     (`email`),
    INDEX `idx_email`             (`email`),
    INDEX `idx_is_active`         (`is_active`),
    INDEX `idx_online_status`     (`online_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Groups`(
    `id`                   BIGINT        NOT NULL AUTO_INCREMENT,
    `group_name`           VARCHAR(255)  NOT NULL,
    `avatar_url`           VARCHAR(500)  NULL,
    `cloudinary_avatar_id` VARCHAR(255)  NULL,
    `join_code`            VARCHAR(50)   NOT NULL,
    `created_by`           BIGINT        NOT NULL,
    `is_deleted`           TINYINT(1)    NOT NULL DEFAULT 0,
    `created_at`           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`           TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_join_code` (`join_code`),
    INDEX `idx_join_code`         (`join_code`),
    INDEX `idx_created_by`        (`created_by`),
    INDEX `idx_is_deleted`        (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Group_Members`(
    `id`        BIGINT                       NOT NULL AUTO_INCREMENT,
    `user_id`   BIGINT                       NOT NULL,
    `group_id`  BIGINT                       NOT NULL,
    `role`      ENUM('OWNER', 'MEMBER')      NOT NULL DEFAULT 'MEMBER',
    `joined_at` TIMESTAMP                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `left_at`   TIMESTAMP                    NULL,
    `is_active` TINYINT(1)                   NOT NULL DEFAULT 1,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_user_group` (`user_id`, `group_id`),
    INDEX `idx_user_id`            (`user_id`),
    INDEX `idx_group_id`           (`group_id`),
    INDEX `idx_role`               (`role`),
    INDEX `idx_is_active`          (`is_active`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Group_Join_Requests`(
    `id`          BIGINT                                        NOT NULL AUTO_INCREMENT,
    `group_id`    BIGINT                                        NOT NULL,
    `user_id`     BIGINT                                        NOT NULL,
    `status`      ENUM('PENDING', 'APPROVED', 'REJECTED')       NOT NULL DEFAULT 'PENDING',
    `message`     TEXT                                          NULL,
    `reviewed_by` BIGINT                                        NULL,
    `reviewed_at` TIMESTAMP                                     NULL,
    `created_at`  TIMESTAMP                                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP                                     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_group_user_request` (`group_id`, `user_id`),
    INDEX `idx_group_id`   (`group_id`),
    INDEX `idx_user_id`    (`user_id`),
    INDEX `idx_status`     (`status`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Posts`(
    `id`               BIGINT                                              NOT NULL AUTO_INCREMENT,
    `group_id`         BIGINT                                              NOT NULL,
    `author_id`        BIGINT                                              NOT NULL,
    `content`          TEXT                                                NOT NULL,
    `media_urls`       JSON                                                NULL,
    `media_public_ids` JSON                                                NULL,
    `media_type`       ENUM('NONE', 'IMAGE', 'VIDEO', 'FILE', 'MULTIPLE')  NULL DEFAULT 'NONE',
    `is_deleted`       TINYINT(1)                                          NOT NULL DEFAULT 0,
    `created_at`       TIMESTAMP                                           NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       TIMESTAMP                                           NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_group_id`   (`group_id`),
    INDEX `idx_author_id`  (`author_id`),
    INDEX `idx_created_at` (`created_at`),
    INDEX `idx_is_deleted` (`is_deleted`),
    INDEX `idx_media_type` (`media_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Post_Comments`(
    `id`                BIGINT     NOT NULL AUTO_INCREMENT,
    `post_id`           BIGINT     NOT NULL,
    `author_id`         BIGINT     NOT NULL,
    `parent_comment_id` BIGINT     NULL,
    `content`           TEXT       NOT NULL,
    `is_deleted`        TINYINT(1) NOT NULL DEFAULT 0,
    `created_at`        TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`        TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_post_id`           (`post_id`),
    INDEX `idx_author_id`         (`author_id`),
    INDEX `idx_parent_comment_id` (`parent_comment_id`),
    INDEX `idx_created_at`        (`created_at`),
    INDEX `idx_is_deleted`        (`is_deleted`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Post_Likes`(
    `id`         BIGINT    NOT NULL AUTO_INCREMENT,
    `post_id`    BIGINT    NOT NULL,
    `user_id`    BIGINT    NOT NULL,
    `created_at` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_post_user_like` (`post_id`, `user_id`),
    INDEX `idx_post_id` (`post_id`),
    INDEX `idx_user_id` (`user_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meetings`(
    `id`                BIGINT                         NOT NULL AUTO_INCREMENT,
    `group_id`          BIGINT                         NOT NULL,
    `meeting_title`     VARCHAR(255)                   NULL,
    `created_by`        BIGINT                         NOT NULL,
    `started_at`        TIMESTAMP                      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `ended_at`          TIMESTAMP                      NULL,
    `status`            ENUM('ONGOING', 'END')         NOT NULL DEFAULT 'ONGOING',
    `livekit_room_name` VARCHAR(255)                   NOT NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_group_id`   (`group_id`),
    INDEX `idx_created_by` (`created_by`),
    INDEX `idx_started_at` (`started_at`),
    INDEX `idx_status`     (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meeting_Participants`(
    `id`               BIGINT                             NOT NULL AUTO_INCREMENT,
    `meeting_id`       BIGINT                             NOT NULL,
    `user_id`          BIGINT                             NOT NULL,
    `livekit_identity` VARCHAR(255)                       NOT NULL,
    `role`             ENUM('HOST', 'PARTICIPANT')         NOT NULL DEFAULT 'PARTICIPANT',
    `session_index`    TINYINT UNSIGNED                   NOT NULL DEFAULT 1,
    `joined_at`        TIMESTAMP                          NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `left_at`          TIMESTAMP                          NULL,
    `duration_seconds` INT UNSIGNED                       NULL,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_meeting_user_session` (`meeting_id`, `user_id`, `session_index`),
    INDEX `idx_meeting_id` (`meeting_id`),
    INDEX `idx_user_id`    (`user_id`),
    INDEX `idx_joined_at`  (`joined_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meeting_Messages`(
    `id`                   BIGINT                       NOT NULL AUTO_INCREMENT,
    `meeting_id`           BIGINT                       NOT NULL,
    `author_id`            BIGINT                       NOT NULL,
    `content`              TEXT                         NULL,
    `message_type`         ENUM('TEXT', 'IMAGE', 'FILE') NOT NULL DEFAULT 'TEXT',
    `attachment_url`       VARCHAR(1000)                NULL,
    `attachment_public_id` VARCHAR(255)                 NULL,
    `attachment_name`      VARCHAR(255)                 NULL,
    `attachment_size`      BIGINT                       NULL,
    `sent_at`              TIMESTAMP                    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_meeting_id`   (`meeting_id`),
    INDEX `idx_author_id`    (`author_id`),
    INDEX `idx_sent_at`      (`sent_at`),
    INDEX `idx_message_type` (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meeting_Records`(
    `id`               BIGINT                                     NOT NULL AUTO_INCREMENT,
    `meeting_id`       BIGINT                                     NOT NULL,
    `egress_id`        VARCHAR(255)                               NULL,
    `recorded_by`      BIGINT                                     NULL,
    `file_name`        VARCHAR(255)                               NOT NULL,
    `s3_key`           VARCHAR(500)                               NULL,
    `s3_bucket`        VARCHAR(255)                               NULL,
    `storage_url`      VARCHAR(1000)                              NULL,
    `file_size_bytes`  BIGINT                                     NULL,
    `duration_seconds` INT UNSIGNED                               NULL,
    `format`           VARCHAR(50)                                NULL,
    `status`           ENUM('PROCESSING', 'COMPLETED', 'FAILED')  NOT NULL DEFAULT 'PROCESSING',
    `created_at`       TIMESTAMP                                  NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`       TIMESTAMP                                  NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE INDEX `idx_egress_id` (`egress_id`),
    INDEX `idx_meeting_id`       (`meeting_id`),
    INDEX `idx_status`           (`status`),
    INDEX `idx_created_at`       (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meeting_Transcripts`(
    `id`                BIGINT    NOT NULL AUTO_INCREMENT,
    `meeting_record_id` BIGINT    NOT NULL,
    `segments`          JSON      NULL,
    `full_text`         TEXT      NULL,
    `status`  ENUM('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING',
    `error_message`     TEXT      NULL,
    `created_at`        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_record_transcript` (`meeting_record_id`),
    INDEX `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meeting_Summary_Candidates`(
    `id`                BIGINT                        NOT NULL AUTO_INCREMENT,
    `meeting_record_id` BIGINT                        NOT NULL,
    `transcript_id`     BIGINT                        NOT NULL,
    `ai_model`          ENUM('QWEN')                  NOT NULL DEFAULT 'QWEN',
    `raw_summary`       TEXT                          NULL,
    `status`  ENUM('PENDING','PROCESSING','COMPLETED','FAILED') NOT NULL DEFAULT 'PENDING',
    `error_message`     TEXT                          NULL,
    `created_at`        TIMESTAMP                     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_record_model`  (`meeting_record_id`, `ai_model`),
    INDEX `idx_record_id`             (`meeting_record_id`),
    INDEX `idx_transcript_id`         (`transcript_id`),
    INDEX `idx_status`                (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meeting_Summary_Points`(
    `id`           BIGINT           NOT NULL AUTO_INCREMENT,
    `candidate_id` BIGINT           NOT NULL,
    `content`      TEXT             NOT NULL,
    `order_index`  TINYINT UNSIGNED NOT NULL DEFAULT 0,
    `is_selected`  TINYINT(1)       NOT NULL DEFAULT 0,
    `created_at`   TIMESTAMP        NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    INDEX `idx_candidate_id` (`candidate_id`),
    INDEX `idx_is_selected`  (`is_selected`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Meeting_Summary_Final`(
    `id`                 BIGINT    NOT NULL AUTO_INCREMENT,
    `meeting_record_id`  BIGINT    NOT NULL,
    `created_by`         BIGINT    NOT NULL,
    `final_content`      TEXT      NOT NULL,
    `selected_point_ids` JSON      NOT NULL,
    `created_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_record_final` (`meeting_record_id`),
    INDEX `idx_record_id`  (`meeting_record_id`),
    INDEX `idx_created_by` (`created_by`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Conversations`(
    `id`              BIGINT    NOT NULL AUTO_INCREMENT,
    `user1_id`        BIGINT    NOT NULL,
    `user2_id`        BIGINT    NOT NULL,
    `last_message_at` TIMESTAMP NULL,
    `created_at`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `updated_at`      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_conversation`  (`user1_id`, `user2_id`),
    INDEX `idx_user1_id`              (`user1_id`),
    INDEX `idx_user2_id`              (`user2_id`),
    INDEX `idx_last_message_at`       (`last_message_at`),
    CONSTRAINT `chk_user_order`   CHECK (`user1_id` < `user2_id`),
    CONSTRAINT `chk_no_self_chat` CHECK (`user1_id` != `user2_id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


CREATE TABLE `Messages`(
    `id`                   BIGINT                                          NOT NULL AUTO_INCREMENT,
    `conversation_id`      BIGINT                                          NOT NULL,
    `sender_id`            BIGINT                                          NOT NULL,
    `content`              TEXT                                            NULL,
    `message_type`         ENUM('TEXT', 'IMAGE', 'VIDEO', 'FILE', 'AUDIO') NOT NULL DEFAULT 'TEXT',
    `attachment_url`       VARCHAR(1000)                                   NULL,
    `attachment_public_id` VARCHAR(255)                                    NULL,
    `attachment_name`      VARCHAR(255)                                    NULL,
    `attachment_size`      BIGINT                                          NULL,
    `is_read`              TINYINT(1)                                      NOT NULL DEFAULT 0,
    `is_deleted`           TINYINT(1)                                      NOT NULL DEFAULT 0,
    `sent_at`              TIMESTAMP                                       NOT NULL DEFAULT CURRENT_TIMESTAMP,
    `read_at`              TIMESTAMP                                       NULL,
    PRIMARY KEY (`id`),
    INDEX `idx_conversation_id` (`conversation_id`),
    INDEX `idx_sender_id`       (`sender_id`),
    INDEX `idx_sent_at`         (`sent_at`),
    INDEX `idx_is_read`         (`is_read`),
    INDEX `idx_is_deleted`      (`is_deleted`),
    INDEX `idx_message_type`    (`message_type`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

CREATE TABLE `Notifications`(
    `id`             BIGINT    NOT NULL AUTO_INCREMENT,
    `type`           ENUM(
                       'NEW_POST',
                       'NEW_MEMBER',
                       'NEW_MEETING',
                       'NEW_SUMMARY',
                       'JOIN_REQUEST'
                     ) NOT NULL,
    `actor_id`       BIGINT       NULL,   -- người thực hiện hành động
    `group_id`       BIGINT       NULL,   -- nhóm liên quan
    `reference_id`   BIGINT       NULL,   -- post_id / meeting_id / record_id / request_id
    `reference_type` VARCHAR(50)  NULL,   -- 'Posts', 'Meetings', 'Meeting_Records', 'Group_Join_Requests'
    `created_at`     TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    INDEX `idx_type`       (`type`),
    INDEX `idx_group_id`   (`group_id`),
    INDEX `idx_actor_id`   (`actor_id`),
    INDEX `idx_created_at` (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- Bảng 2: Ai nhận thông báo đó, đã đọc chưa
CREATE TABLE `User_Notifications`(
    `id`              BIGINT     NOT NULL AUTO_INCREMENT,
    `user_id`         BIGINT     NOT NULL,   -- người nhận
    `notification_id` BIGINT     NOT NULL,   -- FK → Notifications
    `is_read`         TINYINT(1) NOT NULL DEFAULT 0,
    `read_at`         TIMESTAMP  NULL,
    `created_at`      TIMESTAMP  NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (`id`),
    UNIQUE KEY `unique_user_notification` (`user_id`, `notification_id`),
    INDEX `idx_user_id`         (`user_id`),
    INDEX `idx_notification_id` (`notification_id`),
    INDEX `idx_is_read`         (`is_read`),
    INDEX `idx_created_at`      (`created_at`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;


-- =============================================
-- Foreign Key Constraints
-- =============================================

ALTER TABLE `Groups`
    ADD CONSTRAINT `fk_groups_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `Users`(`id`) ON DELETE RESTRICT;

ALTER TABLE `Group_Members`
    ADD CONSTRAINT `fk_group_members_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Group_Members`
    ADD CONSTRAINT `fk_group_members_group_id`
    FOREIGN KEY (`group_id`) REFERENCES `Groups`(`id`) ON DELETE CASCADE;

ALTER TABLE `Group_Join_Requests`
    ADD CONSTRAINT `fk_group_join_requests_group_id`
    FOREIGN KEY (`group_id`) REFERENCES `Groups`(`id`) ON DELETE CASCADE;

ALTER TABLE `Group_Join_Requests`
    ADD CONSTRAINT `fk_group_join_requests_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Group_Join_Requests`
    ADD CONSTRAINT `fk_group_join_requests_reviewed_by`
    FOREIGN KEY (`reviewed_by`) REFERENCES `Users`(`id`) ON DELETE SET NULL;

ALTER TABLE `Posts`
    ADD CONSTRAINT `fk_posts_group_id`
    FOREIGN KEY (`group_id`) REFERENCES `Groups`(`id`) ON DELETE CASCADE;

ALTER TABLE `Posts`
    ADD CONSTRAINT `fk_posts_author_id`
    FOREIGN KEY (`author_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Post_Comments`
    ADD CONSTRAINT `fk_post_comments_post_id`
    FOREIGN KEY (`post_id`) REFERENCES `Posts`(`id`) ON DELETE CASCADE;

ALTER TABLE `Post_Comments`
    ADD CONSTRAINT `fk_post_comments_author_id`
    FOREIGN KEY (`author_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Post_Comments`
    ADD CONSTRAINT `fk_post_comments_parent_comment_id`
    FOREIGN KEY (`parent_comment_id`) REFERENCES `Post_Comments`(`id`) ON DELETE CASCADE;

ALTER TABLE `Post_Likes`
    ADD CONSTRAINT `fk_post_likes_post_id`
    FOREIGN KEY (`post_id`) REFERENCES `Posts`(`id`) ON DELETE CASCADE;

ALTER TABLE `Post_Likes`
    ADD CONSTRAINT `fk_post_likes_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meetings`
    ADD CONSTRAINT `fk_meetings_group_id`
    FOREIGN KEY (`group_id`) REFERENCES `Groups`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meetings`
    ADD CONSTRAINT `fk_meetings_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `Users`(`id`) ON DELETE RESTRICT;

ALTER TABLE `Meeting_Participants`
    ADD CONSTRAINT `fk_meeting_participants_meeting_id`
    FOREIGN KEY (`meeting_id`) REFERENCES `Meetings`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Participants`
    ADD CONSTRAINT `fk_meeting_participants_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Messages`
    ADD CONSTRAINT `fk_meeting_messages_meeting_id`
    FOREIGN KEY (`meeting_id`) REFERENCES `Meetings`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Messages`
    ADD CONSTRAINT `fk_meeting_messages_author_id`
    FOREIGN KEY (`author_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Records`
    ADD CONSTRAINT `fk_meeting_records_meeting_id`
    FOREIGN KEY (`meeting_id`) REFERENCES `Meetings`(`id`) ON DELETE CASCADE;

-- ✅ THÊM: FK recorded_by
ALTER TABLE `Meeting_Records`
    ADD CONSTRAINT `fk_meeting_records_recorded_by`
    FOREIGN KEY (`recorded_by`) REFERENCES `Users`(`id`) ON DELETE SET NULL;

-- ✅ THÊM: FK cho các bảng Summary
ALTER TABLE `Meeting_Transcripts`
    ADD CONSTRAINT `fk_meeting_transcripts_record_id`
    FOREIGN KEY (`meeting_record_id`) REFERENCES `Meeting_Records`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Summary_Candidates`
    ADD CONSTRAINT `fk_summary_candidates_record_id`
    FOREIGN KEY (`meeting_record_id`) REFERENCES `Meeting_Records`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Summary_Candidates`
    ADD CONSTRAINT `fk_summary_candidates_transcript_id`
    FOREIGN KEY (`transcript_id`) REFERENCES `Meeting_Transcripts`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Summary_Points`
    ADD CONSTRAINT `fk_summary_points_candidate_id`
    FOREIGN KEY (`candidate_id`) REFERENCES `Meeting_Summary_Candidates`(`id`) ON DELETE CASCADE;

-- ✅ SỬA: Meeting_Summary → Meeting_Summary_Final
ALTER TABLE `Meeting_Summary_Final`
    ADD CONSTRAINT `fk_meeting_summary_final_record_id`
    FOREIGN KEY (`meeting_record_id`) REFERENCES `Meeting_Records`(`id`) ON DELETE CASCADE;

ALTER TABLE `Meeting_Summary_Final`
    ADD CONSTRAINT `fk_meeting_summary_final_created_by`
    FOREIGN KEY (`created_by`) REFERENCES `Users`(`id`) ON DELETE RESTRICT;

ALTER TABLE `Conversations`
    ADD CONSTRAINT `fk_conversations_user1_id`
    FOREIGN KEY (`user1_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Conversations`
    ADD CONSTRAINT `fk_conversations_user2_id`
    FOREIGN KEY (`user2_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Messages`
    ADD CONSTRAINT `fk_messages_conversation_id`
    FOREIGN KEY (`conversation_id`) REFERENCES `Conversations`(`id`) ON DELETE CASCADE;

ALTER TABLE `Messages`
    ADD CONSTRAINT `fk_messages_sender_id`
    FOREIGN KEY (`sender_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `Notifications`
    ADD CONSTRAINT `fk_notifications_actor_id`
    FOREIGN KEY (`actor_id`) REFERENCES `Users`(`id`) ON DELETE SET NULL;

ALTER TABLE `Notifications`
    ADD CONSTRAINT `fk_notifications_group_id`
    FOREIGN KEY (`group_id`) REFERENCES `Groups`(`id`) ON DELETE CASCADE;

ALTER TABLE `User_Notifications`
    ADD CONSTRAINT `fk_user_notifications_user_id`
    FOREIGN KEY (`user_id`) REFERENCES `Users`(`id`) ON DELETE CASCADE;

ALTER TABLE `User_Notifications`
    ADD CONSTRAINT `fk_user_notifications_notification_id`
    FOREIGN KEY (`notification_id`) REFERENCES `Notifications`(`id`) ON DELETE CASCADE;