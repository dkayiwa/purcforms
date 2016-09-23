package org.purc.purcforms.client.model;

import java.util.Vector;


/**
 * Definition for repeat sets of questions. Basically this is just a specialized collection
 * of a set of repeating questions, together with reference to their parent question.
 * 
 * @author daniel
 *
 */
public class RepeatQtnsDef extends GroupQtnsDef {

	private static final long serialVersionUID = 1L;
	
	/** The maximum number of rows that this repeat questions definition can have. */
	private byte maxRows = -1;
	
	/**
	 * Creates a new repeat questions definition object.
	 */
	public RepeatQtnsDef() {
	}
	
	/** Copy Constructor. */
	public RepeatQtnsDef(RepeatQtnsDef repeatQtnsDef) {
		super(repeatQtnsDef);
	}
	
	public RepeatQtnsDef(QuestionDef qtnDef) {
		super(qtnDef);
	}
	
	public RepeatQtnsDef(QuestionDef qtnDef,Vector<QuestionDef> questions) {
		super(qtnDef, questions);
	}
	
	public void setMaxRows(byte maxRows){
		this.maxRows = maxRows;
	}
	
	public byte getMaxRows(){
		return maxRows;
	}
}
