/*******************************************************************************
 * Copyright 2017 Bstek
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package com.bstek.ureport.expression.model.expr.ifelse;

import java.util.List;

import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.expr.BaseExpression;
import com.bstek.ureport.expression.model.expr.ExpressionBlock;
import com.bstek.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

/**
 * If表达式，包含一个或多个ElseIf表达式和一个Else表达式，最后一个表达式的执行结果会被返回
 * @author Jacky.gao
 * @since 2017年1月16日
 */
@Getter
@Setter
public class IfExpression extends BaseExpression {
	private static final long serialVersionUID = -514395376408127087L;
	/**
	 * If表达式的条件列表
	 */
	private ExpressionConditionList conditionList;
	/**
	 * If表达式的执行体
	 */
	private ExpressionBlock expression;
	/**
	 * ElseIf表达式列表
	 */
	private List<ElseIfExpression> elseIfExpressions;
	/**
	 * Else表达式，如果存在，那么其执行结果会被返回
	 */
	private ElseExpression elseExpression;

	/**
	 * 按照顺序执行if分支的条件计算，如果条件满足，那么执行对应的表达式块，否则继续执行下一个条件，如果所有条件都不满足，那么执行else表达式块
	 * @param cell
	 * @param currentCell
	 * @param context
	 * @return
	 */
	@Override
	protected ExpressionData<?> compute(Cell cell,Cell currentCell, Context context) {
		if(conditionList!=null){
			boolean result=conditionList.eval(context, cell,currentCell);
			if(result){
				return expression.execute(cell, currentCell,context);
			}
		}
		if(elseIfExpressions!=null){				
			for(ElseIfExpression elseIfExpr:elseIfExpressions){
				if(elseIfExpr.conditionsEval(cell, currentCell,context)){
					return elseIfExpr.execute(cell,currentCell, context);
				}
			}
		}
		if(elseExpression!=null){
			return elseExpression.execute(cell,currentCell, context);
		}
		return new ObjectExpressionData(null);
	}
}
