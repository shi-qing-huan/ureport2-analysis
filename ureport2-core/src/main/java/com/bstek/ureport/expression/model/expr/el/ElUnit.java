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
package com.bstek.ureport.expression.model.expr.el;

import com.bstek.ureport.exception.ReportComputeException;
import com.bstek.ureport.expression.model.Operator;
import com.bstek.ureport.utils.ArithUtils;
import lombok.Getter;
import lombok.Setter;

/**
 * EL表达式单元，用于计算EL表达式，即操作符 + - * / %
 * @author Jacky.gao
 * @since 2017年4月25日
 */
@Getter
@Setter
public class ElUnit {
	/**
	 * 左值
	 */
	private Object left;
	/**
	 * 右值
	 */
	private Object right;
	/**
	 * 操作符
	 */
	private Operator op;
	/**
	 * 下一个操作符
	 */
	private Operator nextOp;
	/**
	 * 下一个单元
	 */
	private ElUnit nextUnit;


	public Object compute(){
		if (right instanceof ElUnit) {
			right = ((ElUnit) right).compute();
		}
		switch (op) {
		case Add:
			return ArithUtils.add(left, right);
		case Complementation:
			return ArithUtils.com(left, right);
		case Divide:
			return ArithUtils.div(left, right);
		case Multiply:
			return ArithUtils.mul(left, right);
		case Subtract:
			return ArithUtils.sub(left, right);
		default:
			throw new ReportComputeException("Unknow operator :" + op);
		}
	}
}
