package com.study.controller;

import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;

@Controller
public class LoginController {

	@RequestMapping("/login.do")
	public String login(String username, Model model) {
		if (StringUtils.equalsIgnoreCase("layz", username)) {
			return "../index";
		} else {
			model.addAttribute("message", "哈哈哈，你进不去");
			return "../login";
		}
	}

}
