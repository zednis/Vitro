package formbeans;
import java.util.*;
import java.lang.reflect.*;

// NOTE: MUST BE GET and SET METHODS FOR ALL VARIABLES IN ORDER FOR
// <jsp:setProperty name="docEditFormHandler" property="*"/> TO WORK

public class GenericFormBean {
	private boolean noisy=false;
	private final int MAX_FIELD_COUNT = 20;
	private String id;
	private String savedId;
	private String idFieldName;
	private String querySpec;
	private String querySpecId;
	private String querySpecFieldName;
	private String querySpecPostDelete;
	private String affilTables;
	private String affilTableFieldNames;
	private String errorJSP;
	private String retryJSP;

	private String fieldCountStr;
	private int fieldCount;
	private int staticFieldNum;

	private String tableName;
	private String testForDups;
	private String dupRecordTestFieldName;
	private String stemFieldName;
	private String stemDescriptorIdFieldName;

	private String findMaxId;

	private String postInsertTables;
	private String postInsertIds;
	private String postInsertFields;
	private String postInsertValues;

	private String field1Name;
	private String field2Name;
	private String field3Name;
	private String field4Name;
	private String field5Name;
	private String field6Name;
	private String field7Name;
	private String field8Name;
	private String field9Name;
	private String field10Name;
	private String field11Name;
	private String field12Name;
	private String field13Name;
	private String field14Name;
	private String field15Name;
	private String field16Name;
	private String field17Name;
	private String field18Name;
	private String field19Name;
	private String field20Name;

	public String field1Required;
	public String field2Required;
	public String field3Required;
	public String field4Required;
	public String field5Required;
	public String field6Required;
	public String field7Required;
	public String field8Required;
	public String field9Required;
	public String field10Required;
	public String field11Required;
	public String field12Required;
	public String field13Required;
	public String field14Required;
	public String field15Required;
	public String field16Required;
	public String field17Required;
	public String field18Required;
	public String field19Required;
	public String field20Required;

	private String field1Value;
	private String field2Value;
	private String field3Value;
	private String field4Value;
	private String field5Value;
	private String field6Value;
	private String field7Value;
	private String field8Value;
	private String field9Value;
	private String field10Value;
	private String field11Value;
	private String field12Value;
	private String field13Value;
	private String field14Value;
	private String field15Value;
	private String field16Value;
	private String field17Value;
	private String field18Value;
	private String field19Value;
	private String field20Value;

	private String field1AltNewValue;
	private String field2AltNewValue;
	private String field3AltNewValue;
	private String field4AltNewValue;
	private String field5AltNewValue;
	private String field6AltNewValue;
	private String field7AltNewValue;
	private String field8AltNewValue;
	private String field9AltNewValue;
	private String field10AltNewValue;
	private String field11AltNewValue;
	private String field12AltNewValue;
	private String field13AltNewValue;
	private String field14AltNewValue;
	private String field15AltNewValue;
	private String field16AltNewValue;
	private String field17AltNewValue;
	private String field18AltNewValue;
	private String field19AltNewValue;
	private String field20AltNewValue;

	private Hashtable errors;

	private String editMode;

