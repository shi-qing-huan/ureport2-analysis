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
package com.bstek.ureport.expression.model.expr.dataset;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.bstek.ureport.build.BindData;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.build.DatasetUtils;
import com.bstek.ureport.definition.Order;
import com.bstek.ureport.definition.mapping.MappingItem;
import com.bstek.ureport.definition.mapping.MappingType;
import com.bstek.ureport.definition.value.AggregateType;
import com.bstek.ureport.definition.value.GroupItem;
import com.bstek.ureport.expression.model.Condition;
import com.bstek.ureport.expression.model.data.BindDataListExpressionData;
import com.bstek.ureport.expression.model.data.ExpressionData;
import com.bstek.ureport.expression.model.expr.BaseExpression;
import com.bstek.ureport.model.Cell;

/**
 * 数据集表达式
 * @author Jacky.gao
 * @since 2016年11月18日
 */
@Getter
@Setter
public class DatasetExpression extends BaseExpression {
	private static final long serialVersionUID = -8794866509447790340L;
	/**
	 * 数据集名称
	 */
	private String datasetName;
	/**
	 * 聚合类型
	 */
	private AggregateType aggregate;
	/**
	 * 数据集中的属性
	 */
	private String property;
	/**
	 * 当aggregate类型为自定义分组时，采用此属性来存储自定义分组各个项目
	 */
	private List<GroupItem> groupItems;
	/**
	 * 数据映射类型
	 */
	private MappingType mappingType=MappingType.simple;
	/**
	 * 当mappingType为dataset时，采用此属性来存储数据集名称
	 */
	private String mappingDataset;
	/**
	 * 当mappingType为dataset时，采用此属性来存储数据集中的key属性(显示值属性)
	 */
	private String mappingKeyProperty;
	/**
	 * 当mappingType为dataset时，采用此属性来存储数据集中的value属性(实际值属性
	 */
	private String mappingValueProperty;
	/**
	 * 当mappingType为simple时，采用此属性来存储数据映射项目
	 */
	private List<MappingItem> mappingItems;
	
	@JsonIgnore 
	private Condition condition;

	/**
	 * 数据映射项目转为键值对的属性
	 */
	@JsonIgnore 
	private Map<String,String> mapping=null;
	
	/**
	 * 此属性给设计器使用，引擎不使用该属性
	 */
	private List<Condition> conditions;
	/**
	 * 排序方式
	 */
	private Order order;
	
	@Override
	public ExpressionData<?> compute(Cell cell,Cell currentCell,Context context) {
		List<BindData> bindDataList=DatasetUtils.computeDatasetExpression(this, cell, context);
		return new BindDataListExpressionData(bindDataList);
	}

	public void setMappingItems(List<MappingItem> mappingItems) {
		this.mappingItems = mappingItems;
		if(mappingItems!=null){
			mapping=new HashMap<String,String>();
			for(MappingItem item:mappingItems){
				mapping.put(item.getValue(),item.getLabel());
			}				
		}
	}
}
