package bill.zeacc.salieri.fifthgraph.rags;


public enum HybridizableRagIngestionKey implements RagIngestionKey {
	DOC_ID, BATCH_ID, FILE_ID, DOC_POSITION, META, SESSION_ID, TENANT, CONTENT_TYPE, COLLECTION, PROVENANCE, BATCH_ROOT, MAX_INPUT_TOKEN_COUNT, RESERVE_PERCENTAGE, REQUEST_ID,
	FILE_NAME, FILE_META ;

	public String nameSpace ( ) {
		return "HybridizableRagIngestionKey" ;
	}
}