	public boolean validate() {
		boolean allOk=true;
		int fieldCount = this.getFieldCount();
		System.out.println("Field count in validate is " + fieldCount );
		Class cl = this.getClass();
		try {
			//Field[] fields=cl.getDeclaredFields();
			//AccessibleObject.setAccessible(fields,true);
			for ( int i=1; i <= fieldCount; i++ ) {
				String fieldRequiredMethodStr = "getField" + i + "Required";
				try {
					//Field requiredField = cl.getField( fieldRequiredStr );
					//requiredField.setAccessible(true);
					Method requiredMethod = cl.getMethod( fieldRequiredMethodStr, null );
					try {
						//String fieldRequiredValue = (String)requiredField.get( this );
						String fieldRequiredValue = (String)requiredMethod.invoke( this, null );
						if ( fieldRequiredValue.equalsIgnoreCase("true") || fieldRequiredValue.equalsIgnoreCase("altnew")) { // now see if the associated field has a value
							String fieldValueStr = "getField" + i + "Value";
							try {
								Method fieldValueMethod = cl.getMethod( fieldValueStr, null );
								try {
									String fieldValue = (String)fieldValueMethod.invoke( this, null );
									if ( fieldValue==null || fieldValue.equals("")) {
										String fieldNameStr = "getField" + i + "Name"; // for use in diagnostics
										try {
											Method fieldNameMethod = cl.getMethod( fieldNameStr, null );
											try {
												String fieldName = (String)fieldNameMethod.invoke( this, null );
												if ( fieldName != null && !fieldName.equals("")) {
													if ( fieldRequiredValue.equalsIgnoreCase("true")) {
														errors.put( fieldName,"Please enter a value for " + fieldName );
														errors.put( "validation errors","[note validation errors]");
														allOk = false;
													} else { // "altnew", so look to see if alternate new value specified
														String altValueMethodStr = "getField" + i + "AltNewValue";
														try {
															Method altValueMethod = cl.getMethod( altValueMethodStr, null );
															try {
																String newAltFieldValue = (String)altValueMethod.invoke( this, null );
																if ( newAltFieldValue == null || newAltFieldValue.equals("")) {
																	errors.put( fieldName,"If you don't select an existing value, enter a new one to the right");
																	errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
																	System.out.println("Error: null or blank new alternate field value " + i + " in genericformbean validation");
																	allOk=false;
																} else { //altValueField.set( this, newAltFieldValue );
																	Object formBeanObj = this;
																	String valueMethodStr = "setField" + i + "Value";
																	try {
																		Method valueMethod = cl.getMethod( valueMethodStr, new Class[] { String.class } );
																		try {
																			Object[] args = { new String( newAltFieldValue ) };
																			valueMethod.invoke( formBeanObj, args );
																		} catch (Exception e) {
																			System.out.println("Error invoking method " + valueMethodStr + " in genericformbean validation" );
																			System.out.println("Exception message: " + e.getMessage() );
																			allOk=false;
																		}
																	} catch ( java.lang.NoSuchMethodException e	) {
																		System.out.println("Method " + valueMethodStr + " does not exist in genericformbean validation" );
																		System.out.println("Exception message: " + e.getMessage() );
																		allOk=false;
																	}
																}
															} catch ( Exception e ) {
																allOk = false;
																errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
																System.out.println("Error retrieving value of field " + altValueMethodStr + " in validate()" );
																System.out.println("Exception message: " + e.getMessage() );
															}
														} catch ( java.lang.NoSuchMethodException e ) {
															allOk = false;
															errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
															System.out.println("Error: mehtod " + altValueMethodStr + " not found with altnew value substitution in validate()" );
															System.out.println("Exception message: " + e.getMessage() );
														}
													}
												} else {
													errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
													System.out.println("Error: null or blank fieldname" + i + " in genericformbean validation");
													allOk=false;
												}
											} catch ( Exception e ) {
												errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
												allOk = false;
												System.out.println("Error invoking method " + fieldNameMethod + " in validate()" );
												System.out.println("Exception message: " + e.getMessage() );
											}
										} catch ( java.lang.NoSuchMethodException e ) {
											errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
											allOk = false;
											System.out.println("Error: method " + fieldNameStr + " not found in validate()" );
											System.out.println("Exception message: " + e.getMessage() );
										}
									} // else field not empty
								} catch ( Exception e ) {
									errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
									allOk = false;
									System.out.println("Error invoking method " + fieldValueStr + " in validate()" );
									System.out.println("Exception message: " + e.getMessage() );
								}
							} catch ( java.lang.NoSuchMethodException e ) {
								errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
								allOk = false;
								System.out.println("Error: method " + fieldValueStr + " not found in validate()" );
								System.out.println("Exception message: " + e.getMessage() );
							}
						} else if ( fieldRequiredValue.length() > 0 && fieldRequiredValue.indexOf("false") < 0 ) {
							String fieldValueStr = "getField" + i + "Value";
							if ( fieldRequiredValue.indexOf("http:") >= 0 ) {
								try {
									Method fieldValueMethod = cl.getMethod( fieldValueStr, null );
									try {
										String fieldValue = (String)fieldValueMethod.invoke( this, null );
										if ( fieldValue!=null && !fieldValue.equals("") && !(fieldValue.indexOf("http:")>=0) ) {
											String fieldNameStr = "getField" + i + "Name";
											try {
												Method fieldNameMethod = cl.getMethod( fieldNameStr, null );
												try {
													String fieldName = (String)fieldNameMethod.invoke( this, null );
													if ( fieldName != null && !fieldName.equals("")) {
														errors.put( fieldName,"Please enter a complete http address for " + fieldName );
														errors.put( "validation errors","[note validation errors]");
														allOk = false;
													} else {
														System.out.println("Error: null or blank fieldname" + i + " in genericformbean validation");
													}
												} catch ( Exception e ) {
													allOk = false;
													errors.put( "validation errors","[note validation errors]");
													System.out.println("Error invoking method " + fieldNameMethod + " in validate()" );
													System.out.println("Exception message: " + e.getMessage() );
												}
											} catch ( java.lang.NoSuchMethodException e ) {
												allOk = false;
												errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
												System.out.println("Error: method " + fieldNameStr + " not found with http required value in validate()" );
												System.out.println("Exception message: " + e.getMessage() );
											}
										} // else field is empty, so don't warn about malformed URL
									} catch ( Exception e ) {
										errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
										allOk = false;
										System.out.println("Error invoking method " + fieldValueStr + " in validate()" );
										System.out.println("Exception message: " + e.getMessage() );
									}
								} catch ( java.lang.NoSuchMethodException e ) {
									errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
									allOk = false;
									System.out.println("Error: method " + fieldValueStr + " not found in validate()" );
									System.out.println("Exception message: " + e.getMessage() );
								}
							} else if ( fieldRequiredValue.indexOf("@") >= 0 ) {
								try {
									Method fieldValueMethod = cl.getMethod( fieldValueStr, null );
									try {
										String fieldValue = (String)fieldValueMethod.invoke( this, null );
										if ( fieldValue!=null && !fieldValue.equals("") && !(fieldValue.indexOf("@")>0) ) {
											String fieldNameStr = "getField" + i + "Name";
											try {
												Method fieldNameMethod = cl.getMethod( fieldNameStr, null );
												try {
													String fieldName = (String)fieldNameMethod.invoke( this, null );
													if ( fieldName != null && !fieldName.equals("")) {
														errors.put( fieldName,"Please enter a valid email address for " + fieldName );
														errors.put( "validation errors","[note validation errors]");
														allOk = false;
													} else {
														System.out.println("Error: null or blank fieldname" + i + " in genericformbean validation");
													}
												} catch ( Exception e ) {
													allOk = false;
													errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
													System.out.println("Error invoking method " + fieldNameMethod + " in validate()" );
													System.out.println("Exception message: " + e.getMessage() );
												}
											} catch ( java.lang.NoSuchMethodException e ) {
												allOk = false;
												errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
												System.out.println("Error: method " + fieldNameStr + " not found with email required value in validate()" );
												System.out.println("Exception message: " + e.getMessage() );
											}
										} // else field is empty, so don't warn about malformed email address
									} catch ( Exception e ) {
										errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
										allOk = false;
										System.out.println("Error invoking method " + fieldValueStr + " in validate()" );
										System.out.println("Exception message: " + e.getMessage() );
									}
								} catch ( java.lang.NoSuchMethodException e ) {
									errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
									allOk = false;
									System.out.println("Error: method " + fieldValueStr + " not found in validate()" );
									System.out.println("Exception message: " + e.getMessage() );
								}
							} // ignore other options in fieldRequired (e.g., md5)
						}
					} catch ( Exception e ) {
						allOk = false;
						errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
						System.out.println("Error retrieving value of field " + fieldRequiredMethodStr + " in validate()" );
						System.out.println("Exception message: " + e.getMessage() );
					}
				} catch ( java.lang.NoSuchMethodException e ) {
					allOk = false;
					errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
					System.out.println("Error: method " + fieldRequiredMethodStr + " not found in validate()" );
					System.out.println("Exception message: " + e.getMessage() );
				}
			} // end for loop
		} catch ( Exception e ) {
			allOk = false;
			errors.put( "validation errors","<br>[system validation errors: contact SysAdmin]");
			System.out.println("Error: could not get declared fields or set them accessible in validate()" );
			System.out.println("Exception message: " + e.getMessage() );
		}
		return allOk;
	}

