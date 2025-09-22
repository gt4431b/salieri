package bill.zeacc.salieri.fifthgraph.model.feedback;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals ;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("Grade Tests")
public class GradeTest {

    @Test
    @DisplayName("Should create grade with score and comment")
    public void shouldCreateGradeWithScoreAndComment() {
        // Given
        double score = 85.5;
        String comment = "Good work";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.comment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Should create grade with zero score")
    public void shouldCreateGradeWithZeroScore() {
        // Given
        double score = 0.0;
        String comment = "Needs improvement";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.comment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Should create grade with negative score")
    public void shouldCreateGradeWithNegativeScore() {
        // Given
        double score = -10.5;
        String comment = "Below expectations";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.comment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Should create grade with maximum double value")
    public void shouldCreateGradeWithMaximumDoubleValue() {
        // Given
        double score = Double.MAX_VALUE;
        String comment = "Exceptional";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.comment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Should create grade with null comment")
    public void shouldCreateGradeWithNullComment() {
        // Given
        double score = 75.0;
        String comment = null;
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.comment()).isNull();
    }

    @Test
    @DisplayName("Should create grade with empty comment")
    public void shouldCreateGradeWithEmptyComment() {
        // Given
        double score = 90.0;
        String comment = "";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.comment()).isEmpty();
    }

    @Test
    @DisplayName("Should handle special double values - positive infinity")
    public void shouldHandlePositiveInfinity() {
        // Given
        double score = Double.POSITIVE_INFINITY;
        String comment = "Infinite excellence";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.score()).isPositive();
        assertEquals(Double.POSITIVE_INFINITY, grade.score());
        assertThat(grade.comment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Should handle special double values - negative infinity")
    public void shouldHandleNegativeInfinity() {
        // Given
        double score = Double.NEGATIVE_INFINITY;
        String comment = "Infinitely bad";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
        assertThat(grade.score()).isEqualTo(score);
        assertThat(grade.score()).isNegative();
        assertEquals(Double.NEGATIVE_INFINITY, grade.score());
        assertThat(grade.comment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Should handle NaN values")
    public void shouldHandleNaNValues() {
        // Given
        double score = Double.NaN;
        String comment = "Not a number";
        
        // When
        Grade grade = new Grade(score, comment);
        
        // Then
//        assertThat(grade.score()).isEqualTo(score); // By IEEE 754 spec this isn't possible.
        assertThat(grade.score()).isNaN();
        assertThat(grade.comment()).isEqualTo(comment);
    }

    @Test
    @DisplayName("Should support equality comparison")
    public void shouldSupportEqualityComparison() {
        // Given
        Grade grade1 = new Grade(85.5, "Good work");
        Grade grade2 = new Grade(85.5, "Good work");
        Grade grade3 = new Grade(90.0, "Good work");
        Grade grade4 = new Grade(85.5, "Excellent work");
        
        // When/Then
        assertThat(grade1).isEqualTo(grade2);
        assertThat(grade1).isNotEqualTo(grade3);
        assertThat(grade1).isNotEqualTo(grade4);
        assertThat(grade1.hashCode()).isEqualTo(grade2.hashCode());
    }

    @Test
    @DisplayName("Should support toString representation")
    public void shouldSupportToStringRepresentation() {
        // Given
        Grade grade = new Grade(85.5, "Good work");
        
        // When
        String toString = grade.toString();
        
        // Then
        assertThat(toString).contains("85.5");
        assertThat(toString).contains("Good work");
        assertThat(toString).contains("Grade");
    }
}