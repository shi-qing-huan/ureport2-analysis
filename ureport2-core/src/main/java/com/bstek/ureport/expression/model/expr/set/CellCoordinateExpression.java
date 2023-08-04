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
package com.bstek.ureport.expression.model.expr.set;

import java.util.ArrayList;
import java.util.List;

import com.bstek.ureport.Utils;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.expression.model.Condition;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.data.ObjectListExpressionData;
import com.bstek.ureport.model.Cell;
import com.bstek.ureport.model.Column;
import com.bstek.ureport.model.Row;


/**
 * 单元格坐标的表达式
 * 格式为 单元格名称[左边坐标:展开次数,...;上边坐标:展开次数,...]{条件}, 其中左父格坐标和上父格坐标可以为零个、一个或多个，多个时用逗号分隔
 * 如 C4[A4:2,B4:1]{C4>11} 表示取A4展开后的第二项 的 B4的第一项 的 C4的值，且C4的值必须大于11，如果不满足条件则返回空（多个值则返回满足条件的部分）
 * @author Jacky.gao
 * @since 2017年1月1日
 */
public class CellCoordinateExpression extends CellExpression {
	private static final long serialVersionUID = 4132183845260722859L;
	/**
	 * 条件处理器
	 */
	private Condition condition;
	/**
	 * 左父格坐标集合
	 */
	private CellCoordinateSet leftCoordinate;
	/**
	 * 上父格坐标集合
	 */
	private CellCoordinateSet topCoordinate;

	public CellCoordinateExpression(String cellName,CellCoordinateSet leftCoordinate) {
		super(cellName);
		this.leftCoordinate=leftCoordinate;
	}
	public CellCoordinateExpression(String cellName,CellCoordinateSet leftCoordinate,Condition condition) {
		super(cellName);
		this.leftCoordinate=leftCoordinate;
		this.condition=condition;
	}
	public CellCoordinateExpression(String cellName,CellCoordinateSet leftCoordinate,CellCoordinateSet topCoordinate) {
		super(cellName);
		this.leftCoordinate=leftCoordinate;
		this.topCoordinate=topCoordinate;
	}
	public CellCoordinateExpression(String cellName,CellCoordinateSet leftCoordinate,CellCoordinateSet topCoordinate,Condition condition) {
		super(cellName);
		this.leftCoordinate=leftCoordinate;
		this.topCoordinate=topCoordinate;
		this.condition=condition;
	}
	@Override
	public boolean supportPaging(){
		return false;
	}

	@Override
	protected ExpressionData<?> compute(Cell cell,Cell currentCell, Context context) {
		/*
		 * 检查引用单元格是否已经处理过，没有则调用报表构建器构建单元格
		 * 这里入参中{需要处理的单元}直接传入null，这会让构建器自动从上下文中获取下一批等待处理的单元格，这可能不是引用单元格，当不是引用单元格时，会继续当前循环
		 * 所以直到引用单元格被处理过后，才会跳出循环 TODO 这里是否可以优化
		 */
		while(!context.isCellPocessed(cellName)){
			context.getReportBuilder().buildCell(context, null);
		}
		// 构建引用单元格在左父格坐标限制下的单元格集合
		List<Cell> leftCellList = buildLeftCells(cell, context);
		// 构建引用单元格在上父格坐标限制下的单元格集合
		List<Cell> topCellList=buildTopCells(cell, context);
		List<Object> list=new ArrayList<Object>();
		if(leftCellList==null){
			if(topCellList!=null){
				// 只有上坐标位置有对应的单元格，那么将引用单元格在上坐标限制下的单元格集合过滤后作为结果集
				topCellList=filterCells(cell,context, condition, topCellList);
				for(Cell c:topCellList){
					list.add(c.getData());
				}
			}else{
				// 左、上坐标位置都没有对应的单元格，直接返回引用单元格的所有数据
				List<Cell> cells=context.getReport().getCellsMap().get(cellName);
				if(cells==null){
					throw new ReportComputeException("Cell ["+cellName+"] not exist.");
				}
				topCellList=filterCells(cell,context, condition, cells);
				for(Cell c:cells){
					list.add(c.getData());
				}
			}
		}else{
			if(topCellList!=null){
				// 左、上坐标位置都有对应的单元格，取交集后过滤后作为结果集
				leftCellList=filterCells(cell,context, condition, leftCellList);
				topCellList=filterCells(cell,context, condition, topCellList);
				for(Cell c:topCellList){
					if(leftCellList.contains(c)){
						list.add(c.getData());
					}
				}
			}else{
				// 只有左坐标位置有对应的单元格，那么将引用单元格在左坐标限制下的单元格集合过滤后作为结果集
				leftCellList=filterCells(cell,context, condition, leftCellList);
				for(Cell c:leftCellList){
					list.add(c.getData());
				}
			}
		}
		if(list.size()==1){
			return new ObjectExpressionData(list.get(0));
		}else{
			return new ObjectListExpressionData(list);
		}
	}