	public String getErrorMsg( String s ) {
		String errorMsg =(String) errors.get( s.trim() );
		return ( errorMsg == null ) ? "" : errorMsg;
	}

	public GenericFormBean() {
		editMode 		= "false";
		errors 			= new Hashtable();
		id 				= "";
		savedId 		= "";
		idFieldName 	= "";
		querySpec 		= "";
		querySpecId 	= "";
		querySpecFieldName	= "";
		querySpecPostDelete = "";
		errorJSP        = "";
		retryJSP 		= "";
		affilTables     = "";
		affilTableFieldNames = "";

		fieldCountStr 	= "";
		fieldCount 		= 0;
		staticFieldNum  = 0;

		tableName		= "";
		testForDups = "";
		dupRecordTestFieldName = "";
		stemFieldName = "";
		stemDescriptorIdFieldName = "";

		findMaxId 	= "";

		postInsertTables = "";
		postInsertIds = "";
		postInsertFields = "";
		postInsertValues = "";

		field1Name = "";
		field2Name = "";
		field3Name = "";
		field4Name = "";
		field5Name = "";
		field6Name = "";
		field7Name = "";
		field8Name = "";
		field9Name = "";
		field10Name = "";
		field11Name = "";
		field12Name = "";
		field13Name = "";
		field14Name = "";
		field15Name = "";
		field16Name = "";
		field17Name = "";
		field18Name = "";
		field19Name = "";
		field20Name = "";

		field1Required = "";
		field2Required = "";
		field3Required = "";
		field4Required = "";
		field5Required = "";
		field6Required = "";
		field7Required = "";
		field8Required = "";
		field9Required = "";
		field10Required = "";
		field11Required = "";
		field12Required = "";
		field13Required = "";
		field14Required = "";
		field15Required = "";
		field16Required = "";
		field17Required = "";
		field18Required = "";
		field19Required = "";
		field20Required = "";

		field1Value = "";
		field2Value = "";
		field3Value = "";
		field4Value = "";
		field5Value = "";
		field6Value = "";
		field7Value = "";
		field8Value = "";
		field9Value = "";
		field10Value = "";
		field11Value = "";
		field12Value = "";
		field13Value = "";
		field14Value = "";
		field15Value = "";
		field16Value = "";
		field17Value = "";
		field18Value = "";
		field19Value = "";
		field20Value = "";

		field1AltNewValue = "";
		field2AltNewValue = "";
		field3AltNewValue = "";
		field4AltNewValue = "";
		field5AltNewValue = "";
		field6AltNewValue = "";
		field7AltNewValue = "";
		field8AltNewValue = "";
		field9AltNewValue = "";
		field10AltNewValue = "";
		field11AltNewValue = "";
		field12AltNewValue = "";
		field13AltNewValue = "";
		field14AltNewValue = "";
		field15AltNewValue = "";
		field16AltNewValue = "";
		field17AltNewValue = "";
		field18AltNewValue = "";
		field19AltNewValue = "";
		field20AltNewValue = "";
	}

