package com.study.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import com.study.domain.Topic;

@Controller
public class LoginController {

	@RequestMapping(value = "/save", method = RequestMethod.POST)
	public void testA(Topic topic) {
		System.out.println(1111111);
		org.springframework.util.Assert.notNull(topic);
	}
}
