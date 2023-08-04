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
package com.bstek.ureport.definition;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.bstek.ureport.build.Dataset;
import com.bstek.ureport.definition.datasource.DatasourceDefinition;
import com.bstek.ureport.definition.searchform.RenderContext;
import com.bstek.ureport.definition.searchform.SearchForm;
import com.bstek.ureport.export.html.SearchFormData;
import com.bstek.ureport.model.Cell;
import com.bstek.ureport.model.Column;
import com.bstek.ureport.model.Report;
import com.bstek.ureport.model.Row;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
@Getter
@Setter
public class ReportDefinition implements Serializable{

	private static final long serialVersionUID = 5934291400824773809L;
	/**
	 * 报表全名，格式为：类型前缀:报表文件名
	 */
	private String reportFullName;
	/**
	 * 报表的纸张设置
	 */
	private Paper paper;
	/**
	 * 报表的根单元格定义,报表的单元格处理就是从该单元格开始的，规则为没有任何父格的单元格就是根单元格
	 */
	private CellDefinition rootCell;
	/**
	 * 报表的页眉定义
	 */
	private HeaderFooterDefinition header;
	/**
	 * 报表的页脚定义
	 */
	private HeaderFooterDefinition footer;
	/**
	 * 报表的查询表单定义
	 */
	private SearchForm searchForm;
	/**
	 * 报表的单元格定义列表
	 */
	private List<CellDefinition> cells;
	/**
	 * 报表的行定义列表
	 */
	private List<RowDefinition> rows;
	/**
	 * 报表的列定义列表
	 */
	private List<ColumnDefinition> columns;
	/**
	 * 报表的数据源定义列表
	 */
	private List<DatasourceDefinition> datasources;
	/**
	 * 报表的搜索表单的XML表示
	 */
	private String searchFormXml;
	/**
	 * 报表的样式
	 */
	@JsonIgnore
	private String style;

	/**
	 * 根据报表定义构建报表对象
	 * 1、创建行
	 * 2、创建列
	 * 3、创建单元格，并将单元格放到行和列对象中
	 * 4、绑定单元格的父子关系
	 * @return
	 */
	public Report newReport() {
		Report report = new Report();
		report.setReportFullName(reportFullName);
		report.setPaper(paper);
		report.setHeader(header);
		report.setFooter(footer);
		List<Row> reportRows = new ArrayList<Row>();
		List<Column> reportColumns = new ArrayList<Column>();
		report.setRows(reportRows);
		report.setColumns(reportColumns);
		Map<Integer,Row> rowMap=new HashMap<Integer,Row>();
		int headerRowsHeight=0,footerRowsHeight=0,titleRowsHeight=0,summaryRowsHeight=0;
		// 创建行，将行插入到报表中，并对特殊行进行处理
		for (RowDefinition rowDef : rows) {
			Row newRow=rowDef.newRow(reportRows);
			report.insertRow(newRow, rowDef.getRowNumber());
			rowMap.put(rowDef.getRowNumber(), newRow);
			Band band=rowDef.getBand();
			if(band!=null){
				if(band.equals(Band.headerrepeat)){
					report.getHeaderRepeatRows().add(newRow);
					headerRowsHeight+=newRow.getRealHeight();
				}else if(band.equals(Band.footerrepeat)){
					report.getFooterRepeatRows().add(newRow);
					footerRowsHeight+=newRow.getRealHeight();
				}else if(band.equals(Band.title)){
					report.getTitleRows().add(newRow);
					titleRowsHeight+=newRow.getRealHeight();
				}else if(band.equals(Band.summary)){
					report.getSummaryRows().add(newRow);
					summaryRowsHeight+=newRow.getRealHeight();
				}
			}
		}
		report.setRepeatHeaderRowHeight(headerRowsHeight);
		report.setRepeatFooterRowHeight(footerRowsHeight);
		report.setTitleRowsHeight(titleRowsHeight);
		report.setSummaryRowsHeight(summaryRowsHeight);
		Map<Integer,Column> columnMap=new HashMap<Integer,Column>();
		// 创建列，将列插入到报表中
		for (ColumnDefinition columnDef : columns) {
			Column newColumn=columnDef.newColumn(reportColumns);
			report.insertColumn(newColumn, columnDef.getColumnNumber());
			columnMap.put(columnDef.getColumnNumber(), newColumn);
		}
		Map<CellDefinition,Cell> cellMap=new HashMap<CellDefinition,Cell>();
		// 创建单元格，将单元格插入到报表中
		for (CellDefinition cellDef : cells) {
			// 创建单元格
			Cell cell = cellDef.newCell();
			cellMap.put(cellDef, cell);
			// 获取单元格所在的行和列，并将单元格信息存到行和列中；同时将行列信息存到单元格中
			Row targetRow=rowMap.get(cellDef.getRowNumber());
			cell.setRow(targetRow);
			targetRow.getCells().add(cell);
			Column targetColumn=columnMap.get(cellDef.getColumnNumber());
			cell.setColumn(targetColumn);
			targetColumn.getCells().add(cell);
			// 如果单元格没有父单元格，则将其设置为报表的根单元格
			if(cellDef.getLeftParentCell()==null && cellDef.getTopParentCell()==null){
				report.setRootCell(cell);
			}
			report.addCell(cell);
		}
		// 设置单元格的父单元格
		for (CellDefinition cellDef : cells) {
			Cell targetCell=cellMap.get(cellDef);
			// 根据单元格定义中的父单元格定义，设置单元格的父单元格
			// 左父单元格
			CellDefinition leftParentCellDef=cellDef.getLeftParentCell();
			if(leftParentCellDef!=null){
				targetCell.setLeftParentCell(cellMap.get(leftParentCellDef));
			}else{
				targetCell.setLeftParentCell(null);
			}
			// 上父单元格
			CellDefinition topParentCellDef=cellDef.getTopParentCell();
			if(topParentCellDef!=null){
				targetCell.setTopParentCell(cellMap.get(topParentCellDef));
			}else{
				targetCell.setTopParentCell(null);
			}
		}
		// 设置单元格的子单元格
		for (CellDefinition cellDef : cells) {
			Cell targetCell=cellMap.get(cellDef);
			// 根据单元格定义中的子单元格定义，设置单元格的子单元格
			// 行子单元格
			List<CellDefinition> rowChildrenCellDefinitions=cellDef.getRowChildrenCells();
			for(CellDefinition childCellDef:rowChildrenCellDefinitions){
				Cell childCell=cellMap.get(childCellDef);
				targetCell.addRowChild(childCell);
			}
			// 列子单元格
			List<CellDefinition> columnChildrenCellDefinitions=cellDef.getColumnChildrenCells();
			for(CellDefinition childCellDef:columnChildrenCellDefinitions){
				Cell childCell=cellMap.get(childCellDef);
				targetCell.addColumnChild(childCell);
			}
		}
		return report;
	}
	
