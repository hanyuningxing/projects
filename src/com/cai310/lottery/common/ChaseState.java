package com.cai310.lottery.common;

import com.cai310.lottery.Constant;

/**
 * 追号状态.<br/>
 * 注：添加类型只能在后面添加，不能插入中间
 * 
 */
public enum ChaseState {
	/** 追号中 */
	RUNNING("追号中"),

	/** 已停止 */
	STOPED("已停止");

	private String stateName;

	private ChaseState(String stateName) {
		this.stateName = stateName;
	}

	/**
	 * 获取状态描述
	 * 
	 * @return
	 */
	public String getStateName() {
		return this.stateName;
	}

	/**
	 * 获取状态值
	 * 
	 * @return
	 */
	public byte getStateValue() {
		return (byte) this.ordinal();
	}

	public static final String SQL_TYPE = Constant.ENUM_DEFAULT_SQL_TYPE;
}
