package org.purc.purcforms.client.model;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.GWT;

/**
 * @author Kristof Heirwegh
 */
public abstract class AggregateFunction {

	public static final String FUNCTION_SUM = "sum";
	public static final String FUNCTION_AVG = "avg";
	public static final String FUNCTION_MIN = "min";
	public static final String FUNCTION_MAX = "max";
	public static final String FUNCTION_COUNT = "count";

	public abstract Double execute(List<Double> values);
	
	private AggregateFunction() {}

	public static AggregateFunction create(String functionName) {
		switch (functionName) {
			case FUNCTION_SUM:
				return new Sum();
			case FUNCTION_AVG:
				return new Avg();
			case FUNCTION_MIN:
				return new Min();
			case FUNCTION_MAX:
				return new Max();
			case FUNCTION_COUNT:
				return new Count();
			default:
				GWT.log("Invalid function name: " + functionName);
				return new DummyFunction();
		}
	}
	
	public static List<Double> toDoubles(List<String> values) {
		try {
			List<Double> res = new ArrayList<Double>();
			for (String rawValue : values) {
				if (rawValue == null || "".equals(rawValue)) {
					rawValue = null;
				} else {
					res.add(Double.parseDouble(rawValue));					
				}
			}
			return res;
		} catch (Exception e) {
			GWT.log("Invalid numeric value!");
			return null;
		}
	}
	
	// ---------------------------------------------------

	public static class DummyFunction extends AggregateFunction {
		@Override
		public Double execute(List<Double> values) {
			return null;
		}
	}
	
	public static class Sum extends AggregateFunction {
		@Override
		public Double execute(List<Double> values) {
			if (values == null) { return null; }
			if (values.isEmpty()) { return 0.0; }

			Double res = 0.0;
			for (Double d : values) {
				if (d != null) {
					res += d;
				}
			}
			return res;
		}
	}
	
	public static class Avg extends AggregateFunction {
		@Override
		public Double execute(List<Double> values) {
			if (values == null) { return null; }
			if (values.isEmpty()) { return 0.0; }
			
			Double res = 0.0;
			int count = 0;
			for (Double d : values) {
				if (d != null) {
					res += d;
					count++;
				}
			}
			if (res != 0 && count > 0) { res /= count; }
			return res;
		}
	}
	
	public static class Min extends AggregateFunction {
		@Override
		public Double execute(List<Double> values) {
			if (values == null) { return null; }
			if (values.isEmpty()) { return 0.0; }
			
			Double res = Double.MAX_VALUE;
			for (Double d : values) {
				if (d != null) {
					if (d < res) { res = d; }
				}
			}
			
			return res;
		}
	}
	
	public static class Max extends AggregateFunction {
		@Override
		public Double execute(List<Double> values) {
			if (values == null) { return null; }
			if (values.isEmpty()) { return 0.0; }
			
			Double res = Double.MIN_VALUE;
			for (Double d : values) {
				if (d != null) {
					if (d > res) { res = d; }
				}
			}
			
			return res;
		}
	}

	public static class Count extends AggregateFunction {
		@Override
		public Double execute(List<Double> values) {
			if (values == null) { return null; }
			int count = 0;
			for (Double d : values) {
				if (d != null) { count++; }
			}
			return (double) count;
		}
	}

}
