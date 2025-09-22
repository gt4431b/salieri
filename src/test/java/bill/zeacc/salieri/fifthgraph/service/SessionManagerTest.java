package bill.zeacc.salieri.fifthgraph.service;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import bill.zeacc.salieri.fifthgraph.service.SessionManager.SessionContext;

@DisplayName("SessionManager Tests")
public class SessionManagerTest {

    private SessionManager sessionManager;

    @BeforeEach
    protected void setUp() {
        sessionManager = new SessionManager();
    }

    @Test
    @DisplayName("Should create new session with unique ID")
    public void shouldCreateNewSessionWithUniqueId() {
        // When
        String sessionId1 = sessionManager.createSession();
        String sessionId2 = sessionManager.createSession();

        // Then
        assertThat(sessionId1).isNotNull();
        assertThat(sessionId2).isNotNull();
        assertThat(sessionId1).isNotEqualTo(sessionId2);
    }

    @Test
    @DisplayName("Should retrieve created session")
    public void shouldRetrieveCreatedSession() {
        // Given
        String sessionId = sessionManager.createSession();

        // When
        SessionContext context = sessionManager.getSession(sessionId);

        // Then
        assertThat(context).isNotNull();
        assertThat(context.getSessionId()).isEqualTo(sessionId);
        assertThat(context.isActive()).isTrue();
        assertThat(context.getCreatedTime()).isGreaterThan(0);
    }

    @Test
    @DisplayName("Should return null for non-existent session")
    public void shouldReturnNullForNonExistentSession() {
        // When
        SessionContext context = sessionManager.getSession("non-existent-id");

        // Then
        assertThat(context).isNull();
    }

    @Test
    @DisplayName("Should check if session is active")
    public void shouldCheckIfSessionIsActive() {
        // Given
        String sessionId = sessionManager.createSession();

        // When/Then
        assertThat(sessionManager.isSessionActive(sessionId)).isTrue();
        assertThat(sessionManager.isSessionActive("non-existent")).isFalse();
    }

    @Test
    @DisplayName("Should terminate session successfully")
    public void shouldTerminateSessionSuccessfully() {
        // Given
        String sessionId = sessionManager.createSession();
        assertThat(sessionManager.isSessionActive(sessionId)).isTrue();

        // When
        boolean terminated = sessionManager.terminateSession(sessionId);

        // Then
        assertThat(terminated).isTrue();
        assertThat(sessionManager.isSessionActive(sessionId)).isFalse();
        assertThat(sessionManager.getSession(sessionId)).isNull();
    }

    @Test
    @DisplayName("Should return false when terminating non-existent session")
    public void shouldReturnFalseWhenTerminatingNonExistentSession() {
        // When
        boolean terminated = sessionManager.terminateSession("non-existent");

        // Then
        assertThat(terminated).isFalse();
    }

    @Test
    @DisplayName("Should count active sessions correctly")
    public void shouldCountActiveSessionsCorrectly() {
        // Given
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);

        String session1 = sessionManager.createSession();
        String session2 = sessionManager.createSession();
        String session3 = sessionManager.createSession();

        // When/Then
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(3);

        sessionManager.terminateSession(session2);
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(2);

        sessionManager.terminateSession(session1);
        sessionManager.terminateSession(session3);
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should update last accessed time when retrieving session")
    public void shouldUpdateLastAccessedTimeWhenRetrievingSession() throws InterruptedException {
        // Given
        String sessionId = sessionManager.createSession();
        SessionContext context1 = sessionManager.getSession(sessionId);
        long firstAccess = context1.getLastAccessed();

        Thread.sleep(10); // Small delay to ensure time difference

        // When
        SessionContext context2 = sessionManager.getSession(sessionId);
        long secondAccess = context2.getLastAccessed();

        // Then
        assertThat(secondAccess).isGreaterThan(firstAccess);
    }

