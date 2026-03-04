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

    @Transactional
    public CourseQuestionResponse askQuestion(CourseQuestionRequest request, Student student) {
        MappingContext ctx = new MappingContext(student.getOrganisation(), student, null);
        CourseQuestion question = mapper.toQuestionEntity(request, ctx);
        return mapper.toQuestionResponse(questionRepo.save(question));
    }

    @Transactional
    public ReplyResponse postReply(Long questionId, ReplyRequest request, Student student, Staff staff) {
        CourseQuestion question = questionRepo.findByIdAndOrganisationId(questionId, tenantProvider.get())
                .orElseThrow(() -> new EntityNotFoundException("Question not found"));

        Organisation org = (staff != null) ? staff.getOrganisation() : student.getOrganisation();
        MappingContext ctx = new MappingContext(org, student, staff);

        CourseQuestionReply reply = mapper.toReplyEntity(request, ctx);
        reply.setQuestion(question);

        return mapper.toReplyResponse(replyRepo.save(reply));
    }
}
