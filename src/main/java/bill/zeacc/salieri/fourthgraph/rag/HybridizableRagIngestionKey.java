package bill.zeacc.salieri.fourthgraph.rag;


public enum HybridizableRagIngestionKey implements RagIngestionKey {
	DOC_ID, BATCH_ID, FILE_ID, DOC_POSITION ;

	public String nameSpace ( ) {
		return "HybridizableRagIngestionKey" ;
	}
}
