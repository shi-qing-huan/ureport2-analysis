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

import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.expr.BaseExpression;
import com.bstek.ureport.expression.model.expr.ExpressionBlock;
import com.bstek.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

/**
 * else if条件表达式
 * @author Jacky.gao
 * @since 2017年1月16日
 */
@Setter
@Getter
public class ElseIfExpression extends BaseExpression {
	private static final long serialVersionUID = -198920923804292977L;
	/**
	 * 该判断分支的条件列表
	 */
	private ExpressionConditionList conditionList;
	/**
	 * 表达式块
	 */
	private ExpressionBlock expression;

	/**
	 * 执行该分支的表达式块，会在该分支满足条件时执行
	 */
	@Override
	protected ExpressionData<?> compute(Cell cell,Cell currentCell, Context context) {
		return expression.execute(cell, currentCell,context);
	}

	/**
	 * 判断该分支是否满足条件
	 * @param cell
	 * @param currentCell
	 * @param context
	 * @return
	 */
	public boolean conditionsEval(Cell cell,Cell currentCell, Context context) {
		return conditionList.eval(context, cell,currentCell);
	}
}
