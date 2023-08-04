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

import java.awt.Font;
import java.awt.FontMetrics;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JLabel;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import com.bstek.ureport.Range;
import com.bstek.ureport.Utils;
import com.bstek.ureport.build.BindData;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.definition.Alignment;
import com.bstek.ureport.definition.BlankCellInfo;
import com.bstek.ureport.definition.Border;
import com.bstek.ureport.definition.CellStyle;
import com.bstek.ureport.definition.ConditionCellStyle;
import com.bstek.ureport.definition.ConditionPaging;
import com.bstek.ureport.definition.ConditionPropertyItem;
import com.bstek.ureport.definition.Expand;
import com.bstek.ureport.definition.LinkParameter;
import com.bstek.ureport.definition.PagingPosition;
import com.bstek.ureport.definition.Scope;
import com.bstek.ureport.definition.value.SimpleValue;
import com.bstek.ureport.definition.value.Value;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.expression.model.Condition;
import com.bstek.ureport.expression.model.Expression;
import com.bstek.ureport.expression.model.data.BindDataListExpressionData;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.data.ObjectExpressionData;
import com.bstek.ureport.expression.model.data.ObjectListExpressionData;
import com.bstek.ureport.utils.UnitUtils;

/**
 * @author Jacky.gao
 * @since 2016年11月1日
 */
@Getter
@Setter
public class Cell implements ReportCell {
	private String name;
	/**
	 * 行跨度，即行合并数
	 */
	private int rowSpan;
	/**
	 * 列跨度，即列合并数
	 */
	private int colSpan;
	
	/**
	 * 下面属性用于存放分页后的rowspan信息
	 * */
	private int pageRowSpan=-1;
	
	private String renderBean;

	/**
	 * 当前单元格计算后的实际值
	 */
	private Object data;
	
	/**
	 * 存储当前单元格对应值在进行格式化后的值
	 */
	private Object formatData;
	
	private CellStyle cellStyle;
	private CellStyle customCellStyle;
	/**
	 * 单元格的值，可能是简单值或表达式等
	 */
	private Value value;
	/**
	 * 当前单元格所在的行
	 */
	private Row row;
	/**
	 * 当前单元格所在的列
	 */
	private Column column;
	/**
	 * 展开方向
	 */
	private Expand expand;
	/**
	 * 是否已处理
	 */
	private boolean processed;
	private boolean blankCell;
	private boolean existPageFunction;
	/**
	 * 单元格value计算出的值
	 */
	private List<Object> bindData;
	private Range duplicateRange;
	private boolean forPaging;
	private String linkUrl;
	private String linkTargetWindow;
	private List<LinkParameter> linkParameters;
	
	private Map<String,String> linkParameterMap;
	
	private Expression linkUrlExpression;
	/**
	 * 条件属性列表
	 */
	private List<ConditionPropertyItem> conditionPropertyItems;
	/**
	 * 是否需要填充空白行
	 */
	private boolean fillBlankRows;
	/**
	 * 允许填充空白行时fillBlankRows=true，要求当前数据行数必须是multiple定义的行数的倍数，否则就补充空白行
	 */
	private int multiple;
	
	/**
	 * 当前单元格左父格
	 */
	private Cell leftParentCell;
	/**
	 * 当前单元格上父格
	 */
	private Cell topParentCell;
	
	/**
	 * 当前单元格所在行所有子格
	 */
	private Map<String,List<Cell>> rowChildrenCellsMap=new HashMap<String,List<Cell>>();
	/**
	 * 当前单元格所在列所有子格
	 */
	private Map<String,List<Cell>> columnChildrenCellsMap=new HashMap<String,List<Cell>>();
	
	
	private List<String> increaseSpanCellNames;
	private Map<String,BlankCellInfo> newBlankCellsMap;
	private List<String> newCellNames;
	
	public Cell newRowBlankCell(Context context,BlankCellInfo blankCellInfo,ReportCell mainCell){
		Cell blankCell=newCell();
		blankCell.setBlankCell(true);
		blankCell.setValue(new SimpleValue(""));
		blankCell.setExpand(Expand.None);
		blankCell.setBindData(null);
		if(blankCellInfo!=null){
			int offset=blankCellInfo.getOffset();
			int mainRowNumber=mainCell.getRow().getRowNumber();
			if(offset==0){
				blankCell.setRow(mainCell.getRow());
			}else{
				int rowNumber=mainRowNumber+offset;
				Row row=context.getRow(rowNumber);
				blankCell.setRow(row);			
			}
			blankCell.setRowSpan(blankCellInfo.getSpan());			
		}
		return blankCell;
	}
	
