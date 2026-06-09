package org.lucas.arbackend.entity.course.misc;

import lombok.Data;

import java.io.Serializable;
import java.util.List;

@Data
public class SceneConfig implements Serializable {
    private EnvironmentFile environmentFile;
    private List<SceneObject> objects;
    private CompletionCondition completionCondition;
    private String passFeedback;
    private String failFeedback;
}

@Data
class EnvironmentFile {
    private String url;
    private String fileType;
}

@Data
class SceneObject {
    private String objectId;
    private String label;
    private String assetUrl;
    private Coordinates position;
    private Coordinates rotation;
    private Coordinates scale;
    private String interactionType;
    private boolean physicsEnabled;
    private Double proximityFailRadius;
    private String hint;
    private String requiredItemId;
    private String onInteract;
}

@Data
class Coordinates {
    private double x, y ,z;
}

@Data
class CompletionCondition {
    private String type;
    private String itemId;
    private String targetId;
}


