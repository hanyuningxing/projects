package com.cai310.lottery.dto.user;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Transient;

import com.cai310.lottery.common.AgentLotteryType;
import com.cai310.utils.DateUtil;


public class AgentSumDTO{
	    private BigDecimal luck_money;
	    // 充值，提现，投注，返点，撤单，追号
		private Long userId;
		private String userName;
		private String realName;
		private BigDecimal ips_money;
		private BigDecimal drawing_money;
		private BigDecimal prize_money;
		private BigDecimal bet_money;
		private BigDecimal remainMoney;
		/** 用户的返点*/
		private Double rebate;
		private BigDecimal rebate_money;
	
	/**下属用户总数**/
	private Long sumUser;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getSumUser() {
		return sumUser;
	}

	public void setSumUser(Long sumUser) {
		this.sumUser = sumUser;
	}

	public String getUserName() {
		return userName;
	}

	public void setUserName(String userName) {
		this.userName = userName;
	}

	public String getRealName() {
		return realName;
	}

	public void setRealName(String realName) {
		this.realName = realName;
	}

	public BigDecimal getIps_money() {
		return ips_money;
	}

	public void setIps_money(BigDecimal ips_money) {
		this.ips_money = ips_money;
	}

	public BigDecimal getDrawing_money() {
		return drawing_money;
	}

	public void setDrawing_money(BigDecimal drawing_money) {
		this.drawing_money = drawing_money;
	}

	public BigDecimal getPrize_money() {
		return prize_money;
	}

	public void setPrize_money(BigDecimal prize_money) {
		this.prize_money = prize_money;
	}

	public BigDecimal getBet_money() {
		return bet_money;
	}

	public void setBet_money(BigDecimal bet_money) {
		this.bet_money = bet_money;
	}

	public Double getRebate() {
		return rebate;
	}

	public void setRebate(Double rebate) {
		this.rebate = rebate;
	}

	public BigDecimal getRebate_money() {
		return rebate_money;
	}

	public void setRebate_money(BigDecimal rebate_money) {
		this.rebate_money = rebate_money;
	}

	
	@Transient
	public BigDecimal getYingkui(){
		BigDecimal yingkui= BigDecimal.ZERO;
		if(null!=this.luck_money)yingkui=yingkui.add(luck_money);
		if(null!=this.prize_money)yingkui=yingkui.add(prize_money);
		if(null!=this.rebate_money)yingkui=yingkui.add(rebate_money);
		if(null!=this.bet_money)yingkui=yingkui.subtract(bet_money);
		return yingkui;
	}

	public BigDecimal getLuck_money() {
		return luck_money;
	}

	public void setLuck_money(BigDecimal luck_money) {
		this.luck_money = luck_money;
	}

	public BigDecimal getRemainMoney() {
		return remainMoney;
	}

	public void setRemainMoney(BigDecimal remainMoney) {
		this.remainMoney = remainMoney;
	}

	
	
}