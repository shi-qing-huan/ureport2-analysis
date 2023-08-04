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
package com.bstek.ureport.utils;

import java.util.ArrayList;
import java.util.List;

import com.bstek.ureport.Utils;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.definition.value.DatasetValue;
import com.bstek.ureport.definition.value.ExpressionValue;
import com.bstek.ureport.definition.value.Value;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.expr.BaseExpression;
import com.bstek.ureport.expression.model.expr.JoinExpression;
import com.bstek.ureport.expression.model.expr.ParenExpression;
import com.bstek.ureport.expression.model.expr.dataset.DatasetExpression;
import com.bstek.ureport.model.Cell;

/**
 * @author Jacky.gao
 * @since 2017年6月12日
 */
public class DataUtils {

	/**
	 *  获取单元格的数据集数据
	 *  1、如果单元格没有任何父格，则获取到的就是该数据集的全部数据
	 *  2、如果只有上父格或左父格，则获取到的就是对应父格的数据集数据
	 *  3、如果同时有上父格和左父格，则获取到的就是交集数据
	 * @param cell
	 * @param context
	 * @param datasetName
	 * @return
	 */
	public static List<?> fetchData(Cell cell, Context context,String datasetName) {
		// 左父格和当前格都是同一个数据集，且左父格有值，才会获取到 左父格
		Cell leftCell=fetchLeftCell(cell, context, datasetName);
		// 上父格和当前格都是同一个数据集，且上父格有值，才会获取到 上父格
		Cell topCell=fetchTopCell(cell, context, datasetName);
		// 如果存在对应父格，就获取父格的绑定数据
		List<Object> leftList=null,topList=null;
		if(leftCell!=null){
			leftList=leftCell.getBindData();
		}
		if(topCell!=null){
			topList=topCell.getBindData();
		}
		//
		if(leftList==null && topList==null){
			// 如果左父格和上父格都不存在，就直接取对应数据集的数据
			List<?> data=context.getDatasetData(datasetName);
			return data;
		}else if(leftList==null){
			// 只有上父格有值，返回上父格的绑定数据
			return topList;
		}else if(topList==null){
			// 只有左父格有值，返回左父格的绑定数据
			return leftList;
		}else{
			// 上、左父格都有值，取交集
			List<Object> list=null;
			Object data=null;
			String prop=null;
			if(leftList.size()>topList.size()){
				list=topList;
				data=leftCell.getData();
				Value value=leftCell.getValue();
				DatasetExpression de=fetchDatasetExpression(value);
				if(de==null){
					throw new ReportComputeException("Unsupport value : "+value);
				}
				prop=de.getProperty();
			}else{
				list=leftList;
				data=topCell.getData();
				Value value=topCell.getValue();
				DatasetExpression de=fetchDatasetExpression(value);
				if(de==null){
					throw new ReportComputeException("Unsupport value : "+value);
				}
				prop=de.getProperty();
			}
			List<Object> result=new ArrayList<Object>();
			for(Object obj:list){
				Object o=Utils.getProperty(obj, prop);
				if((o==null && data==null)){
					result.add(obj);
				}else if(o!=null && o.equals(data)){
					result.add(obj);
				}else if(data!=null && data.equals(o)){
					result.add(obj);
				}
			}
			return result;
		}
	}

	/**
	 * 获取单元格的左父格
	 * 满足以下条件才会返回左父格：
	 * 1、有左父格
	 * 2、左父格的值是数据集表达式
	 * 3、左父格和当前格是一个数据集
	 * 4、左父格有绑定数据
	 * @param cell
	 * @param context
	 * @param datasetName
	 * @return
	 */
	public static Cell fetchLeftCell(Cell cell, Context context,String datasetName){
		Cell targetCell=null;
		// 取当前单元的左父格
		Cell leftCell=cell.getLeftParentCell();
		if(leftCell!=null){
			// 如果有左父格，取左父格的值
			Value leftCellValue=leftCell.getValue();
			// 取左父格的数据集表达式，不是数据集表达式则会返回null
			DatasetExpression leftDSValue=fetchDatasetExpression(leftCellValue);
			// 如果左父格值的是数据集表达式
			if(leftDSValue!=null){
				// 如果左父格和当前格是一个数据集，且左父格有绑定数据，则返回左父格
				String leftDatasetName=leftDSValue.getDatasetName();
				if(leftDatasetName.equals(datasetName)){
					if(leftCell.getBindData()!=null){
						targetCell=leftCell;					
					}
				}
			}
		}
		return targetCell;
	}

	/**
	 * 获取单元格的上父格
	 * 满足以下条件才会返回上父格：
	 * 1、有上父格
	 * 2、上父格的值是数据集表达式
	 * 3、上父格和当前格是一个数据集
	 * 4、上父格有绑定数据
	 * @param cell
	 * @param context
	 * @param datasetName
	 * @return
	 */
	public static Cell fetchTopCell(Cell cell, Context context,String datasetName){
		Cell targetCell=null;
		Cell topCell=cell.getTopParentCell();
		if(topCell!=null){
			Value topCellValue=topCell.getValue();
			DatasetExpression leftDSValue=fetchDatasetExpression(topCellValue);
			if(leftDSValue!=null){				
				String leftDatasetName=leftDSValue.getDatasetName();
				if(leftDatasetName.equals(datasetName)){
					if(topCell.getBindData()!=null){
						targetCell=topCell;			
					}
				}
			}
		}
		return targetCell;
	}
	/**
	 * 获取单元格的数据集表达式
	 * @param value 单元格的值对象
	 * @return 返回单元格的数据集表达式对象
	 */
	public static DatasetExpression fetchDatasetExpression(Value value){
		if(value instanceof ExpressionValue){
			ExpressionValue exprValue=(ExpressionValue)value;
			Expression expr=exprValue.getExpression();
			if(expr instanceof DatasetExpression){
				return (DatasetExpression)expr;
			}else if(expr instanceof ParenExpression){
				ParenExpression parenExpr=(ParenExpression)expr;
				DatasetExpression targetExpr=buildDatasetExpression(parenExpr);
				return targetExpr;
			}else{				
				return null;
			}
		}else if(value instanceof DatasetValue){
			return (DatasetValue)value;
		}
		return null;
	}
	
	private static DatasetExpression buildDatasetExpression(JoinExpression joinExpr){
		List<BaseExpression> expressions=joinExpr.getExpressions();
		for(BaseExpression baseExpr:expressions){
			if(baseExpr instanceof DatasetExpression){
				return (DatasetExpression)baseExpr;
			}else if(baseExpr instanceof JoinExpression){
				JoinExpression childExpr=(JoinExpression)baseExpr;
				return buildDatasetExpression(childExpr);
			}
		}
		return null;
	}
}
