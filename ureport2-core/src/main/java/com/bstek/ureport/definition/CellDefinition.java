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
import org.codehaus.jackson.annotate.JsonIgnore;

import com.bstek.ureport.Range;
import com.bstek.ureport.definition.value.Value;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.model.Cell;


/**
 * 单元格定义对象
 * @author Jacky.gao
 * @since 2016年11月1日
 */
@Getter
@Setter
public class CellDefinition implements Serializable{
	private static final long serialVersionUID = -2667510071560936139L;
	/**
	 * 单元格所在行号
	 */
	private int rowNumber;
	/**
	 * 单元格所在列号
	 */
	private int columnNumber;
	/**
	 * 行跨度
	 */
	private int rowSpan;
	/**
	 * 列跨度
	 */
	private int colSpan;
	/**
	 * 单元格名称 如：A1、B1
	 */
	private String name;
	/**
	 * 单元格值
	 */
	private Value value;
	/**
	 * 单元格的样式
	 */
	private CellStyle cellStyle=new CellStyle();
	/**
	 * 链接配置 URL
	 */
	private String linkUrl;
	/**
	 * 链接配置 目标窗口
	 */
	private String linkTargetWindow;
	/**
	 * 链接配置 URL参数
	 */
	private List<LinkParameter> linkParameters;

	/**
	 * 链接配置 URL表达式，比如在配置URL参数值为 # 时，#代表当前单元格的值
	 */
	@JsonIgnore
	private Expression linkUrlExpression;

	/**
	 * 是否填充空白行
	 */
	private boolean fillBlankRows;
	/**
	 * 允许填充空白行时fillBlankRows=true，要求当前数据行数必须是multiple定义的行数的倍数，否则就补充空白行
	 */
	private int multiple;

	/**
	 * 数据展开方向
	 */
	private Expand expand=Expand.None;
	
	@JsonIgnore
	private Range duplicateRange;
	@JsonIgnore
	private List<String> increaseSpanCellNames=new ArrayList<String>();
	@JsonIgnore
	private Map<String,BlankCellInfo> newBlankCellsMap=new HashMap<String,BlankCellInfo>();
	@JsonIgnore
	private List<String> newCellNames=new ArrayList<String>();
	
	/**
	 * 当前单元格左父格名
	 */
	private String leftParentCellName;
	/**
	 * 当前单元格上父格名
	 */
	private String topParentCellName;
	/**
	 * 当前单元格左父格
	 */
	@JsonIgnore
	private CellDefinition leftParentCell;
	/**
	 * 当前单元格上父格
	 */
	@JsonIgnore
	private CellDefinition topParentCell;
	/**
	 * 当前单元格所在行的所有子格
	 */
	@JsonIgnore
	private List<CellDefinition> rowChildrenCells=new ArrayList<CellDefinition>();
	/**
	 * 当前单元格所在列的所有子格
	 */
	@JsonIgnore
	private List<CellDefinition> columnChildrenCells=new ArrayList<CellDefinition>();

	/**
	 * 条件属性
	 */
	private List<ConditionPropertyItem> conditionPropertyItems;
	
	protected Cell newCell(){
		Cell cell=new Cell();
		cell.setValue(value);
		cell.setName(name);
		cell.setRowSpan(rowSpan);
		cell.setColSpan(colSpan);
		cell.setExpand(expand);
		cell.setCellStyle(cellStyle);
		cell.setNewBlankCellsMap(newBlankCellsMap);
		cell.setIncreaseSpanCellNames(increaseSpanCellNames);
		cell.setNewCellNames(newCellNames);
		cell.setDuplicateRange(duplicateRange);
		cell.setLinkParameters(linkParameters);
		cell.setLinkTargetWindow(linkTargetWindow);
		cell.setLinkUrl(linkUrl);
		cell.setConditionPropertyItems(conditionPropertyItems);
		cell.setFillBlankRows(fillBlankRows);
		cell.setMultiple(multiple);
		cell.setLinkUrlExpression(linkUrlExpression);
		return cell;
	}

}
