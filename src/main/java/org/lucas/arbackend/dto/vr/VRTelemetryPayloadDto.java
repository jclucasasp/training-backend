package org.lucas.arbackend.dto.vr;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.lucas.arbackend.dto.vr.event.VREventRequest;

import java.io.Serial;
import java.io.Serializable;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VRTelemetryPayloadDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 1L;
    
    private Long sessionId;
    private Long orgId;
    private List<VREventRequest> events;
}
