package org.liaisonedu.poc;

import static org.liaisonedu.poc.Operator.*;
import static org.liaisonedu.poc.Operator.OR;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ExpressionEvaluatorTest {
	
	
	private static ExpressionEvaluator subject = new ExpressionEvaluator();
	
	private static boolean failFlag = false;
	
	public static void main(String[] args) {
		ExpressionEvaluatorTest test = new ExpressionEvaluatorTest();
		failFlag = false;
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
		test.testMultiGroups1No23();
		test.testMultiGroups23No1();
		test.testMultiGroupsNo123();
		test.testMultiGroups2No13();
		// (1 AND 2) OR (3 AND 4)
		// (1 OR 2) AND (2 AND 3)
		// (1 OR 2) AND (3 AND 4)
		// 1 AND (2 OR (3 AND 4))
		System.out.println("");
		System.out.println("TESTING 1 AND ((2 OR 3) AND (4 OR 5)) EXPRESSION");
		System.out.println("----------------------------------");
		test.testNestedChildren125IsTrue();
		test.testNestedChildren125WithReverseMapOrderIsTrue();
		test.testNestedChildren1236Fails();
		
		System.out.println("");
		System.out.println("TESTING 1 OR ((2 OR 3) AND ((4 OR 5) OR (6 AND 7))) EXPRESSION");
		System.out.println("----------------------------------");
		test.testSomeCraziness0267ShouldBeTrue();
		//Report failures
		System.out.println("");
		System.out.print("Overall Testing Status:");
		if(failFlag) {
			System.out.print(" FAIL");
		} else {
			System.out.print(" PASS");
		}
	}
	
	// (1 AND 2)
	public void testSingleGroupANDGivenAllCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(1,2);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(AND, 1L, null, null, answers, criteria);
		Map<Long, Expression> expressionMap = buildExpressionMap(expression);
		assertResolveExpression(expressionMap, true);
	}
	
	public void testSingleGroupANDGivenSomeCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(2);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(AND, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), false);
	}
	
	public void testSingleGroupORGivenAllCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(1,2);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(OR, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), true);
	}

	public void testSingleGroupORGivenSomeCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(1,3);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(OR, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), true);
	}

	public void testSingleGroupORGivenNoCriteriaMet() {
		List<KeyedValue> answers = createKeyedValueList(3,4);
		List<KeyedValue> criteria = createKeyedValueList(1,2);
		Expression expression = new Expression(OR, null, null, null, answers, criteria);
		assertResolveExpression(buildExpressionMap(expression), false);
	}
	
	//(1 AND (2 OR 3))
	public void testMultiGroupsANDORGivenAllCriteriaMet() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(AND, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}

	public void testMultiGroupsANDORGivenAllOROnlyMet() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(6);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(AND, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}

	public void testMultiGroupsANDORGivenAllANDWithSomeORMet() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(AND, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}
	// (1 AND 2) AND (3 OR 4)
	public void testComplexGroups123No4() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1,2,3);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(AND, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}

	public void testComplexGroups12No34() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(5, 6);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(AND, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}

	public void testComplexGroups134No2() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(3 , 4);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(AND, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}
	
	// 1 OR (2 AND 3)
	public void testMultiGroups1No23() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1,2);
		List<KeyedValue> answerGroup2 = createKeyedValueList(5, 6);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(OR, 1L, 2L, OR, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(AND, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}

	public void testMultiGroups23No1() {
		List<KeyedValue> answerGroup1 = createKeyedValueList();
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		
		Expression expression1 = new Expression(OR, 1L, 2L, OR, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(AND, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), true);
	}

	public void testMultiGroupsNo123() {
		List<KeyedValue> answerGroup1 = createKeyedValueList();
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList();
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(OR, 1L, 2L, OR, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(AND, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}

	public void testMultiGroups2No13() {
		List<KeyedValue> answerGroup1 = createKeyedValueList();
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(3, 4);
		
		Expression expression1 = new Expression(OR, 1L, 2L, OR, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(AND, 2L, null, null, answerGroup2, criteriaGroup2);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2), false);
	}
	
	//1 AND ((2 OR 3) AND (4 OR 5))
	public void testNestedChildren125IsTrue() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> answerGroup3 = createKeyedValueList(5);
		List<KeyedValue> criteriaGroup3 = createKeyedValueList(4, 5);
		
		Expression expression1 = new Expression(OR, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, 3L, AND, answerGroup2, criteriaGroup2);
		Expression expression3 = new Expression(OR, 3L, null, null, answerGroup3, criteriaGroup3);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2, expression3), true);
	}

	public void testNestedChildren125WithReverseMapOrderIsTrue() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> answerGroup3 = createKeyedValueList(5);
		List<KeyedValue> criteriaGroup3 = createKeyedValueList(4, 5);
		
		Expression expression1 = new Expression(OR, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, 3L, AND, answerGroup2, criteriaGroup2);
		Expression expression3 = new Expression(OR, 3L, null, null, answerGroup3, criteriaGroup3);
		
		assertResolveExpression(buildExpressionMap(expression3, expression2, expression1), true);
	}
	
	public void testNestedChildren1236Fails() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(1);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2,3);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> answerGroup3 = createKeyedValueList(6);
		List<KeyedValue> criteriaGroup3 = createKeyedValueList(4, 5);
		
		Expression expression1 = new Expression(OR, 1L, 2L, AND, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, 3L, AND, answerGroup2, criteriaGroup2);
		Expression expression3 = new Expression(OR, 3L, null, null, answerGroup3, criteriaGroup3);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2, expression3), false);
	}
	
	//1 OR ((2 OR 3) AND ((4 OR 5) OR (6 AND 7)))
	public void testSomeCraziness0267ShouldBeTrue() {
		List<KeyedValue> answerGroup1 = createKeyedValueList(0);
		List<KeyedValue> criteriaGroup1 = createKeyedValueList(1);
		List<KeyedValue> answerGroup2 = createKeyedValueList(2);
		List<KeyedValue> criteriaGroup2 = createKeyedValueList(2, 3);
		List<KeyedValue> answerGroup3 = createKeyedValueList(6, 7);
		List<KeyedValue> criteriaGroup3 = createKeyedValueList(4, 5);
		List<KeyedValue> answerGroup4 = createKeyedValueList(6 ,7);
		List<KeyedValue> criteriaGroup4 = createKeyedValueList(6, 7);
		
		
		Expression expression1 = new Expression(OR, 1L, 2L, OR, answerGroup1, criteriaGroup1);
		Expression expression2 = new Expression(OR, 2L, 3L, AND, answerGroup2, criteriaGroup2);
		Expression expression3 = new Expression(OR, 3L, 4L, OR, answerGroup3, criteriaGroup3);
		Expression expression4 = new Expression(AND, 4L, null, null, answerGroup4, criteriaGroup4);
		
		assertResolveExpression(buildExpressionMap(expression1, expression2, expression3, expression4), true);
	}
	
	private void assertResolveExpression(Map<Long,Expression> expressionMap, boolean expectedResolution) {
		boolean result = subject.evaluate(expressionMap);
		for(Map.Entry<Long, Expression> entry : expressionMap.entrySet()) {
			System.out.println(entry.getValue().toString());
		}
		passOrFail(expectedResolution, result);
	}
	
	private void passOrFail(boolean expected, boolean actual) {
		System.out.print("\texpected: " + expected + " | actual: " + actual);
		if(expected == actual) {
			System.out.println("\t======>\tPASS");
		} else {
			System.out.println("\t======>\tFAIL");
			System.out.println("\t^\tFAIL !!!!!!!!!!!!");
			System.out.println("\t^\tFAIL !!!!!!!!!!!!!!!!");
			failFlag = true;
		}
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
		Map<Long, Expression> expressionMap = new LinkedHashMap<>();
		for(Expression expression : expressions) {
			expressionMap.put(expression.groupId, expression);
		}
		return expressionMap;
	}
}
