package org.lucas.arbackend.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;
import org.lucas.arbackend.dto.quiz.QuizRequest;
import org.lucas.arbackend.entity.course.ChapterQuiz;
import org.lucas.arbackend.entity.course.misc.DifficultyTypes;
import org.lucas.arbackend.entity.course.misc.StatusTypes;
import org.lucas.arbackend.util.ValidatedLabel;

import java.util.List;

@Data @Builder
@Schema(name = "CourseRequest", description = "Payload layout required to create or overwrite a curriculum course root entity")
public class CourseRequest {

    @NotBlank(message = "Staff contact email is required", groups = ValidatedLabel.OnCreate.class)
    @Email(message = "Please provide a valid email address")
    @Schema(description = "The email address of the editor or staff member managing this course", example = "editor@staff.com", requiredMode = Schema.RequiredMode.REQUIRED)
    private String staffEmail;

    @NotBlank(message = "Course name is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The official full title of the curriculum course", example = "Cloud Architecture Foundations", requiredMode = Schema.RequiredMode.REQUIRED)
    private String name;

    @NotBlank(message = "Short description is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "A concise marketing snippet or high-level summary of the course", example = "A starter guide to high-scale cloud design.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String shortDescription;

    @NotBlank(message = "Intended audience details are required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Target demographic profiles this material was designed for", example = "Junior Developers and DevOps aspirants.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String intendedAudience;

    @NotBlank(message = "Course requirements are required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Prerequisite technical skills, specific background knowledge, or setup baselines", example = "Basic understanding of networking and Linux.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String requirements;

    @NotNull(message = "Publication status is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The organizational publication state controlling student visibility", example = "DRAFT", requiredMode = Schema.RequiredMode.REQUIRED)
    private StatusTypes status;

    @NotBlank(message = "Learning objectives are required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Numbered list or text block summarizing the explicit core skills gained", example = "1. Understand VPCs 2. Deploy Load Balancers 3. Manage IAM roles.", requiredMode = Schema.RequiredMode.REQUIRED)
    private String learningObjectives;

    @NotNull(message = "Difficulty assessment type is required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Skill entry bracket assessment", example = "BEGINNER", requiredMode = Schema.RequiredMode.REQUIRED)
    private DifficultyTypes difficulty;

    @Schema(description = "Fully qualified public CDN or storage link pointing to the course cover graphic assets", example = "https://example.com/images/cloud-course.png")
    private String imageUrl;

    @NotBlank(message = "Meta indexing tags are required", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "Comma-delimited keywords used for global directory search sorting indices", example = "cloud, aws, devops", requiredMode = Schema.RequiredMode.REQUIRED)
    private String tags;

    @NotEmpty(message = "A course must contain at least one chapter configuration", groups = ValidatedLabel.OnCreate.class)
    @Schema(description = "The sequential chapter collection building up the course syllabus payload tree", requiredMode = Schema.RequiredMode.REQUIRED)
    private List<CourseChapterRequest> chapters;

    @Schema(description = "Optional standalone or detached assessment quiz request schemas assigned downstream to this course template context")
    private List<QuizRequest> quizzes;

}

