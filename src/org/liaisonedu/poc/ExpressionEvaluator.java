package org.liaisonedu.poc;

import static org.liaisonedu.poc.Operator.AND;
import static org.liaisonedu.poc.Operator.AND_NOT;
import static org.liaisonedu.poc.Operator.OR;
import static org.liaisonedu.poc.Operator.OR_NOT;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sun.jvm.hotspot.utilities.Assert;

public class ExpressionEvaluator {
	
	public boolean evaluate(Map<Long, Expression> expressionMap) {
		Assert.that(expressionMap != null, "Expression map should not be null");
		boolean result = false;
		Long rootGroupId = resolveExpressions(expressionMap);
		Expression rootExpression = expressionMap.get(rootGroupId);
		if(rootExpression.childGroupId != null && expressionMap.containsKey(rootExpression.childGroupId)) {
			result = evaluateChildren(rootExpression, expressionMap.get(rootExpression.childGroupId), expressionMap);
		} else {
			result = rootExpression.isTrue;
		}
		return result;
	}
	
	private boolean evaluateChildren(Expression workingExpression, Expression childExpression, Map<Long, Expression> expressionMap) {
		boolean shortCircuit = false;
		boolean evaluationResult = false;
		if ((workingExpression.isTrue && OR.equals(workingExpression.childGroupOperator))
				|| (!workingExpression.isTrue && AND.equals(workingExpression.childGroupOperator))) {
			shortCircuit = true;
			evaluationResult = workingExpression.isTrue;
		}
		if (!shortCircuit) {
			boolean isChildEvaluationTrue = childExpression.isTrue;
			if (childExpression.childGroupId != null) {
				isChildEvaluationTrue = evaluateChildren(childExpression, expressionMap.get(childExpression.childGroupId), expressionMap);
			}
			switch (workingExpression.childGroupOperator) {
				case OR:
					evaluationResult = workingExpression.isTrue || isChildEvaluationTrue;
					break;
				case AND:
					evaluationResult = workingExpression.isTrue && isChildEvaluationTrue;
					break;
				case AND_NOT:
					evaluationResult = workingExpression.isTrue && !isChildEvaluationTrue;
					break;
				case OR_NOT:
					evaluationResult = workingExpression.isTrue || !isChildEvaluationTrue;
			}
		}
		return evaluationResult;
	}
	
	/**
	 * Each expression must be resolved prior to being evaluated in its designated groups.
	 * <p>
	 * I think the idea is that we look up the configuration for the block and end up with a map looking something like the input argument to this function.
	 *
	 * @param groupIdExpressionMap
	 * @return the parent expression groupid
	 */
	private Long resolveExpressions(Map<Long, Expression> groupIdExpressionMap) {
		Map<Long, Expression> groupIdExpressionResolutionMap = new HashMap<>();
		Long rootGroupId = null;
		for (Entry<Long, Expression> expressionEntry : groupIdExpressionMap.entrySet()) {
			//resolve any child groups before this can be evaluated
			//If this first group happens to have a parent, it will get set eventually once its actual parent calls this method.
			resolveGroup(groupIdExpressionResolutionMap, groupIdExpressionMap, expressionEntry.getValue(), null);
			if (groupIdExpressionResolutionMap.get(expressionEntry.getKey()).parentGroupId == null) {
				rootGroupId = groupIdExpressionResolutionMap.get(expressionEntry.getKey()).groupId;
			}
		}
		return rootGroupId;
	}
	
	/**
	 * This is responsible for walking the hierarchy of objects to resolve their individual expressions.
	 * <p>
	 * This also sets the expressions parent relationship.
	 *
	 * @param groupIdExpressionResolutionMap
	 * @param groupIdExpressionMap
	 * @param workingExpression
	 * @param parentGroupId
	 */
	private void resolveGroup(Map<Long, Expression> groupIdExpressionResolutionMap, Map<Long, Expression> groupIdExpressionMap, Expression workingExpression, Long parentGroupId) {
		if (parentGroupId != null) {
			workingExpression.parentGroupId = parentGroupId;
		}
		//Avoid mapping something more than once.
		//If this expression is not already resolved, resolve it.
		if (!groupIdExpressionResolutionMap.containsKey(workingExpression.groupId)) {
			groupIdExpressionResolutionMap.put(workingExpression.groupId, resolveExpression(workingExpression));
		}
		//If the parent is resolved then resolve it's children.  Sometimes children get resolved before their parents, in which case
		if (hasUnresolvedChildGroup(groupIdExpressionResolutionMap, workingExpression)) {
			//iterate this method until there are no more children to operate on.
			resolveGroup(groupIdExpressionResolutionMap, groupIdExpressionMap, groupIdExpressionMap.get(workingExpression.childGroupId), workingExpression.groupId);
		} else if (hasResolvedChildWithoutParentId(groupIdExpressionResolutionMap, workingExpression)) {
			groupIdExpressionResolutionMap.get(workingExpression.childGroupId).parentGroupId = workingExpression.groupId;
		}
	}
	
