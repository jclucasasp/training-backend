package org.lucas.arbackend.dto.student;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;

import java.io.Serial;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Builder
@Schema(name = "EnrollmentResponse", description = "Output tracking record confirming an active course curriculum subscription lease state, detailing student milestones progress")
public record EnrollmentResponse (

        @Schema(description = "Unique internal database primary sequence record mapping this explicit student-to-course relationship bridge", example = "881")
        Long enrollmentId,

        @Schema(description = "The unique institutional registration identity tracking code linking back to the student record profile", example = "STU20268841")
        String studentNumber,

        @Schema(description = "The descriptive public marketing name of the target curriculum entity", example = "AWS Cloud Practitioner Essentials Architecture")
        String courseName,

        @Schema(description = "The URL slug of the course", example = "aws-cloud-practitioner-essentials-architecture")
        String courseSlug,

        @Schema(description = "ISO date-time checkpoint record detailing exactly when the student gained initial access privileges to this course", example = "2026-02-15T09:00:00")
        LocalDateTime enrolledAt,

        @Schema(description = "ISO boundary record pinning explicit overall content path completion. Returns null if the course is still ongoing.", example = "2026-05-20T16:45:11")
        LocalDateTime completedAt,

        @Schema(description = "A precision scale decimal tracking overall lesson module coverage percentage completed by the user profile", example = "72.50")
        BigDecimal currentTotalProgress

) implements Serializable {

    @Serial
    private static final long serialVersionUID = 1L;
}