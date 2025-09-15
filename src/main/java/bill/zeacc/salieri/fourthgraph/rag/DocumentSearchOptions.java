package bill.zeacc.salieri.fourthgraph.rag;

import java.util.HashSet ;
import java.util.Map ;
import java.util.Set ;

import org.springframework.ai.vectorstore.filter.Filter.Expression ;

import lombok.Builder ;
import lombok.Data ;

@Data
@Builder
public class DocumentSearchOptions {
	public static enum SearchType implements RagPullerService.ResultGroup {
		FULL_TEXT,
		VECTOR ;

		public String namespace ( ) {
			return "hybrid_rag_pull_search_type" ;
		}
	}

	public static enum Operator {
		COSINE ( "<=>" ),
		L2 ( "<->" ),
		IP ( "<#>" ) ;

		private final String operator ;
		private Operator ( String operator ) {
			this.operator = operator ;
		}
		public String operator ( ) {
			return operator ;
		}
	}

	private final String collection ;
	private final String tenant ;
	private final String docId ; // for searching within a specific document
	@Builder.Default
	private Operator preferredOperator = Operator.COSINE ;
	@Builder.Default
	private Map <SearchType, Integer> searchTopK = Map.of ( SearchType.VECTOR, 5 ) ;
	@Builder.Default
	private Map <SearchType, Integer> expandedSearchRadius = Map.of ( SearchType.VECTOR, 2, SearchType.FULL_TEXT, 1 ) ;
	@Builder.Default
	private Integer topK = 5 ;
	@Builder.Default
	private Integer defaultSearchRadius = 2 ;
	@Builder.Default
	private boolean doFullTextSearch = true ;
	@Builder.Default
	private boolean doVectorSearch = true ;
	@Builder.Default
	private String searchLanguage = "english" ;
	@Builder.Default
	private Double defaultFullTextScore = 1.0 ;
	@Builder.Default
	private Double similarityThreshold = 1.0 ;
	private String filterExpressionString ;
	private Expression filterExpression ;
	@Builder.Default
	private boolean doExpandedSearch = false ;
	@Builder.Default
	private Set <SearchType> resultGroupsForExpandedSearch = new HashSet <> ( Set.of ( SearchType.FULL_TEXT, SearchType.VECTOR ) ) ;

	public boolean doFullTextSearch ( ) {
		return doFullTextSearch ;
	}

	public boolean doSearchVectorStore ( ) {
		return doVectorSearch ;
	}

	public boolean doExpandedSearch ( ) {
		return doExpandedSearch ;
	}
}
