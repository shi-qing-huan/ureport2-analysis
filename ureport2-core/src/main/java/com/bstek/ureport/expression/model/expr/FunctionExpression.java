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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.build.Context;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.expression.ExpressionUtils;
import com.bstek.ureport.expression.function.Function;
import com.bstek.ureport.expression.function.page.PageFunction;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.data.ObjectListExpressionData;
import com.bstek.ureport.expression.model.expr.set.CellExpression;
import com.bstek.ureport.model.Cell;
import lombok.Getter;
import lombok.Setter;

/**
 * 函数表达式，如：sum()、avg()等
 * @author Jacky.gao
 * @since 2016年11月18日
 */
@Getter
@Setter
public class FunctionExpression extends BaseExpression {
	private static final long serialVersionUID = -6981657541024043558L;
	/**
	 * 函数名称
	 */
	private String name;
	/**
	 * 函数参数列表
	 */
	private List<BaseExpression> expressions;
	
	@Override
	public ExpressionData<?> compute(Cell cell,Cell currentCell,Context context) {
		// 获取所有函数
		Map<String, Function> functions=ExpressionUtils.getFunctions();
		// 根据函数名称获取函数
		Function targetFunction=functions.get(name);
		if(targetFunction==null){
			throw new ReportComputeException("Function ["+name+"] not exist.");
		}
		List<ExpressionData<?>> dataList=new ArrayList<ExpressionData<?>>();
		// 获取函数的参数的值，参数可能是常量表达式，但也可能是单元格表达式
		if(expressions!=null){
			for(BaseExpression expr:expressions){
				if(targetFunction instanceof PageFunction){
					ExpressionData<?> data=buildPageExpressionData(expr,cell,currentCell, context);
					dataList.add(data);
				}else{
					ExpressionData<?> data=expr.execute(cell,currentCell, context);
					dataList.add(data);					
				}
			}
		}
		// 执行函数
		Object obj=targetFunction.execute(dataList, context,currentCell);
		if(obj instanceof List){
			return new ObjectListExpressionData((List<?>)obj);
		}
		return new ObjectExpressionData(obj);
	}

	/**
	 * 计算分页函数数据
	 * 分页函数只计算当前页的数据
	 * @param expr
	 * @param cell
	 * @param currentCell
	 * @param context
	 * @return
	 */
	private ExpressionData<?> buildPageExpressionData(Expression expr,Cell cell,Cell currentCell,Context context){
		if(expr instanceof CellExpression){
			CellExpression cellExpr=(CellExpression)expr;
			if(cellExpr.supportPaging()){
				return cellExpr.computePageCells(cell, currentCell, context);
			}else{
				return cellExpr.execute(cell, currentCell, context);
			}
		}else{
			return expr.execute(cell, currentCell, context);
		}
	}

}
