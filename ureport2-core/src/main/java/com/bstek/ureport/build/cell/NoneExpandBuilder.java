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
package com.bstek.ureport.build.cell;

import java.util.List;

import com.bstek.ureport.build.BindData;
import com.bstek.ureport.build.Context;
import com.bstek.ureport.definition.ConditionPropertyItem;
import com.bstek.ureport.model.Cell;

/**
 * 无需展开的单元格构建器
 * @author Jacky.gao
 * @since 2016年11月1日
 */
public class NoneExpandBuilder implements CellBuilder {

	/**
	 * 不展开的单元格构建器，直接将数据填充到单元格中
	 * 如果数据只有一条，则直接填充到单元格中
	 * 如果数据有多条，则将数据用逗号分隔填充到单元格中
	 * 获取数据时，如果有数据映射值（label），就使用映射值，否则使用原值value
	 * 如果设置了条件属性，就将该单元格放到懒计算单元格列表，后面再处理
	 * 没有设置条件属性的单元格，直接进行数据的格式化（如果有的话）
	 * @param dataList
	 * @param cell
	 * @param context
	 * @return
	 */
	@Override
	public Cell buildCell(List<BindData> dataList, Cell cell, Context context) {
		if(dataList.size()==1){
			BindData bindData=dataList.get(0);
			cell.setData(bindData.getValue());
			cell.setFormatData(bindData.getLabel());
			cell.setBindData(bindData.getDataList());
		}else{
			Object obj=null;
			List<Object> bindData=null;
			for(BindData data:dataList){
				if(obj==null){
					// 初次赋值，如果没有设置label（数据映射值）则直接使用value
					if(data.getLabel()==null){
						obj=data.getValue();
					}else{
						obj=data.getLabel();
					}
				}else{
					// 如果已经有值了，则将值用逗号分隔追加
					if(data.getLabel()==null){
						obj=obj+","+data.getValue();					
					}else{
						obj=obj+","+data.getLabel();					
					}
				}
				bindData=data.getDataList();
			}
			cell.setData(obj);
			cell.setBindData(bindData);
		}
		// 获取单元格的条件属性
		List<ConditionPropertyItem> conditionPropertyItems=cell.getConditionPropertyItems();
		if(conditionPropertyItems!=null && conditionPropertyItems.size()>0){
			// 如果设置了条件属性，则将单元格加入到懒计算单元格列表中，后续等其他单元格计算完毕后再计算
			context.getReport().getLazyComputeCells().add(cell);
		}else{
			// 如果没有设置条件属性，则直接计算单元格的格式化数据
			cell.doFormat();
			cell.doDataWrapCompute(context);
		}
		return cell;
	}
}