	/**
	 * 构建引用单元格在左父格坐标限制下的单元格集合
	 * 如 C2[A2:-1]，C2即为引用单元格，最终的结果就是C2在坐标A2:-1下的单元格集合
	 * @param cell 当前单元格
	 * @param context 上下文
	 * @return 引用单元格在坐标位置下对应的单元格集合
	 */
	private List<Cell> buildLeftCells(Cell cell, Context context) {
		if(leftCoordinate==null){
			return null;
		}
		List<Cell> cellList=null;
		// 坐标单元格
		Cell targetLeftCell=null;
		Row row=cell.getRow();
		int rowNumber=row.getRowNumber();
		// 获取左父格坐标集合
		List<CellCoordinate> leftCoordinates=leftCoordinate.getCellCoordinates();
		// 遍历左父格坐标集合
		for(CellCoordinate coordinate:leftCoordinates){
			// 获取坐标中单元格名称
			String name=coordinate.getCellName();
			/*
			 * 检查引用单元格是否已经处理过，没有则调用报表构建器构建单元格
			 * 这里入参中{需要处理的单元}直接传入null，这会让构建器自动从上下文中获取下一批等待处理的单元格，这可能不是引用单元格，当不是引用单元格时，会继续当前循环
			 * 所以直到引用单元格被处理过后，才会跳出循环
			 */
			while(!context.isCellPocessed(name)){
				context.getReportBuilder().buildCell(context, null);
			}
			if(targetLeftCell==null){
				// 第一次循环
				if(coordinate.getCoordinateType().equals(CoordinateType.relative)){
					// 如果是相对坐标，调用工具类获取 坐标单元格列表
					cellList=Utils.fetchTargetCells(cell, context, name);
				}else{
					// 如果是绝对坐标，直接从报表中获取 坐标单元格列表
					cellList=context.getReport().getCellsMap().get(name);
				}
			}else{
				// 非第一次循环，从坐标单元格的子单元格中获取 下一级坐标单元格
				cellList=targetLeftCell.getRowChildrenCellsMap().get(name);
			}
			// 目标位置
			int position=coordinate.getPosition();
			/*
			 * 如果position等于0，表示要获取与当前单元格所在行相同的单元格。
			 * 在这种情况下，会遍历cellList中的每个子单元格，检查其所在的行是否与当前单元格的行相同。
			 * 如果找到了相同行的单元格，则将其赋值给targetLeftCell，并跳出循环。
			 */
			if(position==0){
				for(Cell childCell:cellList){
					Row childRow=childCell.getRow();
					if(row==childRow){
						targetLeftCell=childCell;
						break;
					}
					int rowSpan=childCell.getRowSpan();
					// 如果单元格的行跨度大于0，表示该单元格占用了多行，需要计算出该单元格占用的行号范围，然后检查当前单元格是否在该范围内
					if(rowSpan>0){
						int childRowNumberStart=childRow.getRowNumber();
						int childRowNumberEnd=childRowNumberStart+rowSpan-1;
						if(childRowNumberStart<=rowNumber && childRowNumberEnd>=rowNumber){
							targetLeftCell=childCell;
							break;
						}
					}
				}
			}
			// 如果position大于0，表示要获取坐标单元格的第position个展开单元格
			if(position>0){
				targetLeftCell=cellList.get(position-1);
			}
			// 如果position小于0，表示要获取的位置为相对于当前单元格位置，在坐标单元格上移position个位置后的单元格
			if(position<0){
				boolean reverse=coordinate.isReverse();
				int cellSize=cellList.size();
				if(reverse){
					int newPosition=cellSize-position;
					if(newPosition>=cellSize){
						newPosition=cellSize-1;
					}
					targetLeftCell=cellList.get(newPosition);
				}else{
					int index=0;
					// 遍历坐标单元格的集合，找到和当前单元格在同一行的单元格
					for(int i=0;i<cellSize;i++){
						Cell childCell=cellList.get(i);
						if(childCell.getRow()==cell.getRow()){
							index=i;
							break;
						}
						int rowSpan=childCell.getRowSpan();
						if(rowSpan>1){
							// 如果单元格的行跨度大于1，表示该单元格占用了多行，需要计算出该单元格占用的行号范围，然后检查当前单元格是否在该范围内
							int rowNum=childCell.getRow().getRowNumber();
							int start=rowNum,end=rowNum+rowSpan-1;
							if(rowNumber>=start && rowNumber<=end){
								index=i;
								break;
							}
						}
					}
					// 计算目标位置，如果小于0，则取单元格集合的第一个单元格，如果大于等于单元格集合的大小，则取最后一个单元格
					int newPosition=index+position;
					if(newPosition<0){
						newPosition=0;
					}
					if(newPosition>=cellSize){
						newPosition=cellSize-1;
					}
					targetLeftCell=cellList.get(newPosition);
				}
			}
		}
		// 从 坐标单元格的子单元格集合中，获取引用单元格
 		List<Cell> leftCellList=targetLeftCell.getRowChildrenCellsMap().get(cellName);
		return leftCellList;
	}

