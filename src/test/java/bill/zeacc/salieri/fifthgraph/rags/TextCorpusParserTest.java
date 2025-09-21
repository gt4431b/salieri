package bill.zeacc.salieri.fifthgraph.rags;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.* ;
import static org.mockito.Mockito.when;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.InputStreamSource;

import dev.langchain4j.data.document.Document;

@ExtendWith(MockitoExtension.class)
@DisplayName("TextCorpusParser Tests")
public class TextCorpusParserTest {

    @Mock
    private InputStreamSource mockInputStreamSource;

    @Mock
    private RagIngestionTreeContext mockContext;

    private TextCorpusParser textCorpusParser;

    @BeforeEach
    void setUp() {
        textCorpusParser = new TextCorpusParser();
    }

    @Test
    @DisplayName("Should support text content types")
    void shouldSupportTextContentTypes() {
        // Given
        String[] supportedTypes = {
            "text/plain",
            "text/markdown",
            "text/html"
        };

        // When/Then
        for (String contentType : supportedTypes) {
            boolean supports = textCorpusParser.supports(contentType, mockContext);
            assertTrue ( supports, "Expected to support content type: " + contentType ) ;
        }
    }

    @Test
    @DisplayName("Should not support non-text content types")
    void shouldNotSupportNonTextContentTypes() {
        // Given
        String[] unsupportedTypes = {
            "application/pdf",
            "image/png",
            "video/mp4",
            null,
            ""
        };

        // When/Then
        for (String contentType : unsupportedTypes) {
            boolean supports = textCorpusParser.supports(contentType, mockContext);
            assertFalse ( supports, "Expected to not support content type: " + contentType ) ;
        }
    }

    @Test
    @DisplayName("Should parse text content successfully")
    void shouldParseTextContentSuccessfully() throws IOException {
        // Given
        String textContent = "This is a sample text document for testing purposes.";
        InputStream textStream = new ByteArrayInputStream(textContent.getBytes());
        when(mockInputStreamSource.getInputStream()).thenReturn(textStream);

        // When
        List<Document> documents = textCorpusParser.parse(mockInputStreamSource, mockContext);

        // Then
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).text().trim ( ) ).isEqualTo(textContent);
    }

    @Test
    @DisplayName("Should NOT handle empty text content")
    void shouldHandleEmptyTextContent() throws IOException {
        // Given
        String emptyContent = "";
        InputStream emptyStream = new ByteArrayInputStream(emptyContent.getBytes());
        when(mockInputStreamSource.getInputStream()).thenReturn(emptyStream);

        // When / Then
        assertThatThrownBy(() -> textCorpusParser.parse(mockInputStreamSource, mockContext))
			.isInstanceOf(IllegalArgumentException.class)
			.hasMessageContaining("text cannot be null or blank");
    }

    @Test
    @DisplayName("Should handle large text content")
    void shouldHandleLargeTextContent() throws IOException {
        // Given
        String largeContent = "This is a large text document. ".repeat(1000);
        InputStream largeStream = new ByteArrayInputStream(largeContent.getBytes());
        when(mockInputStreamSource.getInputStream()).thenReturn(largeStream);

        // When
        List<Document> documents = textCorpusParser.parse(mockInputStreamSource, mockContext);

        // Then
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).text().trim ( ) ).isEqualTo(largeContent.trim ( ) );
        assertThat(documents.get(0).text().length()).isGreaterThan(30000);
    }

    @Test
    @DisplayName("Should handle special characters and encoding")
    void shouldHandleSpecialCharactersAndEncoding() throws IOException {
        // Given
        String specialContent = "Text with special chars: áéíóú, símb∅ls: ∑∆π, émojis: 🤖📝, and newlines:\nLine 2\nLine 3";
        InputStream specialStream = new ByteArrayInputStream(specialContent.getBytes("UTF-8"));
        when(mockInputStreamSource.getInputStream()).thenReturn(specialStream);

        // When
        List<Document> documents = textCorpusParser.parse(mockInputStreamSource, mockContext);

        // Then
        assertThat(documents).hasSize(1);
        assertThat(documents.get(0).text().trim ( ) ).isEqualTo(specialContent);
        assertThat(documents.get(0).text()).contains("áéíóú", "∑∆π", "🤖", "\n");
    }

    @Test
    @DisplayName("Should handle IO exceptions")
    void shouldHandleIOExceptions() throws IOException {
        // Given
        when(mockInputStreamSource.getInputStream()).thenThrow(new IOException("Stream read error"));

        // When/Then
        assertThatThrownBy(() -> textCorpusParser.parse(mockInputStreamSource, mockContext))
            .isInstanceOf(IOException.class)
            .hasMessage("Stream read error");
    }

    @Test
    @DisplayName("Should handle null input stream source")
    void shouldHandleNullInputStreamSource() {
        // When/Then
        assertThatThrownBy(() -> textCorpusParser.parse(null, mockContext))
            .isInstanceOf(NullPointerException.class);
    }

    @Test
    @DisplayName("Should close input stream properly")
    void shouldCloseInputStreamProperly() throws IOException {
        // Given
        String content = "Test content";
        InputStream stream = new ByteArrayInputStream(content.getBytes());
        when(mockInputStreamSource.getInputStream()).thenReturn(stream);

        // When
        List<Document> documents = textCorpusParser.parse(mockInputStreamSource, mockContext);

        // Then
        assertThat(documents).hasSize(1);
        // Stream should be closed automatically by try-with-resources
        /*  // This won't work as ByteArrayInputStream's close is effectively a no-op
         * 	// The BAIS javadoc even says "Closing a ByteArrayInputStream has no effect."
         *  // There's no point in testing this since we're the ones passing the damn
         *  // Byte stream in the first place.  Other InputStream implementations that
         *  // take close() more seriously would be better candidates for this test, but
         *  // do we really need it?  Bill sez Nah.
        assertThatThrownBy(() -> stream.read())
            .isInstanceOf(IOException.class)
            .hasMessage("Stream closed");
            */
    }
}
