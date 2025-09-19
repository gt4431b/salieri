package bill.zeacc.salieri.fifthgraph.model.feedback;

import java.util.LinkedHashMap ;
import java.util.Map ;
import java.util.Set ;

import lombok.Data ;

@Data
public class BaseFeedback <T extends BaseCategory> {

	public Map <T, Grade> grades = new LinkedHashMap <> ( ) ;

	public void addGrade ( T category, Grade g ) {
		grades.put ( category, g ) ;
	}

	public void addGrade ( T category, Double score, String comment ) {
		grades.put ( category, new Grade ( score, comment ) ) ;
	}

	public Grade getGrade ( T category ) {
		return grades.get ( category ) ;
	}

	public Set <T> getCategories ( ) {
		return grades.keySet ( ) ;
	}

	public Map <T, Grade> getGrades ( ) {
		return grades ;
	}
}