    /********************** GET METHODS *********************/

	public boolean isNoisy() {
		return noisy;
	}

	public String getId() {
		return id == null ? "" : id;
	}

	public String getSavedId() {
		return savedId == null ? "" : savedId;
	}

	public String getIdFieldName() {
		return idFieldName == null ? "" : idFieldName;
	}

	public String getQuerySpec() {
		return querySpec == null ? "" : querySpec;
	}

	public String getQuerySpecId() {
		return querySpecId == null ? "" : querySpecId;
	}

	public String getQuerySpecFieldName() {
		return querySpecFieldName == null ? "" : querySpecFieldName;
	}

	public String getQuerySpecPostDelete() {
		return querySpecPostDelete == null ? "" : querySpecPostDelete;
	}

	public String getAffilTables() {
		return affilTables == null ? "" : affilTables;
	}

	public String getAffilTableFieldNames() {
		return affilTableFieldNames == null ? "" : affilTableFieldNames;
	}

	public String getErrorJSP() {
		return errorJSP == null ? "" : errorJSP;
	}

	public String getRetryJSP() {
		return retryJSP == null ? "" : retryJSP;
	}

	public int getFieldCount() {
		return fieldCount;
	}

	public String getFieldCountStr() {
		return fieldCountStr;
	}

	public int getStaticFieldNum() {
		return staticFieldNum;
	}

	public String getTableName() {
		return tableName == null ? "" : tableName;
	}

	public String getTestForDups() {
		return testForDups == null ? "" : testForDups;
	}

	public String getDupRecordTestFieldName() {
		return dupRecordTestFieldName == null ? "" : dupRecordTestFieldName;
	}

	public String getStemFieldName() {
		return stemFieldName == null ? "" : stemFieldName;
	}

