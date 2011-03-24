package edu.cornell.mannlib.ingest.processors;

import edu.cornell.mannlib.ingest.interfaces.StringProcessor;

public class LeadingOrdinalNumberRemover implements StringProcessor {

	/**
	 * Turns strings of form "3. Egypt" into "Egypt"
	 */
	public String process(String in) {
		if (in != null) {
			return in.replaceAll("^[0-9]+\\.\\ ","");
		}
		return null;
	}

}
