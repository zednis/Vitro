package edu.cornell.mannlib.vitro.biosis.beans;
//package biosis;

/**
 * @version 1 2004-06-04
 * @author Jon Corson-Rikert
 *
 */
//import java.net.*;
//import java.io.*;
import java.util.*;


public class PersonToMatch implements Comparable {
	private int    entityId;
	private String originalname;
	private String firstname;
	private String middlename;
	private String lastname;
	private String suffix;

	// note that Jr and other suffixes end up at the end of the middle name -- as they do in tokens coming from Biosis
	public PersonToMatch() {
		entityId  = 0;
		originalname=null;
		firstname = null;
		middlename= null;
		lastname  = null;
	}

	public PersonToMatch( int entity_id, String original_name, String first_name, String middle_name, String last_name ) {
		entityId = entity_id;
		originalname=original_name;
		firstname  = first_name;
		middlename = middle_name;
		lastname   = last_name;
	}

	public String toString() {
		String output = "";
		output += entityId> 0 ? "entity: " + entityId : "";
		output += firstname!=null  && !firstname.equals("")  ? " " + firstname  : "";
		output += middlename!=null && !middlename.equals("") ? " " + middlename : "";
		output += lastname!=null   && !lastname.equals("")   ? " " + lastname   : "";
		return output;
	}

	public int compareTo( Object otherObject ) {
		PersonToMatch other = (PersonToMatch)otherObject;
		if ( other != null ) {
			return this.getSortName().compareTo(other.getSortName());
		}
		return 0;
	}

	public void setEntityId( int int_val ) {
		entityId = int_val;
	}

	public void setFirstname( String string_val ) {
		firstname = string_val;
	}

	public void setMiddlename( String string_val ) {
		middlename = string_val;
	}

	public void setLastname( String string_val ) {
		lastname = string_val;
	}

	public int getEntityId() {
		return entityId;
	}

	public String getOriginalname() {
		return originalname;
	}

	public String getFirstname() {
		return firstname;
	}

	public int getFirstnameLength() {
		return firstname==null ? 0 : firstname.length();
	}

	public String getFirstInitial() {
		return firstname==null || firstname.equals("") ? null : firstname.substring(0,1);
	}

	public String getMiddlename() {
		return middlename;
	}

	public int getMiddlenameLength() {
		return middlename==null ? 0 : middlename.length();
	}

	public String getMiddleInitial() {
		return middlename==null || middlename.equals("") ? null : middlename.substring(0,1);
	}

	public String getLastname() {
		return lastname;
	}

	public String getSortName() {
		String sortName=lastname + ", " + firstname;
		if (middlename != null && !middlename.equals("")) {
			sortName+=" " + middlename;
		}
		return sortName;
	}
}

