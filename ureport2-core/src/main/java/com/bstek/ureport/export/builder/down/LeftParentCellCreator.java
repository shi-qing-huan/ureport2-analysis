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
package com.bstek.ureport.export.builder.down;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.Range;
import com.bstek.ureport.definition.BlankCellInfo;
import com.bstek.ureport.definition.CellDefinition;
import com.bstek.ureport.parser.BuildUtils;

/**
 * @author Jacky.gao
 * @since 2017年2月27日
 */
public class LeftParentCellCreator {
	public List<Range> buildParentCells(CellDefinition cell){
		// 获取所有子单元格的行号范围
		Range childRange=buildChildrenCellRange(cell);
		// 获取单元格的所有直接或间接的 左父单元格 集合
		List<CellDefinition> parentCells=new ArrayList<CellDefinition>();
		collectParentCells(cell,parentCells);
		return buildParents(cell, parentCells, childRange);
	}

	/**
	 * 获取单元格的所有直接或间接 左父单元格
	 * @param cell 单元格
	 * @param parentCells 左父单元格集合，初始传入为空
	 */
	private void collectParentCells(CellDefinition cell,List<CellDefinition> parentCells){
		// 获取左侧父单元格
		CellDefinition leftParentCell=cell.getLeftParentCell();
		// 如果左侧父单元格为空，则不处理
		if(leftParentCell==null){
			return;
		}
		// 将左侧父单元格添加到父单元格集合中
		parentCells.add(leftParentCell);
		// 递归获取左侧父单元格的父单元格
		collectParentCells(leftParentCell,parentCells);
	}

