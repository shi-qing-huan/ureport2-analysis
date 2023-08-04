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
package com.bstek.ureport.expression.model.expr;

import java.util.List;

import com.bstek.ureport.expression.model.Operator;


/**
 * 五则运算表达式的子类，与其父类 JoinExpression 完全相同，不过一般都是使用该类而不是其父类
 * {五则运算表达式，包含加减乘除和取模运算
 * 也包含单个的 可用于五则运算的子单元 ，如 基础类型（数字、字符串、布尔值）、函数、变量、单元格等
 * 注：从运行情况分析，该类型不直接使用，一般会用其子类 ParenExpression}
 * @author Jacky.gao
 * @author Jacky.gao
 * @since 2016年11月18日
 */
public class ParenExpression extends JoinExpression {
	private static final long serialVersionUID = 142186918381087393L;

	public ParenExpression(List<Operator> operators,List<BaseExpression> expressions) {
		super(operators, expressions);
	}
}
