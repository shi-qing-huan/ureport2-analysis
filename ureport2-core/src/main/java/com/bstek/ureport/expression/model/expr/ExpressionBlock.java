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
package com.bstek.ureport.expression.model.expr;

import java.util.List;

import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

/**
 * 表达式块，包含一个或多个表达式，最后一个表达式的执行结果会被返回
 * 不管表达式单元格中的值是什么，都会先被当成 表达式块 来执行
 * @author Jacky.gao
 * @since 2018年7月13日
 */
@Getter
@Setter
public class ExpressionBlock extends BaseExpression{
	private static final long serialVersionUID = -400528304334443664L;
	/**
	 * 表达式列表
	 */
	private List<Expression> expressionList;
	/**
	 * 返回值的表达式
	 */
	private Expression returnExpression;

	/**
	 * 计算表达式块的值
	 * 遍历计算表达式列表中的表达式，最后一个表达式的执行结果会被返回
	 * 如果存在 返回值的表达式，那么返回值的表达式的执行结果会被返回
	 * @param cell
	 * @param currentCell
	 * @param context
	 * @return
	 */
	@Override
	protected ExpressionData<?> compute(Cell cell, Cell currentCell,Context context) {
		ExpressionData<?> data=null;
		if(expressionList!=null){
			for(Expression expr:expressionList){
				data=expr.execute(cell, currentCell, context);
			}
		}
		if(returnExpression!=null){
			data=returnExpression.execute(cell, currentCell, context);
		}
		return data;
	}
}
