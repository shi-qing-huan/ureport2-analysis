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
package com.bstek.ureport.build;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.bstek.ureport.build.compute.ChartValueCompute;
import com.bstek.ureport.build.compute.DatasetValueCompute;
import com.bstek.ureport.build.compute.ExpressionValueCompute;
import com.bstek.ureport.build.compute.ImageValueCompute;
import com.bstek.ureport.build.compute.SimpleValueCompute;
import com.bstek.ureport.build.compute.SlashValueCompute;
import com.bstek.ureport.build.compute.ValueCompute;
import com.bstek.ureport.build.compute.ZxingValueCompute;
import com.bstek.ureport.definition.value.Value;
import com.bstek.ureport.exception.ReportException;
import com.bstek.ureport.model.Cell;

/**
 * 数据计算类，用于计算单元格数据
 * @author Jacky.gao
 * @since 2016年12月21日
 */
public class DataCompute {
	/**
	 * 数据计算器集合
	 */
	private static Map<String,ValueCompute> valueComputesMap = new HashMap<String,ValueCompute>();

	/**
	 * 静态初始化数据计算器集合
	 */
	static{
		// 简单单元格
		SimpleValueCompute simpleValueCompute=new SimpleValueCompute();
		valueComputesMap.put(simpleValueCompute.type().name(), simpleValueCompute);
		// 数据集单元格
		DatasetValueCompute datasetValueCompute=new DatasetValueCompute();
		valueComputesMap.put(datasetValueCompute.type().name(), datasetValueCompute);
		// 表达式单元格
		ExpressionValueCompute expressionValueCompute=new ExpressionValueCompute();
		valueComputesMap.put(expressionValueCompute.type().name(), expressionValueCompute);
		// 图片单元格
		ImageValueCompute imageValueCompute=new ImageValueCompute();
		valueComputesMap.put(imageValueCompute.type().name(), imageValueCompute);
		// 斜线单元格，就是一般用于左上角划分成多个三角形的单元格
		SlashValueCompute slashValueCompute=new SlashValueCompute();
		valueComputesMap.put(slashValueCompute.type().name(), slashValueCompute);
		// 二维码单元格
		ZxingValueCompute zxingValueCompute=new ZxingValueCompute();
		valueComputesMap.put(zxingValueCompute.type().name(), zxingValueCompute);
		// 图表单元格
		ChartValueCompute chartValueCompute=new ChartValueCompute();
		valueComputesMap.put(chartValueCompute.type().name(), chartValueCompute);
		
	}

	/**
	 * 根据单元格数据类型，获取对应的数据计算器
	 * @param cell
	 * @param context
	 * @return
	 */
	public static List<BindData> buildCellData(Cell cell,Context context) {
		context.resetVariableMap();
		Value value = cell.getValue();
		ValueCompute valueCompute=valueComputesMap.get(value.getType().name());
		if(valueCompute!=null){
			List<BindData> list= valueCompute.compute(cell, context);
			return list;
		}
		throw new ReportException("Unsupport value: "+value);
	}
}
