package com.example.demo.service.impl;

import com.example.demo.entity.Meeting;
import com.example.demo.event.MeetingCreatorReminderEvent;
import com.example.demo.event.MeetingMemberReminderEvent;
import com.example.demo.mapper.MeetingMapper;
import com.example.demo.repository.MeetingRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class MeetingSchedulerService {

    private static final int REMINDER_MINUTES_BEFORE = 5;

    private final MeetingRepository meetingRepository;
    private final ApplicationEventPublisher eventPublisher;
    private final MeetingMapper meetingMapper;
    private final GroupWebSocketService groupWebSocketService;

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processCreatorReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderDeadline = now.plusMinutes(REMINDER_MINUTES_BEFORE);

        List<Meeting> meetings = meetingRepository.findMeetingsNeedingCreatorReminder(now, reminderDeadline);

        for (Meeting meeting : meetings) {
            Long creatorId = meeting.getCreator().getId();
            Long groupId = meeting.getGroup().getId();

            meeting.setCreatorReminderSent(true);
            meetingRepository.save(meeting);

            eventPublisher.publishEvent(
                    new MeetingCreatorReminderEvent(creatorId, groupId, meeting.getId())
            );

            log.info("[MeetingSchedule] Creator reminder triggered for meeting {} at {}",
                    meeting.getId(), meeting.getScheduledStartAt());
        }
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processMemberReminders() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime reminderDeadline = now.plusMinutes(REMINDER_MINUTES_BEFORE);

        List<Meeting> meetings = meetingRepository.findMeetingsNeedingMemberReminder(now, reminderDeadline);

        for (Meeting meeting : meetings) {
            Long creatorId = meeting.getCreator().getId();
            Long groupId = meeting.getGroup().getId();

            meeting.setMemberReminderSent(true);
            meetingRepository.save(meeting);

            eventPublisher.publishEvent(
                    new MeetingMemberReminderEvent(creatorId, groupId, meeting.getId())
            );

            log.info("[MeetingSchedule] Member reminder triggered for meeting {} at {}",
                    meeting.getId(), meeting.getScheduledStartAt());
        }
    }

    @Scheduled(fixedRate = 60_000)
    @Transactional
    public void processDueNotifications() {
        LocalDateTime now = LocalDateTime.now();
        List<Meeting> meetings = meetingRepository.findMeetingsNeedingDueNotification(now);

        for (Meeting meeting : meetings) {
            Long groupId = meeting.getGroup().getId();

            meeting.setDueNotificationSent(true);
            meetingRepository.save(meeting);

            groupWebSocketService.broadcastScheduledMeetingDue(
                    groupId,
                    meetingMapper.toResponse(meeting)
            );

            log.info("[MeetingSchedule] Due notification broadcast for meeting {} at {}",
                    meeting.getId(), meeting.getScheduledStartAt());
        }
    }
}
