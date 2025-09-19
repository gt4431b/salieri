package bill.zeacc.salieri.fifthgraph.config;

import org.springframework.boot.context.properties.ConfigurationProperties ;

import lombok.Data ;

@ConfigurationProperties(prefix = "app.llm")
@Data
public class LLMProperties {
    private String provider = "ollama";
    private Double temperature = 0.3;
//  private String ollamaModel = "llama3-groq-tool-use:latest";
//    private String ollamaModel = "nexusraven:13b-q6_K";
	private String ollamaModel = "ikiru/Dolphin-Mistral-24B-Venice-Edition:latest";
    private String openaiModel = "gpt-4";
}