	private boolean hasUnresolvedChildGroup(Map<Long, Expression> groupIdExpressionResolutionMap, Expression parentExpression) {
		return parentExpression.childGroupId != null
					&& !groupIdExpressionResolutionMap.containsKey(parentExpression.childGroupId);
	}

	private boolean hasResolvedChildWithoutParentId(Map<Long, Expression> groupIdExpressionResolutionMap, Expression parentExpression) {
		return parentExpression.childGroupId != null
					&& groupIdExpressionResolutionMap.containsKey(parentExpression.childGroupId)
					&& groupIdExpressionResolutionMap.get(parentExpression.childGroupId).parentGroupId == null;
	}
	
	private Expression resolveExpression(Expression expression) {
		boolean isExpressionTrue = false;
		for (KeyedValue criteria : expression.criteria) {
			isExpressionTrue = evaluateCriteriaToValues(criteria, expression.values);
			//If one of the expressions does not match using the AND operator, we can safely short circuit the loop.
			//If one of the expressions is true and we're using the OR operator, we can safely short circuit this loop.
			if (canShortCircuit(isExpressionTrue, expression.operator)) {
				break;
			}
		}
		expression.isTrue = isExpressionTrue && !(AND_NOT.equals(expression.operator) || OR_NOT.equals(expression.operator));
		return expression;
	}
	
	private boolean canShortCircuit(boolean isExpressionTrue, Operator operator) {
		return (AND.equals(operator) && !isExpressionTrue)
				|| (OR.equals(operator) && isExpressionTrue)
				|| (AND_NOT.equals(operator) && isExpressionTrue)
				|| (OR_NOT.equals(operator) && !isExpressionTrue);
	}
	
	private boolean evaluateCriteriaToValues(KeyedValue criteria, List<KeyedValue> testValueList) {
		boolean isMatched = false;
		for (KeyedValue testValue : testValueList) {
			if (criteria.key == testValue.key) {
				isMatched = true;
				break;
			}
		}
		return isMatched;
	}
}

/**
 * Expression is a group of values are evaluated against criteria linked by single groupId.  All values are evaluated against
 * the criteria using the same operation in the same group.
 * <p>
 * An expression can link multiple expressions together via the childGroupId and childGroupOperator fields.  Those expressions will
 * be evaluated independently, but they will eventually use the hierarchy to build a final decision.
 * <p>
 * This thing is essentially the table we will need to create to support the kind of logic they want to use.
 */
class Expression {
	Operator operator;
	Long groupId;
	//Transient field, does not need to be in the database.  This is used to determine the correct order of operations, a null parentGroupId indicates the root expression.
	Long parentGroupId;
	Long childGroupId;
	Operator childGroupOperator;
	List<KeyedValue> values;
	List<KeyedValue> criteria;
	boolean isTrue;
	
	Expression(Operator operator, Long groupId, Long childGroupId, Operator childGroupOperator, List<KeyedValue> values, List<KeyedValue> criteria) {
		this.operator = operator;
		this.groupId = groupId;
		/**
		 * This expressions result can be evaluated against the value of another group by configuring childGroupId and childGroupOperator
		 *
		 * The expression with the same groupId as this expressions childGroupId will be resolved against this Expressions result.
		 */
		this.childGroupId = childGroupId;
		this.childGroupOperator = childGroupOperator;
		this.values = values;
		this.criteria = criteria;
		this.isTrue = false;
	}
	
	@Override
	public String toString() {
		return "answers" + values.toString()
				+ " operator [" + operator
				+ "] with criteria " + criteria.toString()
				+ " groupId [" + groupId + "]"
				+ " parentGroupId [" + parentGroupId
				+ "] childGroupId ["+ childGroupId
			    + "] childOperator [" + childGroupOperator + "]"
				+ " evaluates to [" + isTrue + "]";
	}
}

enum Operator {
	AND, OR, AND_NOT, OR_NOT
}

class KeyedValue {
	long key;
	
	KeyedValue(long key) {
		this.key = key;
	}
	
	public String toString() {
		return Long.toString(key);
	}
}