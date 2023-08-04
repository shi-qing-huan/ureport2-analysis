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
package com.bstek.ureport.export;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bstek.ureport.Utils;
import com.bstek.ureport.build.ReportBuilder;
import com.bstek.ureport.cache.CacheUtils;
import com.bstek.ureport.definition.CellDefinition;
import com.bstek.ureport.definition.Expand;
import com.bstek.ureport.definition.ReportDefinition;
import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.exception.ReportParseException;
import com.bstek.ureport.export.builder.down.DownCellbuilder;
import com.bstek.ureport.export.builder.right.RightCellbuilder;
import com.bstek.ureport.model.Report;
import com.bstek.ureport.parser.ReportParser;
import com.bstek.ureport.provider.report.ReportProvider;

/**
 * 报表渲染器
 * @author Jacky.gao
 * @since 2016年12月4日
 */
public class ReportRender implements ApplicationContextAware{
	private ReportParser reportParser;
	private ReportBuilder reportBuilder;
	private Collection<ReportProvider> reportProviders;
	private DownCellbuilder downCellParentbuilder=new DownCellbuilder();
	private RightCellbuilder rightCellParentbuilder=new RightCellbuilder();
	
	public Report render(String file,Map<String,Object> parameters){
		ReportDefinition reportDefinition=getReportDefinition(file);
		return reportBuilder.buildReport(reportDefinition,parameters);
	}
	
	public Report render(ReportDefinition reportDefinition,Map<String,Object> parameters){
		return reportBuilder.buildReport(reportDefinition,parameters);
	}
	
	/**
	 * 读取报表模板定义信息：<br>
	 * 1.从缓存中读取文件
	 * 2.若缓存中也不存在 或者是 debug模式，则重新解析板模板源文件，并覆盖缓存
	 * @param file 报表源文件路径
	 * @return
	 */
	public ReportDefinition getReportDefinition(String file){
		long start=System.currentTimeMillis();
		ReportDefinition reportDefinition=CacheUtils.getReportDefinition(file);
		if(reportDefinition==null||Utils.isDebug()){
			reportDefinition=parseReport(file);
			rebuildReportDefinition(reportDefinition);
			CacheUtils.cacheReportDefinition(file, reportDefinition);
		}
		long end=System.currentTimeMillis();
		String msg="~~~ Read ReportDefinition times:"+(end-start)+"ms";
		Utils.logToConsole(msg);
		return reportDefinition;
	}

	/**
	 * 重新构建报表的层级结构
	 * @param reportDefinition 从文件中初步解析出来的报表定义对象
	 */
	public void rebuildReportDefinition(ReportDefinition reportDefinition){
		List<CellDefinition> cells=reportDefinition.getCells();
		for(CellDefinition cell:cells){
			// 将该单元格添加到其所有直接或间接父单元格的子单元格列表中
			addRowChildCell(cell,cell);
			addColumnChildCell(cell,cell);
		}
		for(CellDefinition cell:cells){
			Expand expand=cell.getExpand();
			if(expand.equals(Expand.Down)){
				downCellParentbuilder.buildParentCell(cell,cells);
			}else if(expand.equals(Expand.Right)){
				rightCellParentbuilder.buildParentCell(cell,cells);
			}
		}
	}
	
	public ReportDefinition parseReport(String file){
		InputStream inputStream=null;
		try {
			inputStream=buildReportFile(file);
			ReportDefinition reportDefinition=reportParser.parse(inputStream,file);
			return reportDefinition;
		}finally{
			try {
				if(inputStream!=null){
					inputStream.close();					
				}
			} catch (IOException e) {
				throw new ReportParseException(e);
			}
		}
	}
	
	private InputStream buildReportFile(String file){
		InputStream inputStream=null;
		for(ReportProvider provider:reportProviders){
			if(file.startsWith(provider.getPrefix())){
				inputStream=provider.loadReport(file);
			}
		}
		if(inputStream==null){
			throw new ReportException("Report ["+file+"] not support.");
		}
		return inputStream;
	}

	/**
	 * 把 单元格 添加到其所有直接或间接 左父单元格 的 子单元格集合 中
	 * @param cell 单元格，初次调用时传入的是单元格本身，后续递归调用时传入的是单元格的左父单元格
	 * @param childCell 子单元格，初步调用和后续递归调用时传入的都是单元格本身
	 */
	private void addRowChildCell(CellDefinition cell,CellDefinition childCell){
		// 获取单元格的左父单元格
		CellDefinition leftCell=cell.getLeftParentCell();
		// 如果没有左父单元格，则直接返回
		if(leftCell==null){
			return;
		}
		// 获取左父单元格的子单元格集合
		List<CellDefinition> childrenCells=leftCell.getRowChildrenCells();
		// 将传入的子单元格添加到左父单元格的子单元格集合中
		childrenCells.add(childCell);
		addRowChildCell(leftCell,childCell);
	}

	/**
	 * 把 单元格 添加到其所有直接或间接 上父单元格 的 子单元格集合 中
	 * @param cell 单元格，初次调用时传入的是单元格本身，后续递归调用时传入的是单元格的上父单元格
	 * @param childCell 子单元格，初步调用和后续递归调用时传入的都是单元格本身
	 */
	private void addColumnChildCell(CellDefinition cell,CellDefinition childCell){
		CellDefinition topCell=cell.getTopParentCell();
		if(topCell==null){
			return;
		}
		List<CellDefinition> childrenCells=topCell.getColumnChildrenCells();
		childrenCells.add(childCell);
		addColumnChildCell(topCell,childCell);
	}
	public void setReportParser(ReportParser reportParser) {
		this.reportParser = reportParser;
	}
	public void setReportBuilder(ReportBuilder reportBuilder) {
		this.reportBuilder = reportBuilder;
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
		reportProviders=applicationContext.getBeansOfType(ReportProvider.class).values();
	}
}
