package bill.zeacc.salieri.fifthgraph.service;

import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicInteger ;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

/**
 * Service responsible for managing user sessions.
 * Extracted from the main application to enable testing without threading concerns.
 */
@Service
@Slf4j
public class SessionManager {

    private final ConcurrentMap<String, SessionContext> sessions = new ConcurrentHashMap<>();

    /**
     * Creates a new session and returns its ID.
     *
     * @return the unique session ID
     */
    public String createSession() {
        String sessionId = UUID.randomUUID().toString();
        SessionContext context = new SessionContext(sessionId, System.currentTimeMillis());
        sessions.put(sessionId, context);
        
        log.debug("Created new session: {}", sessionId);
        return sessionId;
    }

    /**
     * Retrieves session context by ID.
     *
     * @param sessionId the session ID
     * @return the session context, or null if not found
     */
    public SessionContext getSession(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        if (context != null) {
            context.updateLastAccessed();
        }
        return context;
    }

    /**
     * Checks if a session exists and is active.
     *
     * @param sessionId the session ID to check
     * @return true if the session exists and is active
     */
    public boolean isSessionActive(String sessionId) {
        SessionContext context = sessions.get(sessionId);
        return context != null && context.isActive();
    }

    /**
     * Terminates a session.
     *
     * @param sessionId the session ID to terminate
     * @return true if the session was found and terminated
     */
    public boolean terminateSession(String sessionId) {
        SessionContext context = sessions.remove(sessionId);
        if (context != null) {
            context.setActive(false);
            log.debug("Terminated session: {}", sessionId);
            return true;
        }
        return false;
    }

    /**
     * Gets the total number of active sessions.
     *
     * @return the count of active sessions
     */
    public int getActiveSessionCount() {
        return (int) sessions.values().stream()
            .filter(SessionContext::isActive)
            .count();
    }

    /**
     * Cleans up inactive sessions older than the specified age.
     *
     * @param maxAgeMs maximum age in milliseconds
     * @return number of sessions cleaned up
     */
    public int cleanupOldSessions(long maxAgeMs) {
        long cutoffTime = System.currentTimeMillis() - maxAgeMs;
        AtomicInteger cleanedUp = new AtomicInteger(0);
        
        sessions.entrySet().removeIf(entry -> {
            SessionContext context = entry.getValue();
            if (context.getCreatedTime() < cutoffTime || !context.isActive()) {
                log.debug("Cleaning up old session: {}", entry.getKey());
                cleanedUp.incrementAndGet ( ) ;
                return true;
            }
            return false;
        });
        
        if (cleanedUp.get ( ) > 0) {
            log.info("Cleaned up {} old sessions", cleanedUp);
        }
        
        return cleanedUp.get ( ) ;
    }

    /**
     * Represents the context of a user session.
     */
    public static class SessionContext {
        private final String sessionId;
        private final long createdTime;
        private volatile long lastAccessed;
        private volatile boolean active;

        public SessionContext(String sessionId, long createdTime) {
            this.sessionId = sessionId;
            this.createdTime = createdTime;
            this.lastAccessed = createdTime;
            this.active = true;
        }

        public String getSessionId() { return sessionId; }
        public long getCreatedTime() { return createdTime; }
        public long getLastAccessed() { return lastAccessed; }
        public boolean isActive() { return active; }

        public void updateLastAccessed() {
            this.lastAccessed = System.currentTimeMillis();
        }

        public void setActive(boolean active) {
            this.active = active;
        }

        public long getAge() {
            return System.currentTimeMillis() - createdTime;
        }

        public long getIdleTime() {
            return System.currentTimeMillis() - lastAccessed;
        }
    }
}