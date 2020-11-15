package ca.mcgill.ecse.flexibook.persistence;

import ca.mcgill.ecse.flexibook.model.FlexiBook;

public class FlexiBookPersistence {

	private static String filename = "data.flexibook";
	
	public static void setFilename(String filename) {
		FlexiBookPersistence.filename = filename;
	}
	
	public static void save(FlexiBook flexibook) {
	    PersistenceObjectStream.serialize(flexibook);
	}

	public static FlexiBook load() {
	    PersistenceObjectStream.setFilename(filename);
	    FlexiBook flexibook = (FlexiBook) PersistenceObjectStream.deserialize();
        
	    if (flexibook == null) {
	        flexibook = new FlexiBook();
	    }
	    else {
			flexibook.reinitialize();
		}
	    return flexibook;
	}
}
