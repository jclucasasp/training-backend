package org.lucas.arbackend.dto.course;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import org.lucas.arbackend.dto.course.attachment.AttachmentResponse;
import org.lucas.arbackend.dto.vr.scene.VRSceneResponse;
import org.lucas.arbackend.entity.course.misc.SceneConfig;
import org.lucas.arbackend.entity.vr.scene.VRScene;
import org.lucas.arbackend.entity.vr.scene.VRSceneVersion;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Builder
@Schema(name = "ChapterSectionResponse", description = "The structural payload returned when pulling curriculum section details")
public record ChapterSectionResponse (
 @Schema(description = "Unique database entity identifier", example = "28")
        long id,

        @Schema(description = "The display title of this module", example = "Module 1: What is the Cloud?")
        String title,

        @Schema(description = "The textbook content, copy, or description of this section", example = "In this section, we discuss the evolution of data centers...")
         String content,

        @Schema(description = "True if this resource bypasses strict paywall enrollment logic", example = "false")
        boolean isPreview,

         @Schema(description = "The web track address for subtitles", example = "https://www.resource.com/subs_en.vtt")
        String subtitlesUrl,

        @Schema(description = "The duration length calculated in minutes", example = "15")
        Integer durationInMinutes,

        @Schema(description = "The media playback resource address", example = "https://www.resource.com/video.mp4")
        String resourceUrl,

        @Schema(description = "The medium categorization", example = "video")
        String resourceMediaType,

//        @Schema(description = "The 3D scene configuration", example = "https://www.resource.com/scene.json")
//        SceneConfig sceneConfig,

        @Schema(description = "The associated VR Scene")
//        Long vrSceneId,
        VRSceneResponse vrScene,

        @Schema(description = "Meta tags coupled to this section record", example = "intro, history")
        String tags,

        @Schema(description = "Array containing downloadable file assets bound to this section")
        List<AttachmentResponse> attachments
) implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
}