	public String getStemDescriptorIdFieldName() {
		return stemDescriptorIdFieldName == null ? "" : stemDescriptorIdFieldName;
	}

	public String getFindMaxId() {
		return findMaxId == null ? "" : findMaxId;
	}

	public String getPostInsertTables() {
		return postInsertTables == null ? "" : postInsertTables;
	}

	public String getPostInsertIds() {
		return postInsertIds == null ? "" : postInsertIds;
	}

	public String getPostInsertFields() {
		return postInsertFields == null ? "" : postInsertFields;
	}

	public String getPostInsertValues() {
		return postInsertValues == null ? "" : postInsertValues;
	}

	public String getField1Name() {
		return field1Name == null ? "" : field1Name;
	}

	public String getField1Required() {
		return field1Required == null ? "" : field1Required;
	}

	public String getField1Value() {
		return field1Value == null ? "" : unEscapeApostrophes(field1Value);
	}

	public String getField1AltNewValue() {
		return field1AltNewValue == null ? "" : unEscapeApostrophes(field1AltNewValue);
	}

	public String getField2Name() {
		return field2Name == null ? "" : field2Name;
	}

	public String getField2Required() {
		return field2Required == null ? "" : field2Required;
	}

	public String getField2Value() {
		return field2Value == null ? "" : unEscapeApostrophes(field2Value);
	}

	public String getField2AltNewValue() {
		return field2AltNewValue == null ? "" : unEscapeApostrophes(field2AltNewValue);
	}

	public String getField3Name() {
		return field3Name == null ? "" : field3Name;
	}

	public String getField3Required() {
		return field3Required == null ? "" : field3Required;
	}

	public String getField3Value() {
		return field3Value == null ? "" : unEscapeApostrophes(field3Value);
	}

	public String getField3AltNewValue() {
		return field3AltNewValue == null ? "" : unEscapeApostrophes(field3AltNewValue);
	}

	public String getField4Name() {
		return field4Name == null ? "" : field4Name;
	}

	public String getField4Required() {
		return field4Required == null ? "" : field4Required;
	}

	public String getField4Value() {
		return field4Value == null ? "" : unEscapeApostrophes(field4Value);
	}

	public String getField4AltNewValue() {
		return field4AltNewValue == null ? "" : unEscapeApostrophes(field4AltNewValue);
	}

	public String getField5Name() {
		return field5Name == null ? "" : field5Name;
	}

	public String getField5Required() {
		return field5Required == null ? "" : field5Required;
	}

	public String getField5Value() {
		return field5Value == null ? "" : unEscapeApostrophes(field5Value);
	}

	public String getField5AltNewValue() {
		return field5AltNewValue == null ? "" : unEscapeApostrophes(field5AltNewValue);
	}

	public String getField6Name() {
		return field6Name == null ? "" : field6Name;
	}

	public String getField6Required() {
		return field6Required == null ? "" : field6Required;
	}

	public String getField6Value() {
		return field6Value == null ? "" : unEscapeApostrophes(field6Value);
	}

	public String getField6AltNewValue() {
		return field6AltNewValue == null ? "" : unEscapeApostrophes(field6AltNewValue);
	}

	public String getField7Name() {
		return field7Name == null ? "" : field7Name;
	}

	public String getField7Required() {
		return field7Required == null ? "" : field7Required;
	}

	public String getField7Value() {
		return field7Value == null ? "" : unEscapeApostrophes(field7Value);
	}

	public String getField7AltNewValue() {
		return field7AltNewValue == null ? "" : unEscapeApostrophes(field7AltNewValue);
	}

	public String getField8Name() {
		return field8Name == null ? "" : field8Name;
	}

	public String getField8Required() {
		return field8Required == null ? "" : field8Required;
	}

	public String getField8Value() {
		return field8Value == null ? "" : unEscapeApostrophes(field8Value);
	}

	public String getField8AltNewValue() {
		return field8AltNewValue == null ? "" : unEscapeApostrophes(field8AltNewValue);
	}

	public String getField9Name() {
		return field9Name == null ? "" : field9Name;
	}

	public String getField9Required() {
		return field9Required == null ? "" : field9Required;
	}

	public String getField9Value() {
		return field9Value == null ? "" : unEscapeApostrophes(field9Value);
	}

	public String getField9AltNewValue() {
		return field9AltNewValue == null ? "" : unEscapeApostrophes(field9AltNewValue);
	}