	public Cell newColumnBlankCell(Context context,BlankCellInfo blankCellInfo,ReportCell mainCell){
		Cell blankCell=newCell();
		blankCell.setBlankCell(true);
		blankCell.setValue(new SimpleValue(""));
		blankCell.setExpand(Expand.None);
		blankCell.setBindData(null);
		
		int offset=blankCellInfo.getOffset();
		int mainColNumber=mainCell.getColumn().getColumnNumber();
		if(offset==0){
			blankCell.setColumn(mainCell.getColumn());
		}else{
			int colNumber=mainColNumber+offset;
			Column col=context.getColumn(colNumber);
			blankCell.setColumn(col);			
		}
		blankCell.setColSpan(blankCellInfo.getSpan());
		return blankCell;
	}
	
	public Cell newCell(){
		Cell cell=new Cell();
		cell.setColumn(column);
		cell.setRow(row);
		cell.setLeftParentCell(leftParentCell);
		cell.setTopParentCell(topParentCell);
		cell.setValue(value);
		cell.setRowSpan(rowSpan);
		cell.setColSpan(colSpan);
		cell.setExpand(expand);
		cell.setName(name);
		cell.setCellStyle(cellStyle);
		cell.setNewBlankCellsMap(newBlankCellsMap);
		cell.setNewCellNames(newCellNames);
		cell.setIncreaseSpanCellNames(increaseSpanCellNames);
		cell.setDuplicateRange(duplicateRange);
		cell.setLinkParameters(linkParameters);
		cell.setLinkTargetWindow(linkTargetWindow);
		cell.setLinkUrl(linkUrl);
		cell.setPageRowSpan(pageRowSpan);
		cell.setConditionPropertyItems(conditionPropertyItems);
		cell.setFillBlankRows(fillBlankRows);
		cell.setMultiple(multiple);
		cell.setLinkUrlExpression(linkUrlExpression);
		return cell;
	}
	
	public void addRowChild(Cell child){
		String name=child.getName();
		List<Cell> cells=rowChildrenCellsMap.get(name);
		if(cells==null){
			cells=new ArrayList<Cell>();
			rowChildrenCellsMap.put(name, cells);
		}
		if(!cells.contains(child)){
			cells.add(child);			
		}
		if(leftParentCell!=null){
			leftParentCell.addRowChild(child);
		}
	}
	
	
	public void addColumnChild(Cell child){
		String name=child.getName();
		List<Cell> cells=columnChildrenCellsMap.get(name);
		if(cells==null){
			cells=new ArrayList<Cell>();
			columnChildrenCellsMap.put(name, cells);
		}
		if(!cells.contains(child)){
			cells.add(child);			
		}
		if(topParentCell!=null){
			topParentCell.addColumnChild(child);
		}
	}

	
	public Object getFormatData() {
		if(formatData==null){
			return data;
		}
		return formatData;
	}
	
	public void doCompute(Context context){
		doComputeConditionProperty(context);
		doFormat();
		doDataWrapCompute(context);
	}

	/**
	 * 进行格式化计算
	 * 只支持数字和日期格式化
	 */
	public void doFormat(){
		String format=cellStyle.getFormat();
		String customFormat=null;
		if(customCellStyle!=null){
			customFormat=customCellStyle.getFormat();
		}
		if(StringUtils.isNotBlank(customFormat)){
			format=customFormat;
		}
		if(StringUtils.isBlank(format) || data==null || StringUtils.isBlank(data.toString())){
			return;
		}
		if(data instanceof Date){
			Date d=(Date)data;
			SimpleDateFormat sd=new SimpleDateFormat(format);
			formatData=sd.format(d);
		}else{
			BigDecimal bd=null;
			try{
				bd=Utils.toBigDecimal(data);				
			}catch(Exception ex){
			}
			if(bd!=null){				
				DecimalFormat df=new DecimalFormat(format);
				formatData=df.format(bd.doubleValue());
			}
		}
	}

