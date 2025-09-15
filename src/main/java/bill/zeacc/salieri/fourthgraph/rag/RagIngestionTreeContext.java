package bill.zeacc.salieri.fourthgraph.rag ;

import java.io.File ;
import java.nio.file.Path ;
import java.util.HashMap ;
import java.util.Map ;

import dev.langchain4j.data.document.Document ;
import dev.langchain4j.data.segment.TextSegment ;

public class RagIngestionTreeContext {

	private enum RagIngestionLevel {
		ROOT, BATCH, FILE, DOCUMENT, SEGMENT ;
	}

	private enum RagIngestionKeyInternal implements RagIngestionKey {
		PARENT, LEVEL ;

		public String nameSpace ( ) {
			return "RagIngestionContextInternal" ;
		}
	}

	private static final Integer DEFAULT_MAX_DOCUMENT_BATCH_SIZE = 200 ;

	private File currentFile ;
	private Document doc ;
	private TextSegment segment ;
	private Map <RagIngestionKey, Object> currentProperties = new HashMap <> ( ) ;
	private Path root ;
	private Integer maxDocumentBatchSize = DEFAULT_MAX_DOCUMENT_BATCH_SIZE ;

	public RagIngestionTreeContext ( ) {
		currentProperties.put ( RagIngestionKeyInternal.LEVEL, RagIngestionLevel.ROOT ) ;
	}

	public <T> T getProperty ( RagIngestionKey key, Class <T> type ) {
		return doGetProperty ( key, type, currentProperties ) ;
	}

	public Integer getMaxDocumentBatchSize ( ) {
		return maxDocumentBatchSize ;
	}

	public void setMaxDocumentBatchSize ( Integer maxDocumentBatchSize ) {
		this.maxDocumentBatchSize = maxDocumentBatchSize ;
	}

	@SuppressWarnings ( "unchecked" )
	private <T> T doGetProperty ( RagIngestionKey key, Class <T> type, Map <RagIngestionKey, Object> props ) {
		Object value = props.get ( key ) ;
		if ( value == null ) {
			RagIngestionLevel level = ( RagIngestionLevel ) props.get ( RagIngestionKeyInternal.LEVEL ) ;
			if ( level == RagIngestionLevel.ROOT ) {
				return null ;
			} else {
				Map <RagIngestionKey, Object> parentProps = ( Map <RagIngestionKey, Object> ) props.get ( RagIngestionKeyInternal.PARENT ) ;
				if ( parentProps == null ) {
					throw new IllegalStateException ( "No parent properties found at level " + level ) ;
				}
				return doGetProperty ( key, type, parentProps ) ;
			}
		}
		if ( type.isInstance ( value ) ) {
			return type.cast ( value ) ;
		}
		throw new IllegalArgumentException ( "Property " + key + " is not of type " + type.getName ( ) ) ;
	}

	public void setProperty ( RagIngestionKey key, Object value ) {
		currentProperties.put ( key, value ) ;
	}

	public Object getCurrentFile ( ) {
		return this.currentFile ;
	}

	public void setCurrentFile ( Object file ) {
		this.currentFile = pushPopProps ( file, RagIngestionLevel.FILE ) ;
	}

	public Document getCurrentDocument ( ) {
		return this.doc ;
	}

	public void setCurrentDocument ( Document doc ) {
		this.doc = pushPopProps ( doc, RagIngestionLevel.DOCUMENT ) ;
	}

	public TextSegment getCurrentSegment ( ) {
		return this.segment ;
	}

	public void setCurrentSegment ( TextSegment seg ) {
		this.segment = pushPopProps ( seg, RagIngestionLevel.SEGMENT ) ;
	}

	public Object getBatchRoot ( ) {
		return this.root ;
	}

	public void setBatchRoot ( Object root ) {
		this.root = pushPopProps ( root, RagIngestionLevel.BATCH ) ;
	}

	@SuppressWarnings ( "unchecked" )
	private <T> T pushPopProps ( Object newVal, RagIngestionLevel nextLevel ) {
		if ( newVal == null ) {
			RagIngestionLevel parentLevel = RagIngestionLevel.values ( ) [ nextLevel.ordinal ( ) - 1 ] ;
			while ( currentProperties.get ( RagIngestionKeyInternal.LEVEL ) != parentLevel ) {
				currentProperties = ( Map <RagIngestionKey, Object> ) currentProperties.get ( RagIngestionKeyInternal.PARENT ) ;
			}
		} else {
			Map <RagIngestionKey, Object> nextProps = new HashMap <> ( ) ;
			nextProps.put ( RagIngestionKeyInternal.PARENT, currentProperties ) ;
			nextProps.put ( RagIngestionKeyInternal.LEVEL, nextLevel ) ;
			currentProperties = nextProps ;
		}
		return (T) newVal ;
	}
}