	public String getField10Name() {
		return field10Name == null ? "" : field10Name;
	}

	public String getField10Required() {
		return field10Required == null ? "" : field10Required;
	}

	public String getField10Value() {
		return field10Value == null ? "" : unEscapeApostrophes(field10Value);
	}

	public String getField10AltNewValue() {
		return field10AltNewValue == null ? "" : unEscapeApostrophes(field10AltNewValue);
	}

	public String getField11Name() {
		return field11Name == null ? "" : field11Name;
	}

	public String getField11Required() {
		return field11Required == null ? "" : field11Required;
	}

	public String getField11Value() {
		return field11Value == null ? "" : unEscapeApostrophes(field11Value);
	}

	public String getField11AltNewValue() {
		return field11AltNewValue == null ? "" : unEscapeApostrophes(field11AltNewValue);
	}

	public String getField12Name() {
		return field12Name == null ? "" : field12Name;
	}

	public String getField12Required() {
		return field12Required == null ? "" : field12Required;
	}

	public String getField12Value() {
		return field12Value == null ? "" : unEscapeApostrophes(field12Value);
	}

	public String getField12AltNewValue() {
		return field12AltNewValue == null ? "" : unEscapeApostrophes(field12AltNewValue);
	}

	public String getField13Name() {
		return field13Name == null ? "" : field13Name;
	}

	public String getField13Required() {
		return field13Required == null ? "" : field13Required;
	}

	public String getField13Value() {
		return field13Value == null ? "" : unEscapeApostrophes(field13Value);
	}

	public String getField13AltNewValue() {
		return field13AltNewValue == null ? "" : unEscapeApostrophes(field13AltNewValue);
	}

	public String getField14Name() {
		return field14Name == null ? "" : field14Name;
	}

	public String getField14Required() {
		return field14Required == null ? "" : field14Required;
	}

	public String getField14Value() {
		return field14Value == null ? "" : unEscapeApostrophes(field14Value);
	}

	public String getField14AltNewValue() {
		return field14AltNewValue == null ? "" : unEscapeApostrophes(field14AltNewValue);
	}

	public String getField15Name() {
		return field15Name == null ? "" : field15Name;
	}

	public String getField15Required() {
		return field15Required == null ? "" : field15Required;
	}

	public String getField15Value() {
		return field15Value == null ? "" : unEscapeApostrophes(field15Value);
	}

	public String getField15AltNewValue() {
		return field15AltNewValue == null ? "" : unEscapeApostrophes(field15AltNewValue);
	}

	public String getField16Name() {
		return field16Name == null ? "" : field16Name;
	}

	public String getField16Required() {
		return field16Required == null ? "" : field16Required;
	}

	public String getField16Value() {
		return field16Value == null ? "" : unEscapeApostrophes(field16Value);
	}

	public String getField16AltNewValue() {
		return field16AltNewValue == null ? "" : unEscapeApostrophes(field16AltNewValue);
	}

	public String getField17Name() {
		return field17Name == null ? "" : field17Name;
	}

	public String getField17Required() {
		return field17Required == null ? "" : field17Required;
	}

	public String getField17Value() {
		return field17Value == null ? "" : unEscapeApostrophes(field17Value);
	}

	public String getField17AltNewValue() {
		return field17AltNewValue == null ? "" : unEscapeApostrophes(field17AltNewValue);
	}

	public String getField18Name() {
		return field18Name == null ? "" : field18Name;
	}

	public String getField18Required() {
		return field18Required == null ? "" : field18Required;
	}

	public String getField18Value() {
		return field18Value == null ? "" : unEscapeApostrophes(field18Value);
	}

	public String getField18AltNewValue() {
		return field18AltNewValue == null ? "" : unEscapeApostrophes(field18AltNewValue);
	}

	public String getField19Name() {
		return field19Name == null ? "" : field19Name;
	}

	public String getField19Required() {
		return field19Required == null ? "" : field19Required;
	}

	public String getField19Value() {
		return field19Value == null ? "" : unEscapeApostrophes(field19Value);
	}

	public String getField19AltNewValue() {
		return field19AltNewValue == null ? "" : unEscapeApostrophes(field19AltNewValue);
	}

	public String getField20Name() {
		return field20Name == null ? "" : field20Name;
	}

	public String getField20Required() {
		return field20Required == null ? "" : field20Required;
	}

