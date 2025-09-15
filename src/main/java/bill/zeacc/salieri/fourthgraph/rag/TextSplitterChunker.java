package bill.zeacc.salieri.fourthgraph.rag ;

import dev.langchain4j.data.document.Document ;
import dev.langchain4j.data.document.DocumentSplitter ;
import dev.langchain4j.data.segment.TextSegment ;
import dev.langchain4j.data.document.splitter.DocumentByParagraphSplitter ;
import dev.langchain4j.data.document.splitter.DocumentBySentenceSplitter ;

import java.util.List ;

public class TextSplitterChunker implements TextChunker {

	private final DocumentSplitter splitter ;

	public TextSplitterChunker ( int maxCharacters, int maxOverlap ) {
		DocumentSplitter subsplitter = new DocumentBySentenceSplitter ( maxCharacters, maxOverlap ) ;
		this.splitter = new DocumentByParagraphSplitter ( maxCharacters, maxOverlap, subsplitter ) ;
	}

	@Override
	public List <TextSegment> split ( Document document, RagIngestionTreeContext ctx ) {
		return splitter.split ( document ) ;
	}
}
