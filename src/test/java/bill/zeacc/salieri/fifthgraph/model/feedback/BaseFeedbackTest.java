package bill.zeacc.salieri.fifthgraph.model.feedback;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@DisplayName("BaseFeedback Tests")
public class BaseFeedbackTest {

    private BaseFeedback<TestCategory> feedback;
    private TestCategory category1;
    private TestCategory category2;
    private TestCategory category3;

    @BeforeEach
    protected void setUp() {
        feedback = new BaseFeedback<>();
        category1 = new TestCategory("Quality", "code");
        category2 = new TestCategory("Performance", "runtime");
        category3 = new TestCategory("Documentation", "docs");
    }

    @Test
    @DisplayName("Should initialize with empty grades map")
    public void shouldInitializeWithEmptyGradesMap() {
        // Given/When
        BaseFeedback<TestCategory> newFeedback = new BaseFeedback<>();
        
        // Then
        assertThat(newFeedback.getGrades()).isEmpty();
        assertThat(newFeedback.getCategories()).isEmpty();
    }

    @Test
    @DisplayName("Should add grade with Grade object")
    public void shouldAddGradeWithGradeObject() {
        // Given
        Grade grade = new Grade(85.5, "Good work");
        
        // When
        feedback.addGrade(category1, grade);
        
        // Then
        assertThat(feedback.getGrade(category1)).isEqualTo(grade);
        assertThat(feedback.getGrades()).hasSize(1);
        assertThat(feedback.getCategories()).contains(category1);
    }

    @Test
    @DisplayName("Should add grade with score and comment")
    public void shouldAddGradeWithScoreAndComment() {
        // Given
        double score = 92.0;
        String comment = "Excellent performance";
        
        // When
        feedback.addGrade(category2, score, comment);
        
        // Then
        Grade retrievedGrade = feedback.getGrade(category2);
        assertThat(retrievedGrade.score()).isEqualTo(score);
        assertThat(retrievedGrade.comment()).isEqualTo(comment);
        assertThat(feedback.getGrades()).hasSize(1);
    }

    @Test
    @DisplayName("Should add multiple grades")
    public void shouldAddMultipleGrades() {
        // Given
        Grade grade1 = new Grade(85.5, "Good work");
        Grade grade2 = new Grade(92.0, "Excellent");
        
        // When
        feedback.addGrade(category1, grade1);
        feedback.addGrade(category2, 78.5, "Needs improvement");
        feedback.addGrade(category3, grade2);
        
        // Then
        assertThat(feedback.getGrades()).hasSize(3);
        assertThat(feedback.getGrade(category1)).isEqualTo(grade1);
        assertThat(feedback.getGrade(category2).score()).isEqualTo(78.5);
        assertThat(feedback.getGrade(category2).comment()).isEqualTo("Needs improvement");
        assertThat(feedback.getGrade(category3)).isEqualTo(grade2);
    }

    @Test
    @DisplayName("Should overwrite existing grade for same category")
    public void shouldOverwriteExistingGradeForSameCategory() {
        // Given
        Grade originalGrade = new Grade(75.0, "Original comment");
        Grade newGrade = new Grade(90.0, "Updated comment");
        feedback.addGrade(category1, originalGrade);
        
        // When
        feedback.addGrade(category1, newGrade);
        
        // Then
        assertThat(feedback.getGrades()).hasSize(1);
        assertThat(feedback.getGrade(category1)).isEqualTo(newGrade);
        assertThat(feedback.getGrade(category1)).isNotEqualTo(originalGrade);
    }