	public String getField20Value() {
		return field20Value == null ? "" : unEscapeApostrophes(field20Value);
	}

	public String getField20AltNewValue() {
		return field20AltNewValue == null ? "" : unEscapeApostrophes(field20AltNewValue);
	}

	public String getEditMode() {
		return editMode;
	}

    /********************** SET METHODS *********************/

	public void setNoisy( boolean boolean_val ) {
		noisy = boolean_val;
	}

	public void setId( String istr ) {
		id = istr;
	}

	public void setSavedId( String sstr ) {
		savedId = ( sstr == null ? "" : sstr.equals("null") ? "" : sstr );
	}

	public void setIdFieldName( String idfield ) {
		idFieldName = ( idfield == null ? "" : idfield.equals("null") ? "" : idfield );
	}

	public void setQuerySpec( String q ) {
		querySpec = ( q == null ? "" : q.equals("null") ? "" : q );
	}

	public void setQuerySpecId( String qi ) {
		querySpecId = ( qi == null ? "" : qi.equals("null") ? "" : qi );
	}

	public void setQuerySpecFieldName( String qf ) {
		querySpecFieldName = ( qf == null ? "" : qf.equals("null") ? "" : qf );
	}

	public void setQuerySpecPostDelete( String qpd ) {
		querySpecPostDelete = ( qpd == null ? "" : qpd.equals("null") ? "" : qpd );
	}

	public void setAffilTables( String a ) {
		affilTables = ( a == null ? "" : a.equals("null") ? "" : a );
	}

	public void setAffilTableFieldNames( String afi ) {
		affilTableFieldNames = ( afi == null ? "" : afi.equals("null") ? "" : afi );
	}

	public void setErrorJSP( String e ) {
		errorJSP = ( e == null ? "" : e.equals("null") ? "" : e );
	}

	public void setRetryJSP( String r ) {
		retryJSP = ( r == null ? "" : r.equals("null") ? "" : r );
	}

	public void setFieldCountStr ( String val ) {
		fieldCountStr = ( val == null ? "" : val.equals("null") ? "" : val );
		int count = Integer.parseInt( fieldCountStr );
		if ( count > 0 && count <= MAX_FIELD_COUNT )
			fieldCount = count;
		else
			System.out.println("Error: trying to set fieldCount to " + fieldCountStr + " in generic formbean");
	}

	public void setStaticFieldNum ( int fieldNum ) {
		if ( fieldNum > 0 && fieldNum <= MAX_FIELD_COUNT )
			staticFieldNum = fieldNum;
		else
			System.out.println("Error: trying to set static field number to " + fieldNum + " in generic formbean");
	}

	public void setTableName( String table ) {
		tableName = ( table == null ? "" : table.equals("null") ? "" : table );
	}