	public String getStyle() {
		if(style==null){
			style=buildStyle();
		}
		return style;
	}
	
	private String buildStyle(){
		StringBuffer sb=new StringBuffer();
		for(CellDefinition cell:cells){
			CellStyle cellStyle=cell.getCellStyle();
			sb.append("._"+cell.getName()+"{");
			int colWidth=getColumnWidth(cell.getColumnNumber(),cell.getColSpan());
			sb.append("width:"+colWidth+"pt;");
			Alignment align=cellStyle.getAlign();
			if(align!=null){
				sb.append("text-align:"+align.name()+";");				
			}
			Alignment valign=cellStyle.getValign();
			if(valign!=null){
				sb.append("vertical-align:"+valign.name()+";");				
			}
			float lineHeight=cellStyle.getLineHeight();
			if(lineHeight>0){
				sb.append("line-height:"+lineHeight+";");
			}
			String bgcolor=cellStyle.getBgcolor();
			if(StringUtils.isNotBlank(bgcolor)){
				sb.append("background-color:rgb("+bgcolor+");");				
			}
			String fontFamilty=cellStyle.getFontFamily();
			if(StringUtils.isNotBlank(fontFamilty)){
				sb.append("font-family:"+fontFamilty+";");				
			}
			int fontSize=cellStyle.getFontSize();
			sb.append("font-size:"+fontSize+"pt;");
			String foreColor=cellStyle.getForecolor();
			if(StringUtils.isNotBlank(foreColor)){
				sb.append("color:rgb("+foreColor+");");				
			}
			Boolean bold=cellStyle.getBold(),italic=cellStyle.getItalic(),underline=cellStyle.getUnderline();
			if(bold!=null && bold){
				sb.append("font-weight:bold;");								
			}
			if(italic!=null && italic){
				sb.append("font-style:italic;");												
			}
			if(underline!=null && underline){
				sb.append("text-decoration:underline;");												
			}
			Border border=cellStyle.getLeftBorder();
			if(border!=null){
				sb.append("border-left:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			border=cellStyle.getRightBorder();
			if(border!=null){
				sb.append("border-right:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			border=cellStyle.getTopBorder();
			if(border!=null){
				sb.append("border-top:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			border=cellStyle.getBottomBorder();
			if(border!=null){
				sb.append("border-bottom:"+border.getStyle().name()+" "+border.getWidth()+"px rgb("+border.getColor()+");");				
			}
			sb.append("}");
		}
		return sb.toString();
	}
	
	public SearchFormData buildSearchFormData(Map<String,Dataset> datasetMap,Map<String, Object> parameters){
		if(searchForm==null){
			return null;
		}
		RenderContext context=new RenderContext(datasetMap,parameters);
		SearchFormData data=new SearchFormData();
		data.setFormPosition(searchForm.getFormPosition());
		data.setHtml(searchForm.toHtml(context));
		data.setJs(searchForm.toJs(context));
		data.setSearchFormXml(searchFormXml);
		return data;
	}
	
	private int getColumnWidth(int columnNumber,int colSpan){
		int width=0;
		if(colSpan>0)colSpan--;
		int start=columnNumber,end=start+colSpan;
		for(int i=start;i<=end;i++){
			for(ColumnDefinition col:columns){
				if(col.getColumnNumber()==i){
					width+=col.getWidth();
				}
			}			
		}
		return width;
	}

}
