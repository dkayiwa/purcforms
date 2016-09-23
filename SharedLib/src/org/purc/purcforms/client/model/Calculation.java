package org.purc.purcforms.client.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import org.purc.purcforms.client.util.FormUtil;
import org.purc.purcforms.client.xforms.XformConstants;

import com.google.gwt.user.client.Window;
import com.google.gwt.xml.client.Element;


/**
 * 
 * @author daniel
 *
 */
public class Calculation implements Serializable {

	private static final long serialVersionUID = 1L;

	/** The unique identifier of the question whose value to calculate. */
	private int questionId = ModelConstants.NULL_ID;
	
	/** The calculate xpath expression. */
	private String calculateExpression = ModelConstants.EMPTY_STRING;


	public Calculation(Calculation calculation) {
		this(calculation.getQuestionId(),calculation.getCalculateExpression());
	}

	public Calculation(int questionId, String calculateExpression) {
		this.questionId = questionId;
		setXmlSafeCalculateExpression(calculateExpression);
	}

	public int getQuestionId() {
		return questionId;
	}

	public void setQuestionId(int questionId) {
		this.questionId = questionId;
	}

	public String getCalculateExpression() {
		return calculateExpression;
	}
	
	public String getXmlSafeCalculateExpression() {
		return calculateExpression != null ? calculateExpression.replaceAll("\\n", "\\\\n") : calculateExpression;
	}

	public String getNormalizedCalculateExpression() {
		return calculateExpression == null ? "" : calculateExpression.replaceAll("\\n", " ");
	}
	
	public void setCalculateExpression(String calculateExpression) {
		if (calculateExpression == null) {
			calculateExpression = ModelConstants.EMPTY_STRING;
		} else {
			this.calculateExpression = calculateExpression;
		}
	}
	
	public void setXmlSafeCalculateExpression(String rawExpression) {
		setCalculateExpression(rawExpression != null ? rawExpression.replaceAll("\\\\n", "\n") : rawExpression);
	}
	
	public void updateDoc(FormDef formDef){
		QuestionDef questionDef = formDef.getQuestion(questionId);
		assert(questionDef != null);
		Element node = questionDef.getBindNode();
		if(node == null)
			node = questionDef.getControlNode();
		
		node.setAttribute(XformConstants.ATTRIBUTE_NAME_CALCULATE,calculateExpression);
	}
	
