package com.cropportal.audit;

import com.cropportal.entity.AuditLog;
import com.cropportal.entity.User;
import com.cropportal.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditService {
    private final AuditLogRepository auditLogRepository;

    @Transactional
    public void record(User actor, String action, String resourceType, Long resourceId, String metadata) {
        AuditLog log = new AuditLog();
        log.setActor(actor);
        log.setUserName(actor == null ? "System" : actor.getFullName());
        log.setRoleName(actor == null || actor.getRoles().isEmpty() ? "SYSTEM" : actor.getRoles().iterator().next().getName().name());
        log.setActivity(action);
        log.setModule(resourceType);
        log.setAction(action);
        log.setResourceType(resourceType);
        log.setResourceId(resourceId);
        log.setDevice("Web");
        log.setBrowser("Unknown");
        log.setOperatingSystem("Unknown");
        log.setLoginStatus(action != null && action.toUpperCase().contains("LOGIN") ? "SUCCESS" : "N/A");
        log.setStatus("SUCCESS");
        log.setRemarks(metadata);
        log.setMetadata(metadata);
        auditLogRepository.save(log);
    }
}
