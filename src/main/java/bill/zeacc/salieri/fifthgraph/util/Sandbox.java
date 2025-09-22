package bill.zeacc.salieri.fifthgraph.util ;

import java.io.IOException ;
import java.io.UncheckedIOException ;
import java.nio.file.Files ;
import java.nio.file.Path ;
import java.util.ArrayList ;
import java.util.List ;
import java.util.Map ;
import java.util.TreeMap ;
import java.util.stream.Collectors ;

import org.springframework.beans.factory.annotation.Autowired ;
import org.springframework.stereotype.Component ;

import com.fasterxml.jackson.core.JsonProcessingException ;
import com.fasterxml.jackson.core.type.TypeReference ;
import com.fasterxml.jackson.databind.ObjectMapper ;

import bill.zeacc.salieri.fifthgraph.model.codeir.Codebase ;

@Component
public class Sandbox {

	@Autowired
	private ObjectMapper om ;

	public String getFromSandbox ( Codebase cb, String handle ) {
		Path p = Path.of ( cb.getSandboxRootPath ( ), handle ) ;
		if ( ! p.toFile ( ).exists ( ) ) {
			if ( handle.equals ( ".__directory.json" ) ) {
				return null ;
			}
			throw new RuntimeException ( "File not found in sandbox: " + p ) ;
		}
		try {
			return Files.readString ( p ) ;
		} catch ( IOException e ) {
			throw new UncheckedIOException ( e ) ;
		}
	}

	public void saveToSandbox ( Codebase cb, String handle, String category, String contents, String comment ) {
		writeFile ( handle, cb.getSandboxRootPath ( ) + ( category == null ? "" : "/" + category ), contents ) ;
		DirectoryListing dir = getFromSandbox ( cb, ".__directory.json", DirectoryListing.class ) ;
		if ( dir == null ) {
			dir = new DirectoryListing ( new ArrayList <> ( ) ) ;
			dir.files ( ).add ( new FileListing ( "", ".__directory.json", "Sandbox contents" ) ) ;
		}
		Map <String, FileListing> ls = dir.files ( ).stream ( ).collect ( Collectors.toMap ( fl -> fl.category ( ) + "/" + fl.handle ( ), fl -> fl, ( a, b ) -> b, ( ) -> new TreeMap <> ( ) ) ) ;
		FileListing thisFile = new FileListing ( category, handle, comment ) ;
		ls.put ( thisFile.name ( ), thisFile ) ;
		dir.files ( ).clear ( ) ;
		dir.files.addAll ( ls.values ( ) ) ;
		String strDir ;
		try {
			strDir = getObjectMapper().writeValueAsString ( dir ) ;
			writeFile ( ".__directory.json", cb.getSandboxRootPath ( ), strDir ) ;
		} catch ( JsonProcessingException e ) {
			throw new RuntimeException ( e ) ;
		}
	}

	private void writeFile ( String handle, String s, String c ) {
		Path p = Path.of ( s, handle ) ;
		p.toFile ( ).getParentFile ( ).mkdirs ( ) ;
		p.toFile ( ).delete ( ) ;
		try {
			p.toFile ( ).createNewFile ( ) ;
			Files.writeString ( p, c ) ;
		} catch ( IOException e ) {
			throw new UncheckedIOException ( e ) ;
		}
	}

	public void saveObjectToSandbox ( Codebase cb, String category, String handle, Object contents, String comment ) {
		try {
			saveToSandbox ( cb, handle, category, getObjectMapper().writeValueAsString ( contents ), comment ) ;
		} catch ( JsonProcessingException e ) {
			throw new RuntimeException ( "Failed to serialize file contents: " + handle, e ) ;
		}
	}

	public <T> T getFromSandbox ( Codebase cb, String handle, Class<T> clazz ) {
		try {
			String v = getFromSandbox ( cb, handle ) ;
			if ( v == null ) {
				return null ;
			} else {
				return getObjectMapper().readValue ( v, clazz ) ;
			}
		} catch ( JsonProcessingException e ) {
			throw new RuntimeException ( "Failed to parse file contents: " + handle, e ) ;
		}
	}

	public <T> T getFromSandbox ( Codebase cb, String handle, TypeReference <T> type ) {
		try {
			String v = getFromSandbox ( cb, handle ) ;
			if ( v == null ) {
				return null ;
			} else {
				return getObjectMapper().readValue ( v, type ) ;
			}
		} catch ( JsonProcessingException e ) {
			throw new RuntimeException ( "Failed to parse file contents: " + handle, e ) ;
		}
	}

	public ObjectMapper getObjectMapper ( ) {
		return om;
	}

	public void setObjectMapper ( ObjectMapper om ) {
		this.om = om;
	}

	record DirectoryListing ( List <FileListing> files ) { ; }
	record FileListing ( String category, String handle, String comment ) implements Comparable <FileListing> {

		@Override
		public int compareTo ( FileListing that ) {
			String thisName = this.name ( ) ;
			String thatName = that.name ( ) ;
			return thisName.compareTo ( thatName ) ;
		}

		private String name ( ) {
			return category + "/" + handle ;
		}
	}
}
