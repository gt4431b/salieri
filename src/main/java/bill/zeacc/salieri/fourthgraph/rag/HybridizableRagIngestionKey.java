package bill.zeacc.salieri.fourthgraph.rag;


public enum HybridizableRagIngestionKey implements RagIngestionKey {
	DOC_ID, BATCH_ID, FILE_ID, DOC_POSITION, META, SESSION_ID, TENANT, CONTENT_TYPE, COLLECTION, PROVENANCE, BATCH_ROOT ;

	public String nameSpace ( ) {
		return "HybridizableRagIngestionKey" ;
	}
}
