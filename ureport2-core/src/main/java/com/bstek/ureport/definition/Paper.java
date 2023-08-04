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

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jacky.gao
 * @since 2014年4月29日
 */
@Getter
@Setter
public class Paper implements Serializable{
	private static final long serialVersionUID = -6153150083492704136L;
	private int leftMargin=90;
	private int rightMargin=90;
	private int topMargin=72;
	private int bottomMargin=72;
	private PaperType paperType;
	private PagingMode pagingMode;
	private int fixRows;
	private int width;
	private int height;
	private Orientation orientation;
	private HtmlReportAlign htmlReportAlign=HtmlReportAlign.left;
	private String bgImage;
	private boolean columnEnabled;
	private int columnCount=2;
	private int columnMargin=5;
	private int htmlIntervalRefreshValue=0;
}
