package com.bstek.ureport.sample;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

@Controller
public class TestController {
	
	@RequestMapping(value="test",method=RequestMethod.GET)
	public String path(){
		return "test";
	}

	@RequestMapping(value="index",method=RequestMethod.GET)
	public String demo(){
		return "/index";
	}
}
