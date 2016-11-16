/*
 * @(#)CompareBeanUtils.java Create on  2016-06-17
 *
 * Copyright (c) 2016 HappiVision
 *
 * Company: 上海快乐讯广告传播有限公司 (HappiVision Multi-Media Co.Ltd)
 *
 *
 *
 */

package com.study.util;

import java.beans.PropertyEditor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.propertyeditors.CustomDateEditor;
import org.springframework.util.ReflectionUtils;

/**
 * CompareBeanUtils.java Create on 2016-06-17 处理日志分析、记录对应的实体修改的结果前后
 * 
 * @param <T>
 *
 */
@SuppressWarnings("rawtypes")
public class CompareBeanUtils<T> {
	private Map<Class, PropertyEditor> propEditorMap = new HashMap<Class, PropertyEditor>();
	private boolean isNew = true;
	private T oldObject;
	private T newObject;
	private Class<T> clazz;
	private StringBuffer content;

	/**
	 * CompareBeanUtils Create on 2016-06-17
	 * 
	 * @description
	 *
	 *
	 * @param clazz
	 */
	protected CompareBeanUtils(Class<T> clazz) {
		super();
		content = new StringBuffer();
		this.clazz = clazz;
		register(java.util.Date.class, new CustomDateEditor(
				new SimpleDateFormat("yyyy-MM-dd"), true)); // 注册日期类型
	}

	/**
	 *
	 * @param clazz
	 * @param newObject
	 */
	public CompareBeanUtils(Class<T> clazz, T newObject) {
		this(clazz);
		this.newObject = newObject;
	}

	/**
	 * 构造
	 *
	 *
	 * @param clazz
	 * @param oldObject
	 *            老对象
	 * @param newObject
	 *            新的对象
	 */
	public CompareBeanUtils(Class<T> clazz, T oldObject, T newObject) {
		this(clazz);
		this.oldObject = oldObject;
		this.newObject = newObject;
		this.isNew = false;
	}

	/**
	 * compare Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param prop
	 *            需要记录日志的属性
	 * @param propLabel
	 *            需要记录日志的属性的中文名，一般对应表单当中的label属性
	 */
	public void compare(String prop, String propLabel) {
		try {
			Field field = ReflectionUtils.findField(clazz, prop);
			Method m = null;
			Object newValue = null;

			if (field != null) {
				ReflectionUtils.makeAccessible(field);
				newValue = field.get((newObject == null) ? oldObject
						: newObject);
			} else {
				char c = prop.charAt(0);

				m = clazz.getDeclaredMethod("get"
						+ StringUtils.upperCase(String.valueOf(c))
						+ prop.substring(1));
				newValue = m
						.invoke((newObject == null) ? oldObject : newObject);
			}

			if (isNew) {
				if (!isNullOrEmpty(newValue)) {
					comparedIsAdd(propLabel, newValue);
				}
			} else {
				if (null == oldObject) {
					return;
				}

				if (null == newObject) {
					comparedIsdel(propLabel);

					return;
				}

				Object oldValue = null;

				if (field != null) {
					oldValue = field.get(oldObject);
				} else {
					oldValue = m.invoke(oldObject);
				}

				if (notEquals(oldValue, newValue)) {
					comparedIsEdit(propLabel, oldValue, newValue);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/*
	 * 记录新值
	 */

	/**
	 * comparedIsAdd Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param propLabel
	 * @param newValue
	 */
	private void comparedIsAdd(String propLabel, Object newValue) {
		content.append("创建了 [");
		content.append(propLabel);
		content.append("],新值为\"");
		content.append(format(newValue));
		content.append("\";");
	}

	/*
	 * 记录变更数据
	 */

	/**
	 * comparedIsEdit Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param propLabel
	 * @param oldValue
	 * @param newValue
	 */
	private void comparedIsEdit(String propLabel, Object oldValue,
			Object newValue) {
		content.append("修改了[");
		content.append(propLabel);
		content.append("],");
		content.append("旧值为\"");
		content.append(format(oldValue));
		content.append("\",新值为\"");
		content.append(format(newValue));
		content.append("\";");
	}

	/**
	 * comparedIsdel Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param propLabel
	 */
	private void comparedIsdel(String propLabel) {
		content.append("删除了 [");
		content.append(propLabel);
		content.append("]");
		content.append(";");
	}

	/**
	 * 格式化数据类型
	 *
	 * @param obj
	 * @return
	 */
	private Object format(Object obj) {
		if (isNullOrEmpty(obj)) {
			return "";
		}

		Class clz = obj.getClass();

		if (propEditorMap.containsKey(clz)) {
			PropertyEditor pe = propEditorMap.get(clz);

			pe.setValue(obj);

			return pe.getAsText();
		} else {
			return obj;
		}
	}

	/**
	 * newInstance Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param newObj
	 * @param <T>
	 *
	 * @return
	 */
	public static <T> CompareBeanUtils<T> newInstance(T newObj) {
		if (null == newObj) {
			return null;
		}

		Class clazz = newObj.getClass();

		return new CompareBeanUtils<T>(clazz, newObj);
	}

	/**
	 * newInstance Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param oldObj
	 * @param newObj
	 * @param <T>
	 *
	 * @return
	 */
	public static <T> CompareBeanUtils<T> newInstance(T oldObj, T newObj) {
		if ((null == oldObj) && (null == newObj)) {
			return null;
		}

		Class clazz;

		clazz = ((newObj == null) ? oldObj.getClass() : newObj.getClass());

		return new CompareBeanUtils<T>(clazz, oldObj, newObj);
	}

	/**
	 * notEquals Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param oldValue
	 * @param newValue
	 *
	 * @return
	 */
	private boolean notEquals(Object oldValue, Object newValue) {
		String tmpOld, tmpNew;

		if (isNullOrEmpty(oldValue)) {
			tmpOld = "";
		} else {
			tmpOld = oldValue.toString();
		}

		if (isNullOrEmpty(newValue)) {
			tmpNew = "";
		} else {
			tmpNew = newValue.toString();
		}

		return !StringUtils.equals(tmpNew, tmpOld);
	}

	/**
	 * register Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param clazz
	 * @param pe
	 * @param <CC>
	 */
	public <CC> void register(Class<CC> clazz, PropertyEditor pe) {
		propEditorMap.put(clazz, pe);
	}

	/**
	 * toResult Create on 2016-06-17
	 * 
	 * @description 数据结果
	 *
	 * @author yangbin
	 *
	 * @return
	 */
	public String toResult() {
		return content.toString();
	}

	/**
	 * isNullOrEmpty Create on 2016-06-17
	 * 
	 * @description
	 *
	 * @author yangbin
	 *
	 * @param val
	 *
	 * @return
	 */
	private boolean isNullOrEmpty(Object val) {
		if (val instanceof String) {
			return (StringUtils.isEmpty(String.class.cast(val)));
		} else {
			return val == null;
		}
	}
}

// ~ Formatted by Jindent --- http://www.jindent.com