	public List<Entity> getEntities(FormDef formDef) {
		List<Entity> entities = new ArrayList<>(); 
		
		String qtnBinding, formBinding = "/" + formDef.getBinding() + "/";
		String expression = getNormalizedCalculateExpression();
	
		int pos = expression.indexOf(formBinding);
		boolean fail = false;
		while (pos > -1) {
			Entity ent = new Entity();
			// -- find the full entity --
			int pos2 = expression.indexOf(' ', pos);
			if(pos2 > -1)
				qtnBinding = expression.substring(pos,pos2);
			else
				qtnBinding = expression.substring(pos);
			ent.setEntityName(qtnBinding);
			
			// -- find the questionName --
			qtnBinding = qtnBinding.substring(formBinding.length());

			// -- is aggregate function on group?
			if (qtnBinding.matches(".*\\/(sum|avg|min|max|count)\\(.*\\)")) {
				qtnBinding = qtnBinding.substring(0, qtnBinding.lastIndexOf("/"));
				ent.setAggregate(true);
				String eN = ent.getEntityName();
				int start = eN.lastIndexOf("/") + 1;
				ent.setAggregateFunction(eN.substring(start, eN.indexOf("(")));
				ent.setAggregateQuestionName(eN.substring(eN.indexOf("(") + 1, eN.length()-1));
			}
			// -- set question name
			ent.setQuestionName(qtnBinding);
			
			// -- Find question --
			
			QuestionDef questionDef = formDef.getQuestion(qtnBinding);
			if(questionDef == null){
				qtnBinding = FormUtil.getBinding(qtnBinding);   
				questionDef = formDef.getQuestion(qtnBinding);
			}

			if (questionDef != null) {
				ent.setQuestionDef(questionDef);

				// -- do some checks
				if (ent.isAggregate()) {
					if (QuestionDef.QTN_TYPE_REPEAT != questionDef.getDataType()) {
						Window.alert("Aggregaat functies kunnen enkel op een repeat veld gebruikt worden in een berekend veld.\nControleer de syntax van het berekend veld: \n\nVraag: " + qtnBinding + "\nExpressie: " + expression);
						fail = true;
					} else {
						QuestionDef inner = questionDef.getGroupQtnsDef().getQuestion(ent.getAggregateQuestionName());
						if (inner == null) { // no point in saving questionDef here, just checking
							Window.alert("Inner veld van groep, onderwerp van Aggregaat functie niet gevonden.\nControleer de syntax van het berekend veld: \n\nVraag: " + qtnBinding + "\nExpressie: " + expression);
							fail = true;
						}
						if (!(QuestionDef.QTN_TYPE_DECIMAL == inner.getDataType() || QuestionDef.QTN_TYPE_NUMERIC == inner.getDataType())) {
							Window.alert("Inner veld van groep, onderwerp van Aggregaat functie moet van type decimal of number zijn. \n\nVraag: " + qtnBinding + "\nExpressie: " + expression);
							fail = true;
						}
					}
				} else {
					if (QuestionDef.QTN_TYPE_REPEAT == questionDef.getDataType()) {
						Window.alert("Repeats kunnen enkel met een aggregaat functie opgenomen worden in een berekend veld.\nControleer de syntax van het berekend veld: \n\nVraag: " + qtnBinding + "\nExpressie: " + expression);
						fail = true;
					}
				}
				
			} else {
				Window.alert("Vraag niet gevonden.\nControleer de syntax van berekend veld: \n\nVraag: " + qtnBinding + "\nExpressie: " + expression);
				fail = true;
			}

			// -- finally add entity to list
			
			if (!fail) {
				entities.add(ent);
			}
			
			// -- prepare for next --
			
			if(pos2 > -1) {
				pos = expression.indexOf(formBinding,pos2+1);
				fail = false;
			} else {
				break;
			}
		}
		return entities;
	}
	
	// ---------------------------------------------------
	
	public class Entity {
		
		private String entityName;
		private String questionName;
		private boolean aggregate = false;
		private String aggregateFunction;
		private String aggregateQuestionName;
		private QuestionDef questionDef;
		
		public String getEntityName() {
			return entityName;
		}
		
		public void setEntityName(String entityName) {
			this.entityName = entityName;
		}
		
		public String getQuestionName() {
			return questionName;
		}
		
		public void setQuestionName(String questionName) {
			this.questionName = questionName;
		}
		
		public boolean isAggregate() {
			return aggregate;
		}
		
		public void setAggregate(boolean aggregate) {
			this.aggregate = aggregate;
		}
		
		public String getAggregateFunction() {
			return aggregateFunction;
		}
		
		public void setAggregateFunction(String aggregateFunction) {
			if (aggregateFunction != null) { aggregateFunction = aggregateFunction.toLowerCase(); }
			this.aggregateFunction = aggregateFunction;
		}
		
		public String getAggregateQuestionName() {
			return aggregateQuestionName;
		}
		
		public void setAggregateQuestionName(String aggregateQuestionName) {
			this.aggregateQuestionName = aggregateQuestionName;
		}
		
		public QuestionDef getQuestionDef() {
			return questionDef;
		}
		
		public void setQuestionDef(QuestionDef questionDef) {
			this.questionDef = questionDef;
		}
		
		@Override
		public String toString() {
			return entityName;
		}
		
		public Double executeAggregateFunction(List<String> values) {
			return AggregateFunction.create(aggregateFunction).execute(AggregateFunction.toDoubles(values));
		}
	}
	
}
