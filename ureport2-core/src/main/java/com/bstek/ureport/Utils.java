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
package com.bstek.ureport;

import java.math.BigDecimal;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.apache.commons.beanutils.PropertyUtils;
import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

import com.bstek.ureport.build.Context;
import com.bstek.ureport.definition.datasource.BuildinDatasource;
import com.bstek.ureport.exception.ConvertException;
import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.model.Cell;
import com.bstek.ureport.model.Report;
import com.bstek.ureport.provider.image.ImageProvider;


/**
 * @author Jacky.gao
 * @since 2016年11月12日
 */
public class Utils implements ApplicationContextAware{
	/**
	 * spring 上下文对象
	 */
	private static ApplicationContext applicationContext;
	/**
	 * 数据源
	 */
	private static Collection<BuildinDatasource> buildinDatasources;
	/**
	 * 图片提供对象
	 */
	private static Collection<ImageProvider> imageProviders;
	/**
	 * debug模式
	 */
	private static boolean debug;
	
	public static boolean isDebug() {
		return Utils.debug;
	}
	
	public static void logToConsole(String msg){
		if(Utils.debug){
			System.out.println(msg);
		}
	}
	
	public static ApplicationContext getApplicationContext() {
		return applicationContext;
	}
	
	public static Collection<BuildinDatasource> getBuildinDatasources() {
		return buildinDatasources;
	}
	
	public static Collection<ImageProvider> getImageProviders() {
		return imageProviders;
	}

	
	public static Connection getBuildinConnection(String name){
		for(BuildinDatasource datasource:buildinDatasources){
			if(name.equals(datasource.name())){
				return datasource.getConnection();
			}
		}
		return null;
	}

	/**
	 * 根据目标单元格的名称，获取目标单元格的值 用在各种单元格表达式中
	 * 首先会判断目标单元格是否已经处理过，如果没有处理过，则会调用报表构建器构建单元格，这里是个循环，直到目标单元格被处理过后，才会跳出循环
	 * TODO 就是这里会存在递归调用，因为在构建单元格的时候，如果遇到 单元格表达式 ，就又会调用这个方法，所以这里会存在递归调用
	 * 如果 目标单元格与当前单元格即在同一行又在同一列，则获取当前单元格的 在这一行的值 和 在这一列的值 的交集
	 * 如果 目标单元格与当前单元格在同一行或者同一列，则获取目标单元格在这一行或者这一列的值
	 * 如果 目标单元格与当前单元格不在同一行、同一列，则直接获取目标单元格的值
	 * @param cell
	 * @param context
	 * @param cellName
	 * @return
	 */
	public static List<Cell> fetchTargetCells(Cell cell,Context context,String cellName){
		/*
		 * 检查目标单元格是否已经处理过，没有则调用报表构建器构建单元格
		 * 这里入参中{需要处理的单元}直接传入null，这会让构建器自动从上下文中获取下一批等待处理的单元格，这可能不是目标单元格，当不是目标单元格时，会继续当前循环
		 * 所以直到目标单元格被处理过后，才会跳出循环 TODO 这里是否可以优化
		 */
		while(!context.isCellPocessed(cellName)){
			context.getReportBuilder().buildCell(context, null);
		}
		// 获取目标单元格与当前单元格在同于行或者同一列的目标单元格的值
		List<Cell> leftCells=fetchCellsByLeftParent(context,cell, cellName);
		List<Cell> topCells=fetchCellsByTopParent(context,cell, cellName);
		// 如果都找到了值，则返回两个集合的交集
		if(leftCells!=null && topCells!=null){
			int leftSize=leftCells.size(),topSize=topCells.size();
			if(leftSize==1 || topSize==0){
				return leftCells;
			}
			if(topSize==1 || leftSize==0){
				return topCells;
			}
			if(leftSize==0 && topSize==0){
				return new ArrayList<Cell>();
			}
			List<Cell> list=new ArrayList<Cell>();
			if(leftSize<=topSize){
				for(Cell c:leftCells){
					if(topCells.contains(c)){
						list.add(c);
					}
				}
			}else{
				for(Cell c:topCells){
					if(leftCells.contains(c)){
						list.add(c);
					}
				}
			}
			return list;
		}else if(leftCells!=null && topCells==null){
			// 如果只找到了左边的值，则直接返回左边的值
			return leftCells;
		}else if(leftCells==null && topCells!=null){
			// 如果只找到了上边的值，则直接返回上边的值
			return topCells;
		}else{
			// 如果都没有找到，说明这两个单元格不在同一行或者同一列，直接获取目标单元格的值
			// 这里其实就是一个单元格对另一个无关联单元格的引用，计算值时 如果是单个值就计算为单个值，如果是集合就使用逗号分隔组装所有值
			Report report=context.getReport();
			return report.getCellsMap().get(cellName);
		}
	}

