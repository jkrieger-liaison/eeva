package org.liaisonedu.poc;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluatorTest {
	
	
	private static ExpressionEvaluator subject = new ExpressionEvaluator();
	
	public static void main(String[] args) {
		ExpressionEvaluatorTest test = new ExpressionEvaluatorTest();
		
		System.out.println("TESTING BASIC (1 AND 2) EXPRESSION");
		System.out.println("----------------------------------");
		test.testSingleGroupANDGivenAllCriteriaMet();
		test.testSingleGroupANDGivenSomeCriteriaMet();
		
		System.out.println("");
		System.out.println("TESTING BASIC (1 OR 2) EXPRESSION");
		System.out.println("----------------------------------");
		test.testSingleGroupORGivenAllCriteriaMet();
		test.testSingleGroupORGivenSomeCriteriaMet();
		test.testSingleGroupORGivenNoCriteriaMet();
		// (1 AND 2 AND 3)
		// (1 OR 2 OR 3)
		System.out.println("");
		System.out.println("TESTING (1 AND (2 OR 3)) EXPRESSION");
		System.out.println("----------------------------------");
		test.testMultiGroupsANDORGivenAllCriteriaMet();
		test.testMultiGroupsANDORGivenAllOROnlyMet();
		test.testMultiGroupsANDORGivenAllANDWithSomeORMet();
		// (1 AND 2) AND (3 OR 4)
		System.out.println("");
		System.out.println("TESTING (1 AND 2) AND (3 OR 4) EXPRESSION");
		System.out.println("----------------------------------");
		test.testComplexGroups123No4();
		test.testComplexGroups12No34();
		test.testComplexGroups134No2();
			// (1 AND 2) OR (2 AND 3)
		//1 OR (2 AND 3)
		System.out.println("");
		System.out.println("TESTING 1 OR (2 AND 3) EXPRESSION");
		System.out.println("----------------------------------");
		test.testMultiGroups1No34();
		// (1 AND 2) OR (3 AND 4)
		// (1 OR 2) AND (2 AND 3)
		// (1 OR 2) AND (3 AND 4)
		// 1 AND (2 OR (3 AND 4))
		// 1 AND (4 AND 5 AND (2 OR 3))
	}
	
	// (1 AND 2)
	public void testSingleGroupANDGivenAllCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(1,2);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(Operator.AND, 1L, null, null, answers, criteria);
		Map<Long, Expression> expressionMap = buildExpressionMap(expression);
		assertResolveExpression(expressionMap, true);
	}
	
	public void testSingleGroupANDGivenSomeCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(2);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(Operator.AND, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), false);
	}
	
	public void testSingleGroupORGivenAllCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(1,2);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(Operator.OR, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), true);
	}

	public void testSingleGroupORGivenSomeCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(1,3);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(Operator.OR, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), true);
	}

	public void testSingleGroupORGivenNoCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(3,4);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(Operator.OR, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), false);
	}
	
	//(1 AND (2 OR 3))
	public void testMultiGroupsANDORGivenAllCriteriaMet() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(Operator.AND, 1L, 2L, Operator.AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(Operator.OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}

	public void testMultiGroupsANDORGivenAllOROnlyMet() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(6);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(Operator.AND, 1L, 2L, Operator.AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(Operator.OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}

	public void testMultiGroupsANDORGivenAllANDWithSomeORMet() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(Operator.AND, 1L, 2L, Operator.AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(Operator.OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}
	// (1 AND 2) AND (3 OR 4)
	public void testComplexGroups123No4() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1,2,3);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(Operator.AND, 1L, 2L, Operator.AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(Operator.OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}

	public void testComplexGroups12No34() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(5, 6);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(Operator.AND, 1L, 2L, Operator.AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(Operator.OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}

	public void testComplexGroups134No2() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(3 , 4);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(Operator.AND, 1L, 2L, Operator.AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(Operator.OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}
	
	// 1 OR (2 AND 3)
	public void testMultiGroups1No34() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(5, 6);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(Operator.OR, 1L, 2L, Operator.OR, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(Operator.AND, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}
	
	
	
	
	
	private void assertResolveExpression(Map<Long,Expression> expressionMap, boolean expectedResolution) {
		boolean result = subject.evaluate(expressionMap);
		for(Map.Entry<Long, Expression> entry : expressionMap.entrySet()) {
			System.out.println(entry.getValue().toString());
		}
		passOrFail(expectedResolution, result);
	}
	
	private void passOrFail(boolean expected, boolean actual) {
		System.out.print("expected: " + expected + " | actual: " + actual);
		if(expected == actual)
			System.out.println("\t======>\tPASS");
		else
			System.out.println("\t======>\tFAIL");
	}
	
	private List<KeyedValue> createKeyedValueList(long ... keys) {
		List<KeyedValue> values = new ArrayList<>();
		if (keys != null) {
			for(int i = 0; i < keys.length; i++) {
				values.add(new KeyedValue(keys[i]));
			}
		}
		return values;
	}
	
	private Map<Long, Expression> buildExpressionMap(Expression ... expressions) {
		Map<Long, Expression> expressionMap = new HashMap<>();
		for(Expression expression : expressions) {
			expressionMap.put(expression.groupId, expression);
		}
		return expressionMap;
	}
}
