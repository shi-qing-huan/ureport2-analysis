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

import java.util.List;

import com.bstek.ureport.model.Column;
import lombok.Getter;
import lombok.Setter;

/**
 * 报表中的列定义对象，用于定义报表设计中的列，包括列宽度、是否隐藏等属性
 * @author Jacky.gao
 * @since 2016年11月1日
 */
@Getter
@Setter
public class ColumnDefinition implements Comparable<ColumnDefinition>{
	/**
	 * 列号
	 */
	private int columnNumber;
	/**
	 * 列宽度
	 */
	private int width;
	/**
	 * 是否隐藏
	 */
	private boolean hide;
	
	protected Column newColumn(List<Column> columns){
		Column col=new Column(columns);
		col.setWidth(width);
		return col;
	}

	@Override
	public int compareTo(ColumnDefinition o) {
		return columnNumber-o.getColumnNumber();
	}
}