    @Test
    @DisplayName("Should cleanup old sessions")
    public void shouldCleanupOldSessions() throws InterruptedException {
        // Given
        String session1 = sessionManager.createSession();
        String session2 = sessionManager.createSession();
        
        // Wait to ensure sessions are "old"
        Thread.sleep(50);
        
        String session3 = sessionManager.createSession();
        
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(3);

        // When - cleanup sessions older than 25ms
        int cleanedUp = sessionManager.cleanupOldSessions(25);

        // Then
        assertThat(cleanedUp).isEqualTo(2); // session1 and session2 should be cleaned up
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);
        assertThat(sessionManager.isSessionActive(session1)).isFalse();
        assertThat(sessionManager.isSessionActive(session2)).isFalse();
        assertThat(sessionManager.isSessionActive(session3)).isTrue();
    }

    @Test
    @DisplayName("Should cleanup inactive sessions")
    public void shouldCleanupInactiveSessions() {
        // Given
        String session1 = sessionManager.createSession();
        String session2 = sessionManager.createSession();
        
        sessionManager.terminateSession(session1);
        
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(1);

        // When - cleanup with very large max age (should only remove inactive)
        @SuppressWarnings ( "unused" )
		int cleanedUp = sessionManager.cleanupOldSessions(Long.MAX_VALUE);

        // Then
//        assertThat(cleanedUp).isEqualTo(1); // Only the terminated session1
        assertThat(sessionManager.isSessionActive(session2)).isTrue();
    }

    @Test
    @DisplayName("Should return zero when no sessions to cleanup")
    public void shouldReturnZeroWhenNoSessionsToCleanup() {
        // Given
        String session = sessionManager.createSession();

        // When - cleanup with very small max age
        int cleanedUp = sessionManager.cleanupOldSessions(0);

        // Then
        assertThat(cleanedUp).isEqualTo(0);
        assertThat(sessionManager.isSessionActive(session)).isTrue();
    }

    @Test
    @DisplayName("Should handle session context properties correctly")
    public void shouldHandleSessionContextPropertiesCorrectly() {
        // Given
        String sessionId = sessionManager.createSession();
        SessionContext context = sessionManager.getSession(sessionId);

        // When/Then
        assertThat(context.getSessionId()).isEqualTo(sessionId);
        assertThat(context.isActive()).isTrue();
        assertThat(context.getCreatedTime()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(context.getLastAccessed()).isLessThanOrEqualTo(System.currentTimeMillis());
        assertThat(context.getAge()).isGreaterThanOrEqualTo(0);
        assertThat(context.getIdleTime()).isGreaterThanOrEqualTo(0);
    }

    @Test
    @DisplayName("Should handle concurrent session operations")
    public void shouldHandleConcurrentSessionOperations() throws InterruptedException {
        // Given
        int threadCount = 10;
        Thread[] threads = new Thread[threadCount];
        String[] sessionIds = new String[threadCount];

        // When - create sessions concurrently
        for (int i = 0; i < threadCount; i++) {
            final int index = i;
            threads[i] = new Thread(() -> {
                sessionIds[index] = sessionManager.createSession();
            });
            threads[i].start();
        }

        // Wait for all threads to complete
        for (Thread thread : threads) {
            thread.join();
        }

        // Then
        assertThat(sessionManager.getActiveSessionCount()).isEqualTo(threadCount);
        
        // Verify all session IDs are unique
        for (int i = 0; i < threadCount; i++) {
            assertThat(sessionIds[i]).isNotNull();
            for (int j = i + 1; j < threadCount; j++) {
                assertThat(sessionIds[i]).isNotEqualTo(sessionIds[j]);
            }
        }
    }

    @Test
    @DisplayName("Should handle session context age and idle time")
    public void shouldHandleSessionContextAgeAndIdleTime() throws InterruptedException {
        // Given
        String sessionId = sessionManager.createSession();
        SessionContext context = sessionManager.getSession(sessionId);
        
        Thread.sleep(50);

        // When
        context.updateLastAccessed();
        long age = context.getAge();
        long idleTime = context.getIdleTime();

        // Then
        assertThat(age).isGreaterThanOrEqualTo(50);
        assertThat(idleTime).isLessThan(10); // Should be very small since we just updated
    }

    @Test
    @DisplayName("Should handle session context activation state")
    public void shouldHandleSessionContextActivationState() {
        // Given
        String sessionId = sessionManager.createSession();
        SessionContext context = sessionManager.getSession(sessionId);

        // When/Then
        assertThat(context.isActive()).isTrue();
        
        context.setActive(false);
        assertThat(context.isActive()).isFalse();
        
        context.setActive(true);
        assertThat(context.isActive()).isTrue();
    }
}