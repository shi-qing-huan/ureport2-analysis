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

import java.util.List;
import java.util.Map;

import com.bstek.ureport.Range;
import com.bstek.ureport.definition.BlankCellInfo;
import com.bstek.ureport.definition.CellDefinition;
import com.bstek.ureport.parser.BuildUtils;

/**
 * @author Jacky.gao
 * @since 2017年2月24日
 */
public class DownCellbuilder {
	private LeftParentCellCreator leftParentCellCreator=new LeftParentCellCreator();
	public void buildParentCell(CellDefinition cell,List<CellDefinition> cells){
		// 获取单元格的、以及所有左侧父单元格的行号范围
		List<Range> rangeList=leftParentCellCreator.buildParentCells(cell);
		// 获取所有子单元格的行号范围,并将子单元格的行号范围添加到行号范围集合中
		Range childRange=buildChildrenCells(cell,rangeList);
		buildChildrenBlankCells(cell,cells,childRange);
		Range rowRange=buildRowRange(rangeList);
		buildRowsBlankCells(cell,cells,rowRange);
		int start=rowRange.getStart(),end=rowRange.getEnd();
		int rowNumberStart=cell.getRowNumber(),rowNumberEnd=cell.getRowNumber();
		int rowSpan=cell.getRowSpan();
		if(rowSpan>0){
			rowNumberEnd+=rowSpan-1;
		}
		int rangeStart=0,rangeEnd=0;
		if(start!=-1){
			rangeStart=start-rowNumberStart;
		}
		if(end>rowNumberStart && end>rowNumberEnd){
			rangeEnd=end-rowNumberStart;
		}else{
			rangeEnd=rowNumberEnd-rowNumberStart;
		}
		Range duplicateRange=new Range(rangeStart,rangeEnd);
		cell.setDuplicateRange(duplicateRange);
	}
	

	private void buildRowsBlankCells(CellDefinition cell,List<CellDefinition> cells,Range range){
		Map<String,BlankCellInfo> blankCellNamesMap=cell.getNewBlankCellsMap();
		int start=range.getStart(),end=range.getEnd();
		int nextEnd=0;
		for(int i=start;i<=end;i++){
			for(CellDefinition cellDef:cells){
				String name=cellDef.getName();
				if(cellPrcessed(cell, name)){
					continue;
				}
				int rowNumber=cellDef.getRowNumber();
				if(rowNumber==i){
					int offset=rowNumber-cell.getRowNumber();
					blankCellNamesMap.put(name, new BlankCellInfo(offset,cellDef.getRowSpan(),false));
				}else if(rowNumber<i){
					int endRowNumber=BuildUtils.buildRowNumberEnd(cellDef, rowNumber);
					if(endRowNumber>=i){
						int offset=rowNumber-cell.getRowNumber();
						blankCellNamesMap.put(name, new BlankCellInfo(offset,cellDef.getRowSpan(),false));
						if(i>end && i>nextEnd){
							nextEnd=i;
						}
					}
				}
			}
		}
		if(nextEnd>end){
			buildRowsBlankCells(cell,cells,new Range(end,nextEnd));
		}
	}
	

	private Range buildRowRange(List<Range> rangeList){
		Range rowRange=new Range();
		for(Range range:rangeList){
			for(int i=range.getStart();i<=range.getEnd();i++){
				if(rowRange.getStart()==-1 || i<rowRange.getStart()){
					rowRange.setStart(i);
				}
				if(rowRange.getEnd()<i){
					rowRange.setEnd(i);
				}
			}
		}
		return rowRange;
	}


	/**
	 * 获取所有子单元格的行号范围,并将子单元格的行号范围添加到行号范围集合中
	 * @param cell 单元格
	 * @param rangeList 行号范围集合
	 * @return 所有子单元格的行号范围
	 */
	private Range buildChildrenCells(CellDefinition cell,List<Range> rangeList){
		Range range=new Range();
		List<CellDefinition> rowChildrenCells=cell.getRowChildrenCells();
		for(CellDefinition childCell:rowChildrenCells){
			cell.getNewCellNames().add(childCell.getName());
			int rowNumber=childCell.getRowNumber();
			int endRowNumber=BuildUtils.buildRowNumberEnd(childCell, rowNumber);
			rangeList.add(new Range(rowNumber,endRowNumber));
			if(endRowNumber>range.getEnd()){
				range.setEnd(endRowNumber);
			}
			if(range.getStart()==-1 || rowNumber<range.getStart()){
				range.setStart(rowNumber);
			}
		}
		return range;
	}

	/**
	 * 处理子单元格行号范围内的所有单元格的空白单元格
	 * @param cell
	 * @param cells
	 * @param childRange
	 */
	private void buildChildrenBlankCells(CellDefinition cell,List<CellDefinition> cells,Range childRange){
		// 获取单元格行号范围
		int startRowNumber=cell.getRowNumber();
		int endRowNumber=BuildUtils.buildRowNumberEnd(cell, startRowNumber);
		// 根据所有子单元格的行号范围,更新起始行号和结束行
		int start=childRange.getStart(),end=childRange.getEnd();
		if(start!=-1 && start<startRowNumber){
			startRowNumber=start;
		}
		if(end>endRowNumber){
			endRowNumber=end;
		}
		// 获取单元格的空白单元格集合
		Map<String,BlankCellInfo> blankCellNamesMap=cell.getNewBlankCellsMap();
		// 遍历行号范围内的所有行
		for(int i=startRowNumber;i<=endRowNumber;i++){
			// 遍历所有单元格
			for(CellDefinition c : cells){
				// 如果单元格不在当前行,则跳过
				if(c.getRowNumber()!=i){
					continue;
				}
				// 如果单元格是当前单元格,则跳过
				if(c.equals(cell)){
					continue;
				}
				// 到这里说明找到了在该行的单元格
				// 检查该单元格是否已经处理过（在leftParentCellCreator.buildParentCells中处理过）
				String name=c.getName();
				boolean contain=cellPrcessed(cell,name);
				if(contain){
					continue;
				}
				// 未处理过的单元格,则添加到空白单元格集合中
				int offset=c.getRowNumber()-cell.getRowNumber();
				blankCellNamesMap.put(name, new BlankCellInfo(offset,c.getRowSpan(),false));
			}			
		}
	}
	
	private boolean cellPrcessed(CellDefinition cell,String name){
		List<String> newCellNames=cell.getNewCellNames();
		List<String> increaseCellNames=cell.getIncreaseSpanCellNames();
		Map<String,BlankCellInfo> blankCellNamesMap=cell.getNewBlankCellsMap();
		boolean contain=false;
		if(cell.getName().equals(name)){
			contain=true;			
		}
		if(newCellNames.contains(name)){
			contain=true;
		}
		if(increaseCellNames.contains(name)){
			contain=true;
		}
		if(blankCellNamesMap.containsKey(name)){
			contain=true;					
		}
		return contain;
	}
}
