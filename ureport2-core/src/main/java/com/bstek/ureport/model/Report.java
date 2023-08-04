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
package com.bstek.ureport.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.build.Context;
import com.bstek.ureport.build.paging.Page;
import com.bstek.ureport.build.paging.PagingBuilder;
import com.bstek.ureport.definition.Band;
import com.bstek.ureport.definition.ConditionPropertyItem;
import com.bstek.ureport.definition.HeaderFooterDefinition;
import com.bstek.ureport.definition.Paper;
import lombok.Getter;
import lombok.Setter;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
@Getter
@Setter
public class Report {
	/**
	 * 页面纸张定义
	 */
	private Paper paper;
	/**
	 * 页眉
	 */
	private HeaderFooterDefinition header;
	/**
	 * 页脚
	 */
	private HeaderFooterDefinition footer;
	/**
	 * 根单元格
	 */
	private Cell rootCell;
	/**
	 * 上下文对象
	 */
	private Context context;
	/**
	 * 所有行
	 */
	private List<Row> rows;
	/**
	 * 重复表头行
	 */
	private List<Row> headerRepeatRows=new ArrayList<Row>();
	/**
	 * 重复表尾行
	 */
	private List<Row> footerRepeatRows=new ArrayList<Row>();
	/**
	 * 标题行
	 */
	private List<Row> titleRows=new ArrayList<Row>();
	/**
	 * 汇总行
	 */
	private List<Row> summaryRows=new ArrayList<Row>();
	/**
	 * 重复表头行高度、重复表尾行高度、标题行高度、汇总行高度
	 */
	private int repeatHeaderRowHeight=0,repeatFooterRowHeight=0,titleRowsHeight=0,summaryRowsHeight=0;
	/**
	 * 所有列
	 */
	private List<Column> columns;

	private List<Page> pages;
	/**
	 * 报表名称
	 */
	private String reportFullName;
	/**
	 * 懒计算单元，当单元格设置了条件属性时，就加入到该集合中，最后在构建报表时再计算条件
	 */
	private List<Cell> lazyComputeCells=new ArrayList<Cell>();
	private Map<Row,Map<Column,Cell>> rowColCellMap=new HashMap<Row,Map<Column,Cell>>();
	/**
	 * 以单元格名称（如 A1）为key，单元格对象为value的Map
	 */
	private Map<String,List<Cell>> cellsMap=new HashMap<String,List<Cell>>();

	public void insertRow(Row row,int rowNumber){
		int pos=rowNumber-1;
		rows.add(pos,row);
		Band band=row.getBand();
		if(band==null){
			return;
		}
	}
	public void insertRows(int firstRowIndex,List<Row> insertRows){
		int pos=firstRowIndex-1;
		rows.addAll(pos,insertRows);
	}
	public void insertColumn(Column column,int columnNumber){
		int pos=columnNumber-1;
		columns.add(pos,column);
	}
	
	public void insertColumns(int firstColumnIndex,List<Column> insertColumns){
		int pos=firstColumnIndex-1;
		columns.addAll(pos, insertColumns);
	}
	
	public Row getRow(int rowNumber){
		if(rowNumber>rows.size()){
			return null;
		}
		return rows.get(rowNumber-1);
	}
	
	public Column getColumn(int columnNumber){
		if(columnNumber>columns.size()){
			return null;
		}
		return columns.get(columnNumber-1);
	}

	public boolean addCell(Cell cell){
		String cellName=cell.getName();
		List<Cell> cells=null;
		if(cellsMap.containsKey(cellName)){
			cells=cellsMap.get(cellName);			
		}else{
			cells=new ArrayList<Cell>();
			cellsMap.put(cellName, cells);
		}
		cells.add(cell);
		Row row=cell.getRow();
		Column col=cell.getColumn();
		Map<Column,Cell> colMap=null;
		if(rowColCellMap.containsKey(row)){
			colMap=rowColCellMap.get(row);
		}else{
			colMap=new HashMap<Column,Cell>();
			rowColCellMap.put(row, colMap);
		}
		colMap.put(col, cell);
		return addLazyCell(cell);
	}
	
	public boolean addLazyCell(Cell cell){
		List<ConditionPropertyItem> conditionPropertyItems=cell.getConditionPropertyItems();
		if(conditionPropertyItems!=null && conditionPropertyItems.size()>0){
			lazyComputeCells.add(cell);
			return true;
		}
		return false;
	}

	
	public List<Page> getPages() {
		if(pages==null){
			pages=PagingBuilder.buildPages(this);
		}
		return pages;
	}
	
	public void rePaging(Paper paper){
		paper.setColumnCount(this.paper.getColumnCount());
		paper.setColumnEnabled(this.paper.isColumnEnabled());
		paper.setFixRows(this.paper.getFixRows());
		paper.setPagingMode(this.paper.getPagingMode());
		setPaper(paper);
		pages=PagingBuilder.buildPages(this);
	}

}
