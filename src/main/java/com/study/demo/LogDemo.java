package com.study.demo;

import com.study.domain.Topic;
import com.study.util.CompareBeanUtils;

public class LogDemo {
	public static void main(String[] args) {
		// 旧对象
		Topic topic1 = new Topic(1, "title", "content");
		// 新对象
		Topic topic2 = new Topic(2, "title", "content2");
		CompareBeanUtils<Topic> cb = new CompareBeanUtils<Topic>(Topic.class,
				topic1, topic2);
		// 需要比较的属性
		cb.compare("title", "标题");
		cb.compare("content", "内容");
		// 比较结果
		System.out.println(cb.toResult());
	}
}