	public void setTestForDups( String name ) {
		testForDups = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setDupRecordTestFieldName( String name ) {
		dupRecordTestFieldName = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setStemFieldName( String name ) {
		stemFieldName = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setStemDescriptorIdFieldName( String name ) {
		stemDescriptorIdFieldName = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setFindMaxId( String name ) {
		findMaxId = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setPostInsertTables( String name ) {
		postInsertTables = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setPostInsertIds( String name ) {
		postInsertIds = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setPostInsertFields( String name ) {
		postInsertFields = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setPostInsertValues( String name ) {
		postInsertValues = ( name == null ? "" : name.equals("null") ? "" : name );
	}

	public void setField1Name( String f ) {
		field1Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField1Required( String f ) {
		field1Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField1Value( String f ) {
		field1Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField1AltNewValue( String f ) {
		field1AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField2Name( String f ) {
		field2Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField2Required( String f ) {
		field2Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField2Value( String f ) {
		field2Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField2AltNewValue( String f ) {
		field2AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField3Name( String f ) {
		field3Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField3Required( String f ) {
		field3Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField3Value( String f ) {
		field3Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField3AltNewValue( String f ) {
		field3AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField4Name( String f ) {
		field4Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField4Required( String f ) {
		field4Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField4Value( String f ) {
		field4Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField4AltNewValue( String f ) {
		field4AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField5Name( String f ) {
		field5Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField5Required( String f ) {
		field5Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField5Value( String f ) {
		field5Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField5AltNewValue( String f ) {
		field5AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField6Name( String f ) {
		field6Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField6Required( String f ) {
		field6Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField6Value( String f ) {
		field6Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField6AltNewValue( String f ) {
		field6AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField7Name( String f ) {
		field7Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField7Required( String f ) {
		field7Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField7Value( String f ) {
		field7Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField7AltNewValue( String f ) {
		field7AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField8Name( String f ) {
		field8Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField8Required( String f ) {
		field8Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField8Value( String f ) {
		field8Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField8AltNewValue( String f ) {
		field8AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField9Name( String f ) {
		field9Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField9Required( String f ) {
		field9Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField9Value( String f ) {
		field9Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField9AltNewValue( String f ) {
		field9AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField10Name( String f ) {
		field10Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField10Required( String f ) {
		field10Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField10Value( String f ) {
		field10Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField10AltNewValue( String f ) {
		field10AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField11Name( String f ) {
		field11Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField11Required( String f ) {
		field11Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField11Value( String f ) {
		field11Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField11AltNewValue( String f ) {
		field11AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField12Name( String f ) {
		field12Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField12Required( String f ) {
		field12Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField12Value( String f ) {
		field12Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField12AltNewValue( String f ) {
		field12AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField13Name( String f ) {
		field13Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField13Required( String f ) {
		field13Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField13Value( String f ) {
		field13Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField13AltNewValue( String f ) {
		field13AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField14Name( String f ) {
		field14Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField14Required( String f ) {
		field14Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField14Value( String f ) {
		field14Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField14AltNewValue( String f ) {
		field14AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField15Name( String f ) {
		field15Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField15Required( String f ) {
		field15Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField15Value( String f ) {
		field15Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField15AltNewValue( String f ) {
		field15AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField16Name( String f ) {
		field16Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField16Required( String f ) {
		field16Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField16Value( String f ) {
		field16Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField16AltNewValue( String f ) {
		field16AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField17Name( String f ) {
		field17Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField17Required( String f ) {
		field17Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField17Value( String f ) {
		field17Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField17AltNewValue( String f ) {
		field17AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField18Name( String f ) {
		field18Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField18Required( String f ) {
		field18Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField18Value( String f ) {
		field18Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField18AltNewValue( String f ) {
		field18AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField19Name( String f ) {
		field19Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField19Required( String f ) {
		field19Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField19Value( String f ) {
		field19Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField19AltNewValue( String f ) {
		field19AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField20Name( String f ) {
		field20Name = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField20Required( String f ) {
		field20Required = ( f == null ? "" : f.equals("null") ? "" : f );
	}

	public void setField20Value( String f ) {
		field20Value = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setField20AltNewValue( String f ) {
		field20AltNewValue = ( f == null ? "" : f.equals("null") ? "" : escapeApostrophes(f) );
	}

	public void setErrors( String key, String msg ) {
		errors.put( key,msg );
	}

	public void setEditMode( String flagVal ) {
		editMode = flagVal;
	}

	/********************************* utilities *********************************/

	private static String stripLeadingSpaces( String termStr ) {
		int characterPosition= -1;

		while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
			termStr = termStr.substring(characterPosition+1);
		}
		return termStr;
	}

	private static String unEscapeApostrophes( String termStr ) {
		int characterPosition= -1;

		while ( ( characterPosition = termStr.indexOf( 92, characterPosition+1 ) ) >= 0 ) {
			if ( termStr.charAt( characterPosition+1 )==39 ) {
				termStr = termStr.substring(0,characterPosition) + termStr.substring(characterPosition+1);
			}
		}
		return termStr;
	}

	private static String escapeApostrophes( String termStr ) {
		int characterPosition= -1;

		if (termStr==null || termStr.equals("")) {
			return termStr;
		}
		while ( ( characterPosition = termStr.indexOf( 32, characterPosition+1 ) ) == 0 ) {
			termStr = termStr.substring(characterPosition+1);
		}
		characterPosition=-1;
		while ( ( characterPosition = termStr.indexOf( 39, characterPosition+1 ) ) >= 0 ) {
			if ( characterPosition == 0 ) // just drop it
				termStr = termStr.substring( characterPosition+1 );
			else if ( termStr.charAt(characterPosition-1) != 92 ) // already escaped
				termStr = termStr.substring(0,characterPosition) + "\\" + termStr.substring(characterPosition);
			++characterPosition;
		}
		return termStr;
	}

}
