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

import java.awt.Font;
import java.io.Serializable;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang.StringUtils;
import org.codehaus.jackson.annotate.JsonIgnore;

import com.bstek.ureport.export.pdf.font.FontBuilder;


/**
 * @author Jacky.gao
 * @since 2017年1月18日
 */
@Getter
@Setter
public class CellStyle implements Serializable{
	private static final long serialVersionUID = 8327688051735343849L;
	private String bgcolor;
	private String forecolor;
	private int fontSize;
	private String fontFamily;
	private String format;
	private float lineHeight;
	private Alignment align;
	private Alignment valign;
	private Boolean bold;
	private Boolean italic;
	private Boolean underline;
	/**
	 * 换行计算，即是否自动换行
	 */
	private Boolean wrapCompute;
	private Border leftBorder;
	private Border rightBorder;
	private Border topBorder;
	private Border bottomBorder;
	
	private Font font;


	@JsonIgnore
	public Font getFont(){
		if(this.font==null){
			int fontStyle=Font.PLAIN;
			if((bold!=null && bold) && (italic!=null && italic)){
				fontStyle=Font.BOLD|Font.ITALIC;				
			}else if(bold!=null && bold){
				fontStyle=Font.BOLD;							
			}else if(italic!=null && italic){
				fontStyle=Font.ITALIC;							
			}
			String fontName=fontFamily;
			if(StringUtils.isBlank(fontName)){
				fontName="宋体";
			}
			this.font=FontBuilder.getAwtFont(fontName, fontStyle, new Float(fontSize));
		}
		return this.font;
	}
}
