package com.cai310.lottery.entity.lottery.welfare3d;

import javax.persistence.Entity;
import javax.persistence.Table;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import com.cai310.lottery.entity.lottery.MissDataInfo;

@Table(name = com.cai310.lottery.Constant.LOTTERY_TABLE_PREFIX + "W3D_MISS")
@Entity
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Welfare3dMissDataInfo extends MissDataInfo {

	private static final long serialVersionUID = -2988762963175815079L;

}