	/**
	 * 根据名称获取在当前行的目标单元格，如果目标单元格不在当前行，则返回null
	 * @param context
	 * @param cell 当前单元格
	 * @param cellName 目标单元格名称
	 * @return
	 */
	private static List<Cell> fetchCellsByLeftParent(Context context,Cell cell,String cellName){
		// 获取左父单元格，不存在则返回null
		Cell leftParentCell=cell.getLeftParentCell();
		if(leftParentCell==null){
			return null;
		}
		// 如果左父单元格就是目标单元格，则直接返回左父单元格
		if(leftParentCell.getName().equals(cellName)){
			List<Cell> list=new ArrayList<Cell>();
			list.add(leftParentCell);
			return list;
		}
		// 获取左父单元格的子单元格集合
		Map<String,List<Cell>> childrenCellsMap=leftParentCell.getRowChildrenCellsMap();
		// 从子单元格集合中查找目标单元格，如果找到则直接返回
		List<Cell> targetCells=childrenCellsMap.get(cellName);
		if(targetCells!=null){
			return targetCells;
		}
		// 如果没有找到目标单元格，则继续递归查找
		return fetchCellsByLeftParent(context,leftParentCell,cellName);
	}

	/**
	 * 根据名称获取在当前列的目标单元格，如果目标单元格不在当前列，则返回null
	 * @param context
	 * @param cell 当前单元格
	 * @param cellName 目标单元格名称
	 * @return
	 */
	private static List<Cell> fetchCellsByTopParent(Context context,Cell cell,String cellName){
		Cell topParentCell=cell.getTopParentCell();
		if(topParentCell==null){
			return null;
		}
		if(topParentCell.getName().equals(cellName)){
			List<Cell> list=new ArrayList<Cell>();
			list.add(topParentCell);
			return list;
		}
		Map<String,List<Cell>> childrenCellsMap=topParentCell.getColumnChildrenCellsMap();
		List<Cell> targetCells=childrenCellsMap.get(cellName);
		if(targetCells!=null){
			return targetCells;
		}
		return fetchCellsByTopParent(context,topParentCell,cellName);
	}
	
	public static Object getProperty(Object obj,String property){
		if(obj==null)return null;
		try{
			if(obj instanceof Map && property.indexOf(".")==-1){
				Map<?,?> map=(Map<?,?>)obj;
				return map.get(property);
			}
			return PropertyUtils.getProperty(obj, property);
		}catch(Exception ex){
			throw new ReportComputeException(ex);
		}
	}
	
	public static Date toDate(Object obj){
		if(obj instanceof Date){
			return (Date)obj;
		}else if(obj instanceof String){
			SimpleDateFormat sd=new SimpleDateFormat("yyyy-MM-dd");
			try{
				return sd.parse(obj.toString());
			}catch(Exception ex){
				sd=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
				try{
					return sd.parse(obj.toString());					
				}catch(Exception e){
					throw new ReportComputeException("Can not convert "+obj+" to Date.");
				}
			}
		}
		throw new ReportComputeException("Can not convert "+obj+" to Date.");
	}
	public static BigDecimal toBigDecimal(Object obj){
		if(obj==null){
			return null;
		}
		if(obj instanceof BigDecimal){
			return (BigDecimal)obj;
		}else if(obj instanceof String){
			if(obj.toString().trim().equals("")){
				return new BigDecimal(0);
			}
			try{
				String str=obj.toString().trim();
				return new BigDecimal(str);				
			}catch(Exception ex){
				throw new ConvertException("Can not convert "+obj+" to BigDecimal.");
			}
		}else if(obj instanceof Number){
			return new BigDecimal(String.valueOf(obj));
		}
		throw new ConvertException("Can not convert "+obj+" to BigDecimal.");
	}
	
	public void setDebug(boolean debug) {
		Utils.debug = debug;
	}
	
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)throws BeansException {
		Utils.applicationContext=applicationContext;
		buildinDatasources=new ArrayList<BuildinDatasource>();
		buildinDatasources.addAll(applicationContext.getBeansOfType(BuildinDatasource.class).values());
		imageProviders=new ArrayList<ImageProvider>();
		imageProviders.addAll(applicationContext.getBeansOfType(ImageProvider.class).values());
	}
}
