package com.logistics.identity.application.service;

import com.logistics.identity.domain.entity.UserSession;
import com.logistics.identity.domain.valueobject.UserId;
import java.util.List;
import java.util.UUID;

public interface SessionManagementService {
    void revokeSession(UUID sessionId, UserId userId);

    void revokeAllSessions(UserId userId);

    List<UserSession> getActiveSessions(UserId userId);
}