	/**
	 * 进行条件属性计算
	 * 根据条件属性的配置，计算出当前单元格的样式
	 * @param context
	 */
	private void doComputeConditionProperty(Context context){
		if(conditionPropertyItems==null || conditionPropertyItems.size()==0){
			return;
		}
		for(ConditionPropertyItem item:conditionPropertyItems){
			Condition condition=item.getCondition();
			if(condition==null){
				continue;
			}
			Object obj=null;
			List<Object> bindDataList=this.bindData;
			if(bindDataList!=null && bindDataList.size()>0){
				obj=bindDataList.get(0);
			}
			if(!condition.filter(this, this, obj, context)){
				continue;
			}
			ConditionPaging paging=item.getPaging();
			if(paging!=null){
				PagingPosition position=paging.getPosition();
				if(position!=null){
					if(position.equals(PagingPosition.after)){						
						int line=paging.getLine();
						if(line==0){
							row.setPageBreak(true);
						}else{
							int rowNumber=row.getRowNumber()+line;
							Row targetRow=context.getRow(rowNumber);
							targetRow.setPageBreak(true);
						}
						
					}else{
						int rowNumber=row.getRowNumber()-1;
						Row targetRow=context.getRow(rowNumber);
						targetRow.setPageBreak(true);
					}
				}
			}
			
			int rowHeight=item.getRowHeight();
			if(rowHeight>-1){
				row.setRealHeight(rowHeight);
				if(rowHeight==0 && !row.isHide()){
					context.doHideProcessRow(row);
				}
			}
			int colWidth=item.getColWidth();
			if(colWidth>-1){
				column.setWidth(colWidth);
				if(colWidth==0 && !column.isHide()){
					context.doHideProcessColumn(column);
				}
			}
			if(StringUtils.isNotBlank(item.getNewValue())){
				this.data=item.getNewValue();
				this.formatData=item.getNewValue();
			}
			if(StringUtils.isNotBlank(item.getLinkUrl())){
				linkUrl=item.getLinkUrl();
				if(StringUtils.isNotBlank(item.getLinkTargetWindow())){
					linkTargetWindow=item.getLinkTargetWindow();					
				}
				if(item.getLinkParameters()!=null && item.getLinkParameters().size()>0){
					linkParameters=item.getLinkParameters();					
				}
			}
			ConditionCellStyle style=item.getCellStyle();
			if(style!=null){
				Boolean bold=style.getBold();
				if(bold!=null){
					Scope scope=style.getBoldScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setBold(bold);						
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setBold(bold);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setBold(bold);
					}
				}
				Boolean italic=style.getItalic();
				if(italic!=null){
					Scope scope=style.getItalicScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setItalic(italic);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setItalic(italic);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setItalic(italic);
					}
				}
				Boolean underline=style.getUnderline();
				if(underline!=null){
					Scope scope=style.getUnderlineScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setUnderline(underline);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setUnderline(underline);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setUnderline(underline);
					}
					
				}
				String forecolor=style.getForecolor();
				if(StringUtils.isNotBlank(forecolor)){
					Scope scope=style.getForecolorScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setForecolor(forecolor);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setForecolor(forecolor);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setForecolor(forecolor);
					}
				}
				String bgcolor=style.getBgcolor();
				if(StringUtils.isNotBlank(bgcolor)){
					Scope scope=style.getBgcolorScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setBgcolor(bgcolor);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setBgcolor(bgcolor);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setBgcolor(bgcolor);
					}
				}
				int fontSize=style.getFontSize();
				if(fontSize>0){
					Scope scope=style.getFontSizeScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setFontSize(fontSize);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setFontSize(fontSize);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setFontSize(fontSize);
					}
				}
				String fontFamily=style.getFontFamily();
				if(StringUtils.isNotBlank(fontFamily)){
					Scope scope=style.getFontFamilyScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setFontFamily(fontFamily);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setFontFamily(fontFamily);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setFontFamily(fontFamily);
					}
				}
				String format=style.getFormat();
				if(StringUtils.isNotBlank(format)){
					if(this.customCellStyle==null){
						this.customCellStyle=new CellStyle();
					}
					this.customCellStyle.setFormat(format);
				}
				Alignment align=style.getAlign();
				if(align!=null){
					Scope scope=style.getAlignScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setAlign(align);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setAlign(align);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setAlign(align);
					}
				}
				Alignment valign=style.getValign();
				if(valign!=null){
					Scope scope=style.getValignScope();
					if(scope.equals(Scope.cell)){
						if(this.customCellStyle==null){
							this.customCellStyle=new CellStyle();
						}
						this.customCellStyle.setValign(valign);
					}else if(scope.equals(Scope.row)){
						if(row.getCustomCellStyle()==null){
							row.setCustomCellStyle(new CellStyle());
						}
						row.getCustomCellStyle().setValign(valign);
					}else if(scope.equals(Scope.column)){
						if(column.getCustomCellStyle()==null){
							column.setCustomCellStyle(new CellStyle());
						}
						column.getCustomCellStyle().setValign(valign);
					}
				}
				Border leftBorder=style.getLeftBorder();
				if(leftBorder!=null){
					this.customCellStyle.setLeftBorder(leftBorder);
				}
				Border rightBorder=style.getRightBorder();
				if(rightBorder!=null){
					this.customCellStyle.setRightBorder(rightBorder);
				}
				Border topBorder=style.getTopBorder();
				if(topBorder!=null){
					this.customCellStyle.setTopBorder(topBorder);
				}
				Border bottomBorder=style.getBottomBorder();
				if(bottomBorder!=null){
					this.customCellStyle.setBottomBorder(bottomBorder);
				}
			}
		}
	}

	/**
	 * 进行换行计算
	 * @param context
	 */
	public void doDataWrapCompute(Context context){
		Boolean wrapCompute=cellStyle.getWrapCompute();
		if(wrapCompute==null || !wrapCompute){
			return;
		}
		Object targetData=getFormatData();
		if(targetData==null || !(targetData instanceof String)){
			return;
		}
		String dataText=targetData.toString();
		if(StringUtils.isBlank(dataText) || dataText.length()<2){
			return;
		}
		int totalColumnWidth=column.getWidth();
		if(colSpan>0){
			int colNumber=column.getColumnNumber();
			for(int i=1;i<colSpan;i++){
				Column col=context.getColumn(colNumber+i);
				totalColumnWidth+=col.getWidth();
			}
		}
		Font font=cellStyle.getFont();
		JLabel jlabel=new JLabel();
		FontMetrics fontMetrics=jlabel.getFontMetrics(font);
		int textWidth=fontMetrics.stringWidth(dataText);
		
		double fontSize=cellStyle.getFontSize();
		float lineHeight=1.2f;
		if(cellStyle.getLineHeight()>0){
			lineHeight=cellStyle.getLineHeight();
		}
		fontSize=fontSize*lineHeight;
		int singleLineHeight=UnitUtils.pointToPixel(fontSize)-2;//fontMetrics.getHeight();
		if(textWidth<=totalColumnWidth){
			return;
		}
		int totalLineHeight=0;
		StringBuilder multipleLine=new StringBuilder();
		StringBuilder sb=new StringBuilder();
		int length=dataText.length();
		for(int i=0;i<length;i++){
			char text=dataText.charAt(i);
			if(text=='\r' || text=='\n'){
				if(text=='\r'){
					int nextIndex=i+1;
					if(nextIndex<length){
						char nextText=dataText.charAt(nextIndex);
						if(nextText=='\n'){
							i=nextIndex;
						}
					}
				}
				continue;
			}
			sb.append(text);
			
			int width=fontMetrics.stringWidth(sb.toString())+4;
			if(width>totalColumnWidth){
				sb.deleteCharAt(sb.length()-1);
				totalLineHeight+=singleLineHeight;										
				if(multipleLine.length()>0){
					multipleLine.append('\n');
				}
				multipleLine.append(sb);
				sb.delete(0, sb.length());
				sb.append(text);
			}
		}
		if(sb.length()>0){
			totalLineHeight+=singleLineHeight;
			if(multipleLine.length()>0){
				multipleLine.append('\n');
			}
			multipleLine.append(sb);
		}
		this.formatData=multipleLine.toString();
		int totalRowHeight=row.getHeight();
		if(rowSpan>0){
			int rowNumber=row.getRowNumber();
			for(int i=1;i<rowSpan;i++){
				Row targetRow=context.getRow(rowNumber+i);
				totalRowHeight+=targetRow.getHeight();
			}
		}
		int dif=totalLineHeight-totalRowHeight;
		if(dif>0){
			int rowHeight=row.getHeight();
			int newRowHeight = rowHeight+dif;
			if(row.getRealHeight()< newRowHeight){
				row.setRealHeight(newRowHeight);
			}
		}
	}
	
	
	
	public static void main(String[] args) {
		FontMetrics fontMetrics=new JLabel().getFontMetrics(new Font("宋体",Font.PLAIN,12));
		String text="我是中国人，我来自China,好吧！top和bottom文档描述地很模糊，其实这里我们可以借鉴一下TextView对文本的绘制，"
				+ "TextView在绘制文本的时候总会在文本的最外层留出一些内边距，为什么要这样做？因为TextView在绘制文本的时候考虑到了类似读音符号，"
				+ "下图中的A上面的符号就是一个拉丁文的类似读音符号的东西";
		int columnWidth=50;
		long start=System.currentTimeMillis();
		int totalLineHeight=0;
		int singleLineHeight=fontMetrics.getHeight();
		StringBuffer multipleLine=new StringBuffer();
		StringBuffer sb=new StringBuffer();
		for(int i=0;i<text.length();i++){
			char str=text.charAt(i);
			sb.append(str);
			int width=fontMetrics.stringWidth(sb.toString());
			if(width>columnWidth){
				sb.deleteCharAt(sb.length()-1);
				if(multipleLine.length()>0){
					multipleLine.append("\r");
					totalLineHeight+=singleLineHeight;
				}
				multipleLine.append(sb);
				sb.delete(0, sb.length());
				sb.append(str);
			}
		}
		if(multipleLine.length()>0){
			multipleLine.append("\r");
		}
		if(sb.length()>0){			
			multipleLine.append(sb);
		}
		long end=System.currentTimeMillis();
		System.out.println(end-start);
		System.out.println(multipleLine.toString());
		System.out.println("totalLineHeight:"+totalLineHeight);
	}

	public int getPageRowSpan() {
		if(pageRowSpan==-1){
			return rowSpan;
		}
		return pageRowSpan;
	}
	
	public String buildLinkParameters(Context context){
		StringBuilder sb=new StringBuilder();
		if(linkParameters!=null){
			for(int i=0;i<linkParameters.size();i++){
				LinkParameter param=linkParameters.get(i);
				String name=param.getName();
				if(linkParameterMap==null){
					linkParameterMap=new HashMap<String,String>();
				}
				String value=linkParameterMap.get(name);
				if(value==null){
					Expression expr=param.getValueExpression();
					value=buildExpression(context, name, expr);
				}
				try {
					value=URLEncoder.encode(value, "utf-8");
					value=URLEncoder.encode(value, "utf-8");
				} catch (UnsupportedEncodingException e) {
					throw new ReportComputeException(e);
				}
				if(i>0){
					sb.append("&");
				}
				sb.append(name+"="+value);
			}
		}
		return sb.toString();
	}


	private String buildExpression(Context context, String name, Expression expr) {
		ExpressionData<?> exprData=expr.execute(this,this,context);
		if(exprData instanceof ObjectListExpressionData){
			ObjectListExpressionData listData=(ObjectListExpressionData)exprData;
			List<?> list=listData.getData();
			StringBuilder dataSB=new StringBuilder();
			for(int i=0;i<list.size();i++){
				Object obj=list.get(i);
				if(obj==null){
					obj="null";
				}
				if(i>0){
					dataSB.append(",");
				}
				dataSB.append(obj);
			}
			linkParameterMap.put(name, dataSB.toString());
			return dataSB.toString();
		}else if(exprData instanceof ObjectExpressionData){
			ObjectExpressionData data=(ObjectExpressionData)exprData;
			Object obj=data.getData();
			if(obj==null){
				obj="null";
			}else if(obj instanceof String){
				obj=(String) obj;
			}
			linkParameterMap.put(name, obj.toString());
			return obj.toString();
		}else if(exprData instanceof BindDataListExpressionData){
			BindDataListExpressionData bindDataListData=(BindDataListExpressionData)exprData;
			List<BindData> list=bindDataListData.getData();
			if(list.size()==1){
				Object data = list.get(0).getValue();
				if(data!=null){
					return data.toString();
				}else{
					return "";
				}
			}else if(list.size()>1){
				StringBuilder sb=new StringBuilder();
				for(BindData bindData:list){
					if(sb.length()>0){
						sb.append(",");
					}
					Object data=bindData.getValue();
					if(data!=null){
						sb.append(data.toString());
					}
				}
				return sb.toString();
			}
		}
		return "";
	}
}
