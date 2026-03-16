package org.lucas.arbackend.service.QAndA;

import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.lucas.arbackend.dto.QAndA.CourseQuestionRequest;
import org.lucas.arbackend.dto.QAndA.CourseQuestionResponse;
import org.lucas.arbackend.dto.QAndA.ReplyRequest;
import org.lucas.arbackend.dto.QAndA.ReplyResponse;
import org.lucas.arbackend.entity.Organisation.Organisation;
import org.lucas.arbackend.entity.Organisation.Staff;
import org.lucas.arbackend.entity.QAndA.CourseQuestion;
import org.lucas.arbackend.entity.QAndA.CourseQuestionReply;
import org.lucas.arbackend.entity.student.Student;
import org.lucas.arbackend.mapper.CourseDiscussionMapper;
import org.lucas.arbackend.mapper.context.MappingContext;
import org.lucas.arbackend.repository.QAndA.CourseQuestionReplyRepository;
import org.lucas.arbackend.repository.QAndA.CourseQuestionRepository;
import org.lucas.arbackend.util.TenantProvider;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class CourseDiscussionService {
  private final CourseQuestionRepository questionRepo;
    private final CourseQuestionReplyRepository replyRepo;
    private final CourseDiscussionMapper mapper;
    private final TenantProvider tenantProvider;

    public List<CourseQuestionResponse> getQuestionsByCourse(Long courseId) {
        return questionRepo.findAllByCourseIdAndOrganisationId(courseId, tenantProvider.get())
                .stream().map(mapper::toQuestionResponse).toList();
    }

    public CourseQuestionResponse askQuestion(CourseQuestionRequest request, Student student) {
        MappingContext ctx = new MappingContext(student.getOrganisation(), student, null);
        CourseQuestion question = mapper.toQuestionEntity(request, ctx);
        return mapper.toQuestionResponse(questionRepo.save(question));
    }

    public ReplyResponse postReply(Long questionId, ReplyRequest request, Student student, Staff staff) {
        Organisation org;
        if (staff != null) {
            if (!staff.getOrganisation().getId().equals(tenantProvider.get())) {
                throw new AccessDeniedException("Invalid tenant context for staff");
            }
            org = staff.getOrganisation();
        } else if (student != null) {
            if (!student.getOrganisation().getId().equals(tenantProvider.get())) {
                throw new AccessDeniedException("Invalid tenant context for student");
            }
            org = student.getOrganisation();
        } else {
            throw new AccessDeniedException("No valid author provided for reply");
        }

        CourseQuestion question = questionRepo.findByIdAndOrganisationId(questionId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        MappingContext ctx = new MappingContext(org, student, staff);
        CourseQuestionReply reply = mapper.toReplyEntity(request, ctx);
        reply.setQuestion(question);

        // 2. Privilege Check for Accepted Answer
        if (staff != null) {
            // If staff is posting and wants to accept it immediately
            if (request.isAcceptedAnswer()) {
                replyRepo.clearAcceptedAnswerForQuestion(questionId, tenantProvider.get());
                reply.setAcceptedAnswer(true);
            }
        } else {
            // Students can NEVER post a reply that is pre-accepted
            reply.setAcceptedAnswer(false);
        }

        return mapper.toReplyResponse(replyRepo.save(reply));
    }

    public void acceptAnswer(Long replyId, boolean isAcceptedAnswer, Staff staff) {

        if (staff == null || !staff.getOrganisation().getId().equals(tenantProvider.get())) {
            throw new AccessDeniedException("You are not authorised to accept an answer");
        }

        CourseQuestionReply reply = replyRepo.findByIdAndOrganisationId(replyId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Reply not found"));

         // This ensures only ONE reply per question is marked as 'Accepted'
        replyRepo.clearAcceptedAnswerForQuestion(reply.getQuestion().getId(), tenantProvider.get());

        MappingContext ctx = new MappingContext(staff.getOrganisation(), null, staff);

        reply.setAcceptedAnswer(isAcceptedAnswer);

        replyRepo.save(reply);
    }

}
