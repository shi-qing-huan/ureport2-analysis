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

import com.bstek.ureport.build.Context;
import com.bstek.ureport.expression.model.Condition;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.model.Cell;

/**
 * @author Jacky.gao
 * @since 2016年11月18日
 */
public abstract class BaseExpression implements Expression{
	private static final long serialVersionUID = 3853234856946931008L;
	protected String expr;
	
	@Override
	public final ExpressionData<?> execute(Cell cell,Cell currentCell, Context context) {
		ExpressionData<?>  data=compute(cell,currentCell,context);
		return data;
	}
	
	protected abstract ExpressionData<?> compute(Cell cell,Cell currentCell, Context context);


	/**
	 * 根据条件过滤单元格
	 * @param cell 当前单元格
	 * @param context 上下文
	 * @param condition 过滤条件
	 * @param targetCells 要过滤的单元格集合
	 * @return
	 */
	protected List<Cell> filterCells(Cell cell,Context context,Condition condition,List<Cell> targetCells) {
		// 如果没有过滤条件，则直接返回
		if(condition==null){
			return targetCells;
		}
		List<Cell> list=new ArrayList<Cell>();
		// 遍历处理要过滤的单元格
		for(Cell targetCell:targetCells){
			boolean conditionResult=true;
			List<Object> dataList=targetCell.getBindData();
			if(dataList==null){
				// 单元格没有绑定数据，不满足
				conditionResult=false;				
			}else{
				// 遍历绑定数据，如果有一个不满足条件，则视为不满足
				for(Object obj:dataList){
					boolean result=condition.filter(cell,targetCell, obj, context);
					if(!result){
						conditionResult=false;
						break;
					}
				}
			}
			if(!conditionResult){
				continue;
			}
			list.add(targetCell);
		}
		return list;
	}
	
	public void setExpr(String expr) {
		this.expr = expr;
	}
	public String getExpr() {
		return expr;
	}
}
