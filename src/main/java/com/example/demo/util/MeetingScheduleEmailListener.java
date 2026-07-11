package com.example.demo.util;

import com.example.demo.entity.Group;
import com.example.demo.entity.Meeting;
import com.example.demo.entity.User;
import com.example.demo.event.MeetingCreatorReminderEvent;
import com.example.demo.event.MeetingScheduledEvent;
import com.example.demo.repository.GroupMemberRepository;
import com.example.demo.repository.GroupRepository;
import com.example.demo.repository.MeetingRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.impl.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class MeetingScheduleEmailListener {

    private final EmailService emailService;
    private final MeetingRepository meetingRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupRepository groupRepository;
    private final UserRepository userRepository;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleMeetingScheduled(MeetingScheduledEvent event) {
        Meeting meeting = meetingRepository.findById(event.getReferenceId()).orElse(null);
        if (meeting == null) {
            return;
        }

        User creator = userRepository.findById(event.getActorId()).orElse(null);
        Group group = groupRepository.findById(event.getGroupId()).orElse(null);
        if (creator == null || group == null) {
            return;
        }

        List<User> members = groupMemberRepository.findActiveUsersByGroupId(event.getGroupId());

        for (User member : members) {
            emailService.sendMeetingScheduledEmail(
                    member.getEmail(),
                    member.getFullname(),
                    group.getGroupName(),
                    meeting.getScheduledStartAt(),
                    creator.getFullname()
            );
        }

        log.info("[MeetingSchedule] Sent scheduled emails for meeting {} to {} members",
                meeting.getId(), members.size());
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleCreatorReminder(MeetingCreatorReminderEvent event) {
        Meeting meeting = meetingRepository.findById(event.getReferenceId()).orElse(null);
        if (meeting == null) {
            return;
        }

        User creator = userRepository.findById(event.getActorId()).orElse(null);
        Group group = groupRepository.findById(event.getGroupId()).orElse(null);
        if (creator == null || group == null) {
            return;
        }

        emailService.sendMeetingCreatorReminderEmail(
                creator.getEmail(),
                creator.getFullname(),
                group.getGroupName(),
                meeting.getScheduledStartAt()
        );

        log.info("[MeetingSchedule] Sent creator reminder email for meeting {}", meeting.getId());
    }
}