	/**
	 * 构建上方单元格
	 * 逻辑分析参考buildLeftCells
	 * @param cell
	 * @param context
	 * @return
	 */
	private List<Cell> buildTopCells(Cell cell, Context context) {
		if(topCoordinate==null){
			return null;
		}
		List<Cell> cellList=null;
		Cell targetTopCell=null;
		Column col=cell.getColumn();
		int colNumber=col.getColumnNumber();
		List<CellCoordinate> topCoordinates=topCoordinate.getCellCoordinates();
		for(CellCoordinate coordinate:topCoordinates){
			String name=coordinate.getCellName();
			while(!context.isCellPocessed(name)){
				context.getReportBuilder().buildCell(context, null);
			}
			if(cellList==null){
				if(coordinate.getCoordinateType().equals(CoordinateType.relative)){
					cellList=Utils.fetchTargetCells(cell, context, name);
				}else{	
					cellList=context.getReport().getCellsMap().get(name);
				}			
			}else{
				cellList=targetTopCell.getColumnChildrenCellsMap().get(name);
			}
			int position=coordinate.getPosition();
			if(position==0){
				for(Cell childCell:cellList){
					Column childCol=childCell.getColumn();
					if(col==childCol){
						targetTopCell=childCell;
						break;
					}
					int colSpan=childCell.getColSpan();
					if(colSpan>0){
						int childColNumberStart=childCol.getColumnNumber();
						int childColNumberEnd=childColNumberStart+colSpan-1;
						if(childColNumberStart<=colNumber && childColNumberEnd>=colNumber){
							targetTopCell=childCell;
							break;
						}
					}
				}
			}else{
				if(position>0){
					targetTopCell=cellList.get(position-1);
				}else if(position<0){
					boolean reverse=coordinate.isReverse();
					int cellSize=cellList.size();
					if(reverse){
						int newPosition=cellSize-position;
						if(newPosition>=cellSize){
							newPosition=cellSize-1;
						}
						targetTopCell=cellList.get(newPosition);
					}else{
						int index=0;
						for(int i=0;i<cellSize;i++){
							Cell childCell=cellList.get(i);
							if(childCell.getColumn()==cell.getColumn()){
								index=i;
								break;
							}
							int colSpan=childCell.getColSpan();
							if(colSpan>1){
								int colNum=childCell.getColumn().getColumnNumber();
								int start=colNum,end=colNum+colSpan-1;
								if(colNumber>=start && colNumber<=end){
									index=i;
									break;
								}								
							}
						}
						int newPosition=index+position;
						if(newPosition<0){
							newPosition=0;
						}
						if(newPosition>=cellSize){
							newPosition=cellSize-1;
						}
						targetTopCell=cellList.get(newPosition);
					}
				}
			}
		}
		List<Cell> topCellList=targetTopCell.getColumnChildrenCellsMap().get(cellName);
		return topCellList;
	}
}
