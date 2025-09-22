package bill.zeacc.salieri.fifthgraph.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.Configuration;

@DisplayName("AgentConfig Tests")
public class AgentConfigTest {

    private AgentConfig agentConfig;

    @BeforeEach
    protected void setUp() {
        agentConfig = new AgentConfig();
    }

    @Test
    @DisplayName("Should be instantiable")
    public void shouldBeInstantiable() {
        // When/Then
        assertThat(agentConfig).isNotNull();
        assertThat(agentConfig).isInstanceOf(AgentConfig.class);
    }

    @Test
    @DisplayName("Should be annotated with Configuration")
    public void shouldBeAnnotatedWithConfiguration() {
        // When/Then
        assertThat(agentConfig.getClass().isAnnotationPresent(Configuration.class)).isTrue();
    }

    @Test
    @DisplayName("Should have Configuration annotation with correct values")
    public void shouldHaveConfigurationAnnotationWithCorrectValues() {
        // When
        Configuration configAnnotation = agentConfig.getClass().getAnnotation(Configuration.class);
        
        // Then
        assertThat(configAnnotation).isNotNull();
        assertThat(configAnnotation.value()).isEmpty(); // Default value
    }

    @Test
    @DisplayName("Should support multiple instances")
    public void shouldSupportMultipleInstances() {
        // Given
        AgentConfig config1 = new AgentConfig();
        AgentConfig config2 = new AgentConfig();
        
        // When/Then
        assertThat(config1).isNotNull();
        assertThat(config2).isNotNull();
        assertThat(config1).isNotSameAs(config2);
    }

    @Test
    @DisplayName("Should be a valid placeholder configuration class")
    public void shouldBeAValidPlaceholderConfigurationClass() {
        // When/Then
        // Verify it's a proper Spring configuration class
        assertThat(agentConfig.getClass().getPackage().getName()).isEqualTo("bill.zeacc.salieri.fifthgraph.config");
        assertThat(agentConfig.getClass().getSimpleName()).isEqualTo("AgentConfig");
        assertThat(agentConfig.getClass().isAnnotationPresent(Configuration.class)).isTrue();
    }
}