	/**
	 * 获取主格和所有父格的行号范围，同时构建主格的空白单元格集合
	 * @param mainCell 单元格
	 * @param parentCells 左父单元格集合(直接或间接)
	 * @param childRange 子单元格的行号范围
	 * @return
	 */
	private List<Range> buildParents(CellDefinition mainCell,List<CellDefinition> parentCells,Range childRange){
		List<Range> rangeList=new ArrayList<>();
		// 添加主单元格的行号范围
		int rowNumberStart=mainCell.getRowNumber();
		int rowNumberEnd=BuildUtils.buildRowNumberEnd(mainCell, rowNumberStart);
		rangeList.add(new Range(rowNumberStart,rowNumberEnd));
		
		int start=childRange.getStart(),end=childRange.getEnd();
		Map<String,BlankCellInfo> newBlankCellsMap=mainCell.getNewBlankCellsMap();
		boolean increase=true;
		// 遍历所有父格
		for(CellDefinition parentCell:parentCells){
			// 获取父单元格行号范围
			String parentCellName=parentCell.getName();
			int parentRowNumberStart=parentCell.getRowNumber();
			int parentRowNumberEnd=BuildUtils.buildRowNumberEnd(parentCell,parentRowNumberStart);
			// 开始行距离主单元格的行数
			int offset=parentRowNumberStart-rowNumberStart;
			int parentRowSpan=parentCell.getRowSpan();
			// 是否 父格 和 主格 的行号都超出了所有子格的行号范围
			boolean isOut=assertOut(parentCell, mainCell, childRange);
			if(isOut){
				increase=false;
				// 检查父格的所有 父格 是否在父格的行号范围内，是则返回true
				boolean doBlank=assertDoBlank(parentCell.getLeftParentCell(), parentCell, mainCell, childRange);
				if(doBlank){
					// 如果是，则将父格添加到新的空白单元格集合中，并传入开始行距离主单元格的行数、父格的行数跨度、是否是左父格
					newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset,parentRowSpan,true));
					// 将父格的行号范围添加到行号范围集合中
					rangeList.add(new Range(parentRowNumberStart,parentRowNumberEnd));
				}
				continue;
			}
			// 父格的开始或结束行有一个在子格范围内，则添加到空白单元格集合中
			if((start!=-1 && start<parentRowNumberStart) || end>parentRowNumberEnd){
				newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset,parentRowSpan,true));
				rangeList.add(new Range(parentRowNumberStart,parentRowNumberEnd));
				increase=false;
				continue;
			}
			// 父格的开始和结束行都在子格范围内
			if(increase){
				// 目前还没出现 父格 和 主格 的行号都超出了所有子格的行号范围 或 父格的开始或结束行有一个在子格范围内 的情况
				// 则将父格添加到主格的增加行号集合中
				mainCell.getIncreaseSpanCellNames().add(parentCellName);				
			}else{
				// 否则 将父格添加到新的空白单元格集合中，并传入开始行距离主单元格的行数、父格的行数跨度、是否是左父格
				newBlankCellsMap.put(parentCellName, new BlankCellInfo(offset,parentRowSpan,true));
				rangeList.add(new Range(parentRowNumberStart,parentRowNumberEnd));
			}
		}
		return rangeList;
	}

	/**
	 * 检查父格的所有 父格 是否在父格的行号范围内，是则返回true
	 * @param nextParentCell 父父格
	 * @param parentCell 父格
	 * @param mainCell 主格
	 * @param childRange 所有子格的行号范围
	 * @return
	 */
	private boolean assertDoBlank(CellDefinition nextParentCell,CellDefinition parentCell,CellDefinition mainCell,Range childRange){
		if(nextParentCell==null){
			return false;
		}
		boolean isOut=assertOut(nextParentCell, mainCell, childRange);
		if(isOut){
			return assertDoBlank(nextParentCell.getLeftParentCell(), parentCell, mainCell, childRange);
		}
		int start=parentCell.getRowNumber(),end=BuildUtils.buildRowNumberEnd(parentCell, start);
		int nextStart=nextParentCell.getRowNumber();
		if(nextStart<=end){
			return true;
		}
		return assertDoBlank(nextParentCell.getLeftParentCell(), parentCell, mainCell, childRange);
	}

	/**
	 * 是否 父格 和 主格 的行号都超出了所有子格的行号范围
	 * @param parentCell
	 * @param mainCell
	 * @param childRange
	 * @return
	 */
	private boolean assertOut(CellDefinition parentCell,CellDefinition mainCell,Range childRange){
		// 父格的开始行和结束行
		int start=parentCell.getRowNumber(),end=BuildUtils.buildRowNumberEnd(parentCell, start);
		// 所有子格的开始行和结束行
		int rangeStart=childRange.getStart(),rangeEnd=childRange.getEnd();
		if(rangeStart!=-1){
			// 父格的开始行 或 结束行 至少有一个在子格的行号范围内，则返回false
			if((start>=rangeStart && start<=rangeEnd) || (end>=rangeStart && end<=rangeEnd)){
				return false;
			}
		}
		// 主格的开始行和结束行
		int rowStart=mainCell.getRowNumber(),rowEnd=BuildUtils.buildRowNumberEnd(mainCell, rowStart);
		// 主格的开始行 或 结束行 至少有一个在主格的行号范围内，则返回false
		if((start>=rowStart && start<=rowEnd) || (end>=rowStart && end<=rowEnd) || (start<=rowStart && end>=rowEnd)){
			return false;
		}
		// 父格、主格的开始行 和 结束行 都在子格的行号范围外，则返回true
		return true;
	}

	/**
	 * 计算子单元格的行号范围
	 * @param mainCell
	 * @return
	 */
	private Range buildChildrenCellRange(CellDefinition mainCell){
		Range range=new Range();
		// 获取所有子单元格
		List<CellDefinition> childrenCells=mainCell.getRowChildrenCells();
		// 计算子单元格的行号范围
		for(CellDefinition childCell:childrenCells){
			int childRowNumberStart=childCell.getRowNumber();
			int childRowSpan=childCell.getRowSpan();
			childRowSpan=childRowSpan>0 ? childRowSpan-1 :childRowSpan;
			int childRowNumberEnd=childRowNumberStart+childRowSpan;
			if(range.getStart()==-1 || childRowNumberStart<range.getStart()){
				range.setStart(childRowNumberStart);
			}
			if(childRowNumberEnd>range.getEnd()){
				range.setEnd(childRowNumberEnd);
			}
		}
		return range;
	}
}
