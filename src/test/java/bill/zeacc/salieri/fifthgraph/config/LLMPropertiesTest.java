package bill.zeacc.salieri.fifthgraph.config;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("LLMProperties Tests")
public class LLMPropertiesTest {

    private LLMProperties llmProperties;

    @BeforeEach
    protected void setUp() {
        llmProperties = new LLMProperties();
    }

    @Test
    @DisplayName("Should have default provider value")
    public void shouldHaveDefaultProviderValue() {
        // When/Then
        assertThat(llmProperties.getProvider()).isEqualTo("ollama");
    }

    @Test
    @DisplayName("Should have default temperature value")
    public void shouldHaveDefaultTemperatureValue() {
        // When/Then
        assertThat(llmProperties.getTemperature()).isEqualTo(0.3);
    }

    @Test
    @DisplayName("Should have default ollama model value")
    public void shouldHaveDefaultOllamaModelValue() {
        // When/Then
        assertThat(llmProperties.getOllamaModel()).isEqualTo("ikiru/Dolphin-Mistral-24B-Venice-Edition:latest");
    }

    @Test
    @DisplayName("Should have default openai model value")
    public void shouldHaveDefaultOpenaiModelValue() {
        // When/Then
        assertThat(llmProperties.getOpenaiModel()).isEqualTo("gpt-4");
    }

    @Test
    @DisplayName("Should allow setting provider")
    public void shouldAllowSettingProvider() {
        // Given
        String newProvider = "openai";
        
        // When
        llmProperties.setProvider(newProvider);
        
        // Then
        assertThat(llmProperties.getProvider()).isEqualTo(newProvider);
    }

    @Test
    @DisplayName("Should allow setting temperature")
    public void shouldAllowSettingTemperature() {
        // Given
        Double newTemperature = 0.7;
        
        // When
        llmProperties.setTemperature(newTemperature);
        
        // Then
        assertThat(llmProperties.getTemperature()).isEqualTo(newTemperature);
    }

    @Test
    @DisplayName("Should allow setting ollama model")
    public void shouldAllowSettingOllamaModel() {
        // Given
        String newModel = "llama3:latest";
        
        // When
        llmProperties.setOllamaModel(newModel);
        
        // Then
        assertThat(llmProperties.getOllamaModel()).isEqualTo(newModel);
    }

    @Test
    @DisplayName("Should allow setting openai model")
    public void shouldAllowSettingOpenaiModel() {
        // Given
        String newModel = "gpt-3.5-turbo";
        
        // When
        llmProperties.setOpenaiModel(newModel);
        
        // Then
        assertThat(llmProperties.getOpenaiModel()).isEqualTo(newModel);
    }

    @Test
    @DisplayName("Should handle null provider")
    public void shouldHandleNullProvider() {
        // When
        llmProperties.setProvider(null);
        
        // Then
        assertThat(llmProperties.getProvider()).isNull();
    }

    @Test
    @DisplayName("Should handle null temperature")
    public void shouldHandleNullTemperature() {
        // When
        llmProperties.setTemperature(null);
        
        // Then
        assertThat(llmProperties.getTemperature()).isNull();
    }

    @Test
    @DisplayName("Should handle null ollama model")
    public void shouldHandleNullOllamaModel() {
        // When
        llmProperties.setOllamaModel(null);
        
        // Then
        assertThat(llmProperties.getOllamaModel()).isNull();
    }

    @Test
    @DisplayName("Should handle null openai model")
    public void shouldHandleNullOpenaiModel() {
        // When
        llmProperties.setOpenaiModel(null);
        
        // Then
        assertThat(llmProperties.getOpenaiModel()).isNull();
    }

    @Test
    @DisplayName("Should handle empty string values")
    public void shouldHandleEmptyStringValues() {
        // When
        llmProperties.setProvider("");
        llmProperties.setOllamaModel("");
        llmProperties.setOpenaiModel("");
        
        // Then
        assertThat(llmProperties.getProvider()).isEmpty();
        assertThat(llmProperties.getOllamaModel()).isEmpty();
        assertThat(llmProperties.getOpenaiModel()).isEmpty();
    }

    @Test
    @DisplayName("Should handle extreme temperature values")
    public void shouldHandleExtremeTemperatureValues() {
        // Given/When/Then
        llmProperties.setTemperature(0.0);
        assertThat(llmProperties.getTemperature()).isEqualTo(0.0);
        
        llmProperties.setTemperature(1.0);
        assertThat(llmProperties.getTemperature()).isEqualTo(1.0);
        
        llmProperties.setTemperature(2.0);
        assertThat(llmProperties.getTemperature()).isEqualTo(2.0);
        
        llmProperties.setTemperature(-0.1);
        assertThat(llmProperties.getTemperature()).isEqualTo(-0.1);
    }

    @Test
    @DisplayName("Should handle special double values for temperature")
    public void shouldHandleSpecialDoubleValuesForTemperature() {
        // Given/When/Then
        llmProperties.setTemperature(Double.NaN);
        assertThat(Double.isNaN(llmProperties.getTemperature())).isTrue();
        
        llmProperties.setTemperature(Double.POSITIVE_INFINITY);
        assertThat(Double.isInfinite(llmProperties.getTemperature())).isTrue();
        assertThat(llmProperties.getTemperature() > 0).isTrue();
        
        llmProperties.setTemperature(Double.NEGATIVE_INFINITY);
        assertThat(Double.isInfinite(llmProperties.getTemperature())).isTrue();
        assertThat(llmProperties.getTemperature() < 0).isTrue();
    }

    @Test
    @DisplayName("Should support lombok generated methods")
    public void shouldSupportLombokGeneratedMethods() {
        // Given
        LLMProperties properties1 = new LLMProperties();
        LLMProperties properties2 = new LLMProperties();
        
        // When/Then
        assertThat(properties1).isEqualTo(properties2);
        assertThat(properties1.hashCode()).isEqualTo(properties2.hashCode());
        assertThat(properties1.toString()).isNotNull();
        assertThat(properties1.toString()).contains("LLMProperties");
    }

    @Test
    @DisplayName("Should support different instances with different values")
    public void shouldSupportDifferentInstancesWithDifferentValues() {
        // Given
        LLMProperties properties1 = new LLMProperties();
        LLMProperties properties2 = new LLMProperties();
        
        properties1.setProvider("openai");
        properties1.setTemperature(0.8);
        
        properties2.setProvider("ollama");
        properties2.setTemperature(0.3);
        
        // When/Then
        assertThat(properties1).isNotEqualTo(properties2);
        assertThat(properties1.getProvider()).isNotEqualTo(properties2.getProvider());
        assertThat(properties1.getTemperature()).isNotEqualTo(properties2.getTemperature());
    }
}