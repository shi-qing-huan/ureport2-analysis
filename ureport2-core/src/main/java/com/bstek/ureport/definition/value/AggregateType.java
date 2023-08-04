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
package com.bstek.ureport.definition.value;

/**
 * @author Jacky.gao
 * @since 2016年12月21日
 */
public enum AggregateType {
	/**
	 * 分组
	 */
    group,
	/**
	 * 自定义分组
	 */
    customgroup,
	regroup,
	/**
	 * 列表
	 */
	select,
	reselect,
	/**
	 * 求和
	 */
	sum,
	/**
	 * 平均值
	 */
	avg,
	/**
	 * 最大值
	 */
	max,
	/**
	 * 最小值
	 */
	min,
	/**
	 * 计数
	 */
	count
}
