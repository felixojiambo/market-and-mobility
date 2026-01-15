package com.cymelle.app.common.audit;

import com.cymelle.app.security.CurrentUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repo;

    public void log(String action, String entityType, Long entityId, String metadata) {
        Long actorId = null;
        String actorRole = null;

        try {
            var u = CurrentUser.require();
            actorId = u.getId();
            actorRole = u.getRole();
        } catch (Exception ignored) {
            // allow anonymous logs if needed
        }

        repo.save(AuditLog.builder()
                .actorId(actorId)
                .actorRole(actorRole)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .metadata(mask(metadata))
                .build());
    }

    private String mask(String metadata) {
        if (metadata == null) return null;
        // mask tokens/passwords if accidentally passed
        return metadata
                .replaceAll("(?i)\"password\"\\s*:\\s*\"[^\"]+\"", "\"password\":\"***\"")
                .replaceAll("(?i)\"token\"\\s*:\\s*\"[^\"]+\"", "\"token\":\"***\"")
                .replaceAll("(?i)\"refreshToken\"\\s*:\\s*\"[^\"]+\"", "\"refreshToken\":\"***\"");
    }
}
