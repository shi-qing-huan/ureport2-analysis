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
package com.bstek.ureport.build.aggregate;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.Utils;
import com.bstek.ureport.build.BindData;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.definition.Order;
import com.bstek.ureport.expression.model.expr.dataset.DatasetExpression;
import com.bstek.ureport.model.Cell;
import com.bstek.ureport.utils.DataUtils;

/**
 * @author Jacky.gao
 * @since 2016年12月22日
 */
public class GroupAggregate extends Aggregate {
	@Override
	public List<BindData> aggregate(DatasetExpression expr,Cell cell,Context context) {
		/*
		 * 获取单元格的数据集数据
		 * 1、如果单元格没有任何父格，则获取到的就是该数据集的全部数据
		 * 2、如果只有上父格或左父格，则获取到的就是对应父格的数据集数据
		 * 3、如果同时有上父格和左父格，则获取到的就是交集数据
		 */
		List<?> objList=DataUtils.fetchData(cell, context, expr.getDatasetName());
		List<BindData> list = doAggregate(expr, cell, context, objList);
		return list;
	}

	/**
	 * 计算分组聚合数据
	 * @param expr 数据集表达式
	 * @param cell 单元格
	 * @param context 上下文
	 * @param objList 数据集数据
	 * @return 经过聚合后的数据
	 */
	protected List<BindData> doAggregate(DatasetExpression expr, Cell cell,Context context, List<?> objList) {
		String property=expr.getProperty();
		// 获取数据映射
		Map<String,String> mappingMap=context.getMapping(expr);
		List<BindData> list=new ArrayList<BindData>();
		// 如果没有数据，则添加一个空的数据
		if(objList.size()==0){
			list.add(new BindData(""));
			return list;
		}
		// 如果只有一条数据，则计算条件过滤、数据映射后，直接返回
		if(objList.size()==1){
			Object o=objList.get(0);
			// 计算是否满足条件
			boolean conditionResult=doCondition(expr.getCondition(),cell,o,context);
			if(!conditionResult){
				list.add(new BindData(""));
				return list;
			}
			Object data=Utils.getProperty(o, property);
			// 进行数据映射
			Object mappingData=mappingData(mappingMap,data);
			List<Object> rowList=new ArrayList<Object>();
			rowList.add(o);
			if(mappingData==null){
				list.add(new BindData(data,rowList));				
			}else{
				list.add(new BindData(data,mappingData,rowList));								
			}
			return list;
		}
		Map<Object,List<Object>> map=new HashMap<Object,List<Object>>();
		// 数据超过一条时，进行分组聚合
		for(Object o:objList){
			boolean conditionResult=doCondition(expr.getCondition(),cell,o,context);
			if(!conditionResult){
				continue;
			}
			Object data=Utils.getProperty(o, property);
			Object mappingData=mappingData(mappingMap,data);
			List<Object> rowList=null;
			if(map.containsKey(data)){
				rowList=map.get(data);
			}else{
				rowList=new ArrayList<Object>();
				map.put(data, rowList);
				if(mappingData==null){
					list.add(new BindData(data,rowList));				
				}else{
					list.add(new BindData(data,mappingData,rowList));								
				}
			}
			rowList.add(o);				
		}
		if(list.size()==0){
			List<Object> rowList=new ArrayList<Object>();
			rowList.add(new HashMap<String,Object>());
			list.add(new BindData("",rowList));
		}
		if(list.size()>1){
			Order order=expr.getOrder();
			orderBindDataList(list, order);			
		}
		return list;
	}
}