    @Test
    @DisplayName("Should return null for non-existent category")
    public void shouldReturnNullForNonExistentCategory() {
        // Given
        TestCategory nonExistentCategory = new TestCategory("NonExistent", "test");
        
        // When
        Grade result = feedback.getGrade(nonExistentCategory);
        
        // Then
        assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return all categories")
    public void shouldReturnAllCategories() {
        // Given
        feedback.addGrade(category1, 85.0, "Good");
        feedback.addGrade(category2, 90.0, "Great");
        feedback.addGrade(category3, 78.0, "Okay");
        
        // When
        Set<TestCategory> categories = feedback.getCategories();
        
        // Then
        assertThat(categories).hasSize(3);
        assertThat(categories).contains(category1, category2, category3);
    }

    @Test
    @DisplayName("Should return grades map")
    public void shouldReturnGradesMap() {
        // Given
        Grade grade1 = new Grade(85.0, "Good");
        Grade grade2 = new Grade(90.0, "Great");
        feedback.addGrade(category1, grade1);
        feedback.addGrade(category2, grade2);
        
        // When
        Map<TestCategory, Grade> grades = feedback.getGrades();
        
        // Then
        assertThat(grades).hasSize(2);
        assertThat(grades.get(category1)).isEqualTo(grade1);
        assertThat(grades.get(category2)).isEqualTo(grade2);
    }

    @Test
    @DisplayName("Should handle null comment when adding grade")
    public void shouldHandleNullCommentWhenAddingGrade() {
        // Given
        double score = 88.0;
        String comment = null;
        
        // When
        feedback.addGrade(category1, score, comment);
        
        // Then
        Grade retrievedGrade = feedback.getGrade(category1);
        assertThat(retrievedGrade.score()).isEqualTo(score);
        assertThat(retrievedGrade.comment()).isNull();
    }

    @Test
    @DisplayName("Should handle empty comment when adding grade")
    public void shouldHandleEmptyCommentWhenAddingGrade() {
        // Given
        double score = 75.5;
        String comment = "";
        
        // When
        feedback.addGrade(category1, score, comment);
        
        // Then
        Grade retrievedGrade = feedback.getGrade(category1);
        assertThat(retrievedGrade.score()).isEqualTo(score);
        assertThat(retrievedGrade.comment()).isEmpty();
    }

    @Test
    @DisplayName("Should handle special double values")
    public void shouldHandleSpecialDoubleValues() {
        // Given/When
        feedback.addGrade(category1, Double.NaN, "Not a number");
        feedback.addGrade(category2, Double.POSITIVE_INFINITY, "Infinite");
        feedback.addGrade(category3, Double.NEGATIVE_INFINITY, "Negative infinite");
        
        // Then
        assertThat(Double.isNaN(feedback.getGrade(category1).score())).isTrue();
        assertThat(Double.isInfinite(feedback.getGrade(category2).score())).isTrue();
        assertThat(feedback.getGrade(category2).score() > 0).isTrue();
        assertThat(Double.isInfinite(feedback.getGrade(category3).score())).isTrue();
        assertThat(feedback.getGrade(category3).score() < 0).isTrue();
    }

    @Test
    @DisplayName("Should maintain insertion order in LinkedHashMap")
    public void shouldMaintainInsertionOrderInLinkedHashMap() {
        // Given
        feedback.addGrade(category3, 70.0, "Third");
        feedback.addGrade(category1, 80.0, "First");
        feedback.addGrade(category2, 90.0, "Second");
        
        // When
        Set<TestCategory> categories = feedback.getCategories();
        TestCategory[] categoryArray = categories.toArray(new TestCategory[0]);
        
        // Then
        assertThat(categoryArray[0]).isEqualTo(category3);
        assertThat(categoryArray[1]).isEqualTo(category1);
        assertThat(categoryArray[2]).isEqualTo(category2);
    }

    @Test
    @DisplayName("Should support lombok generated methods")
    public void shouldSupportLombokGeneratedMethods() {
        // Given
        BaseFeedback<TestCategory> feedback1 = new BaseFeedback<>();
        BaseFeedback<TestCategory> feedback2 = new BaseFeedback<>();
        feedback1.addGrade(category1, 85.0, "Good");
        feedback2.addGrade(category1, 85.0, "Good");
        
        // When/Then
        assertThat(feedback1).isEqualTo(feedback2);
        assertThat(feedback1.hashCode()).isEqualTo(feedback2.hashCode());
        assertThat(feedback1.toString()).isNotNull();
        assertThat(feedback1.toString()).contains("BaseFeedback");
    }

    // Test helper class that implements BaseCategory
    private static class TestCategory implements BaseCategory {
        private final String name;
        private final String namespace;

        public TestCategory(String name, String namespace) {
            this.name = name;
            this.namespace = namespace;
        }

        @Override
        public String name() {
            return name;
        }

        @Override
        public String namespace() {
            return namespace;
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (obj == null || getClass() != obj.getClass()) return false;
            TestCategory that = (TestCategory) obj;
            return java.util.Objects.equals(name, that.name) && 
                   java.util.Objects.equals(namespace, that.namespace);
        }

        @Override
        public int hashCode() {
            return java.util.Objects.hash(name, namespace);
        }

        @Override
        public String toString() {
            return "TestCategory{name='" + name + "', namespace='" + namespace + "'}";
        }
    }
}