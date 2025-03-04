package com.cai310.lottery.support.jczq;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.cai310.lottery.support.CombinationObj;
import com.cai310.lottery.support.Compound2Single;
import com.cai310.lottery.support.Item;
import com.cai310.lottery.web.controller.WebDataException;
import com.cai310.utils.NumUtils;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

public class PrizeForecast implements Serializable {
	private static final long serialVersionUID = -1511486373061304346L;
	private Map<Integer,Map<Integer,Integer>> passMatchWonUnitsMap = Maps.newHashMap();
	private Map<Integer,double[]> passMatchPrizeMap = Maps.newHashMap(); 
	private Map<Integer,Object[]> passMatchPrizeInfoMap = Maps.newHashMap();
	private PlayType[] playTypes = {PlayType.SPF,PlayType.RQSPF};

	/** 胆码最小命中数 */
	private final Integer danMinHit;

	/** 胆码最大命中数 */
	private final Integer danMaxHit;

	private final List<PassType> passTypes;

	private final List<Integer> passMatchList;

	private final Integer multiple;
	
	private Map<String,Object[]> minMaxSpOfMatchMap;
	
	private int units = 0;
	
	public PrizeForecast(List<String> minSpSortList,List<String> maxSpSortList,Integer multiple, Map<String,List<JczqMatchItem>> matchItemMap, List<PassType> passTypes, List<JczqMatchItem> danList, List<JczqMatchItem> unDanList, Integer danMinHit, Integer danMaxHit, Map<String,Object[]> minMaxSpOfMatchMap, Map<String,List<Item>> excludeMap_max) throws WebDataException {
		List<String> danMatchkeys = Lists.newArrayList();
		for(JczqMatchItem matchItem : danList){
			danMatchkeys.add(matchItem.getMatchKey());
		}
		List<String> unDanMatchkeys = Lists.newArrayList();
		for(JczqMatchItem matchItem : unDanList){
			unDanMatchkeys.add(matchItem.getMatchKey());
		}
		this.multiple = multiple;
		this.minMaxSpOfMatchMap = minMaxSpOfMatchMap;
		if (danMinHit == null || danMinHit > danList.size()){
			this.danMinHit = danList.size();
		}else{
			this.danMinHit = danMinHit;
		}
		if (danMaxHit == null || danMaxHit > danList.size() || danMaxHit < danMinHit){
			this.danMaxHit = danList.size();
		}else{
			this.danMaxHit = danMaxHit;
		}			
		this.passTypes = passTypes;
		List<Integer> passMatchList = new ArrayList<Integer>();
		for (PassType passType : this.passTypes) {
			int[] passMatchArr = passType.getPassMatchs();
			for (int passMatch : passMatchArr) {
				if (!passMatchList.contains(passMatch))
					passMatchList.add(passMatch);
			}
		}
		Collections.sort(passMatchList);
		this.passMatchList = passMatchList;
				
		//统计方案注数
		for(Integer passMatch : passMatchList){
			JczqSchemeConverWork<JczqMatchItem> work = new JczqSchemeConverWork<JczqMatchItem>(passMatch, danList, unDanList, this.danMinHit, this.danMaxHit);
			List<List<JczqMatchItem>> combList = work.getResultList();
			for(List<JczqMatchItem> matchItems : combList){
				int unitsOfItem = 1;
				for(JczqMatchItem matchItem : matchItems){
					List<JczqMatchItem> matchItemsT = matchItemMap.get(matchItem.getMatchKey());
					int matchItemSelectedCount = 0;
					for(JczqMatchItem matchItemT : matchItemsT){
						matchItemSelectedCount += matchItemT.selectedCount();
					}
					unitsOfItem *= matchItemSelectedCount;
				}
				this.units += unitsOfItem;
			}
		}
				
		if (this.danMinHit <= danList.size()) {
			int start = this.passMatchList.get(0);
			int size = matchItemMap.size();
			
			//取过关相关的场次组合
			List<String> danMatchList = Lists.newArrayList();
			for(JczqMatchItem matchItem : danList){
				danMatchList.add(matchItem.getMatchKey());
			}

			Map<Integer,List<List<String>>> matchKeysOfPassMap_min = Maps.newHashMap();
			Map<Integer,List<List<String>>> matchKeysOfPassMap_max = Maps.newHashMap();
			for (int matchSize=start; matchSize<=size; matchSize++) {
				int maxDanSize = this.danMaxHit>matchSize?matchSize:this.danMaxHit;
				int subListSize = matchSize-(danList.size()-this.danMinHit);
				int realSubSize = subListSize<minSpSortList.size()?subListSize:minSpSortList.size();
				List<String> UnDanMatchList = minSpSortList.subList(0, realSubSize);
				CombinationObj comMatchKeys = new CombinationObj(matchSize, danMatchList, UnDanMatchList, this.danMinHit, maxDanSize);
				List<List<String>> matchKeysList = comMatchKeys.getResultList();				
				matchKeysOfPassMap_min.put(matchSize, matchKeysList);
				
				UnDanMatchList = maxSpSortList.subList(0, realSubSize);
				comMatchKeys = new CombinationObj(matchSize, danMatchList, UnDanMatchList, this.danMinHit, maxDanSize);
				matchKeysList = comMatchKeys.getResultList();				
				matchKeysOfPassMap_max.put(matchSize, matchKeysList);
			}
			
			Iterator<Entry<Integer,List<List<String>>>> itorOfPassMatchKeys = matchKeysOfPassMap_min.entrySet().iterator();
			while(itorOfPassMatchKeys.hasNext()){
				Entry<Integer,List<List<String>>> matchkeysEntry = itorOfPassMatchKeys.next();
				List<List<String>> matchkeysList = matchkeysEntry.getValue();
				double minPrizeOfPass = 0d;				
				int index = 0;
				int minIndexFlag=0;
				for(List<String> matchkeys : matchkeysList){
					double[] minMaxPrize = this.countComOfMatchsPrize(matchItemMap, matchkeys, minMaxSpOfMatchMap);
					double minPrize = minMaxPrize[0];
					if(minPrizeOfPass==0 || minPrizeOfPass>minPrize){
						minPrizeOfPass=minPrize;
						minIndexFlag=index;
					}
					index++;
				}
				
				Object[] danInfoArr = this.findDanInfo(danMatchkeys, unDanMatchkeys, matchkeysList.get(minIndexFlag), danMinHit, danMaxHit);
				this.countPassMatchInfoOfMin(matchkeysEntry.getKey(),matchItemMap,danInfoArr,minMaxSpOfMatchMap);			
			}	
			
			itorOfPassMatchKeys = matchKeysOfPassMap_max.entrySet().iterator();
			while(itorOfPassMatchKeys.hasNext()){
				Entry<Integer,List<List<String>>> matchkeysEntry = itorOfPassMatchKeys.next();
				List<List<String>> matchkeysList = matchkeysEntry.getValue();
				double maxPrizeOfPass = 0d;				
				int index = 0;
				int maxIndexFlag=0;
				for(List<String> matchkeys : matchkeysList){
					double[] minMaxPrize = this.countComOfMatchsPrize(matchItemMap, matchkeys, minMaxSpOfMatchMap);					
					double maxPrize = minMaxPrize[1];
					if(maxPrizeOfPass==0 || maxPrizeOfPass<maxPrize){
						maxPrizeOfPass=maxPrize;
						maxIndexFlag=index;
					}
					index++;
				}				
				Object[] danInfoArr = this.findDanInfo(danMatchkeys, unDanMatchkeys, matchkeysList.get(maxIndexFlag), danMinHit, danMaxHit);
				this.countPassMatchInfoOfMax(matchkeysEntry.getKey(),matchItemMap,danInfoArr,minMaxSpOfMatchMap,excludeMap_max);				
			}	
						
//			Iterator<Entry<Integer,Map<Integer,Integer>>> itor1 = passMatchWonUnitsMap.entrySet().iterator();
//			while(itor1.hasNext()){
//				Entry<Integer,Map<Integer,Integer>> unitEntry = itor1.next();
//				Iterator<Entry<Integer,Integer>> itor3 = unitEntry.getValue().entrySet().iterator();
//				while(itor3.hasNext()){
//					Entry<Integer,Integer> unitPassMatchEntry = itor3.next();
//					System.out.println(unitEntry.getKey()+"场 串"+ unitPassMatchEntry.getKey() + " " + unitPassMatchEntry.getValue());
//				}					
//			}
//			
//			Iterator<Entry<Integer,double[]>> itor2 = passMatchPrizeMap.entrySet().iterator();
//			while(itor2.hasNext()){
//				Entry<Integer,double[]> unitEntry = itor2.next();
//				System.out.println(unitEntry.getKey()+"场 "+ unitEntry.getValue()[0] + " " + unitEntry.getValue()[1]);
//			}	
			
//			Iterator<Entry<Integer,Object[]>> itor4 = passMatchPrizeInfoMap.entrySet().iterator();
//			while(itor4.hasNext()){
//				Entry<Integer,Object[]> entry = itor4.next();
//				Object[] values = entry.getValue();
//				for(int i=0;i<values.length;i++){
//					Map<Integer,List<String>> prizeInfoMap = (Map<Integer,List<String>>)values[i];
//					Iterator<Entry<Integer,List<String>>> itor5 = prizeInfoMap.entrySet().iterator();
//					while(itor5.hasNext()){
//						Entry<Integer,List<String>> entryT = itor5.next();
//						List<String> results = entryT.getValue();
//						for(String result : results){
//							System.out.println(entry.getKey()+"场"+i+"：串"+entryT.getKey()+",内容："+result);
//						}
//					}
//				}
//			}
			
			
		}
	}
	
	/**
	 * 获取胆的相关信息
	 * @param danMatchkeys
	 * @param unDanMatchkeys
	 * @param comMatchkeys
	 * @param danMinHit
	 * @param danMaxHit
	 * @return
	 */
	private Object[] findDanInfo(List<String> danMatchkeys,List<String> unDanMatchkeys,List<String> comMatchkeys,Integer danMinHit,Integer danMaxHit){		
		List<String> comMatchkeys_dan = Lists.newArrayList();
		List<String> comMatchkeys_unDan = Lists.newArrayList();
		for(String matchkey : comMatchkeys){
			if(danMatchkeys.contains(matchkey)){
				comMatchkeys_dan.add(matchkey);
			}
			if(unDanMatchkeys.contains(matchkey)){
				comMatchkeys_unDan.add(matchkey);
			}
		}
		Integer danMinHitCom = null;
		Integer danMaxHitCom = null;
		if (danMinHit==null || danMinHit > comMatchkeys_dan.size()){
			danMinHitCom = comMatchkeys_dan.size();
		}else{
			danMinHitCom = danMinHit;
		}
		if (danMaxHit == null || danMaxHit > comMatchkeys_dan.size() || danMaxHit < danMinHit){
			danMaxHitCom = comMatchkeys_dan.size();
		}else{
			danMaxHitCom = danMaxHit;
		}
		Object[] danInfoArr = new Object[4]; 
		danInfoArr[0] = comMatchkeys_dan;
		danInfoArr[1] = comMatchkeys_unDan;
		danInfoArr[2] = danMinHitCom;
		danInfoArr[3] = danMaxHitCom;
		return danInfoArr;
	}
	
	/**
	 * 计算场次过关的最小奖金组合信息
	 * @param wonMatchCount
	 * @param matchItemMap
	 * @param matchkeys
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void countPassMatchInfoOfMin(final Integer wonMatchCount,final Map<String,List<JczqMatchItem>> matchItemMap,final Object[] danInfoArr,final Map<String,Object[]> minMaxSpOfMatchMap){
		Map<Integer,Object[]> minPrizeInfoOfPassMatchMap = Maps.newHashMap();
		double[] minMaxPrize = passMatchPrizeMap.get(wonMatchCount);
		if(minMaxPrize==null){
			minMaxPrize = new double[]{0,0};
		}
		Object[] minMaxPrizeInfo = passMatchPrizeInfoMap.get(wonMatchCount);
		if(minMaxPrizeInfo==null){
			minMaxPrizeInfo = new Object[2];
		}
		for(int passMatch : this.passMatchList){
			if(passMatch>wonMatchCount)break;
			CombinationObj combMatchkeys = new CombinationObj(passMatch,(List)danInfoArr[0],(List)danInfoArr[1],(Integer)danInfoArr[2],(Integer)danInfoArr[3]);
			List<List<String>> matchkeysList = combMatchkeys.getResultList();
			List<String> prizeInfoList = Lists.newArrayList();
			double totalPrizeOfPassMatch = 0.0;
			for(List<String> matchkeysT : matchkeysList){
				double[] minMaxPrizeOfItem = this.countComOfMatchsPrize(matchItemMap, matchkeysT, minMaxSpOfMatchMap);
				minMaxPrize[0]+=minMaxPrizeOfItem[0];
				Object[] wonInfoArr = this.buildWonContent(matchkeysT,false);
				prizeInfoList.addAll((List<String>)wonInfoArr[0]);
				totalPrizeOfPassMatch+=(Double)wonInfoArr[1];
			}
			Object[] prizeInfoArr = new Object[2];
			prizeInfoArr[0] = prizeInfoList;
			prizeInfoArr[1] = totalPrizeOfPassMatch;
			minPrizeInfoOfPassMatchMap.put(passMatch, prizeInfoArr);
		}
		minMaxPrizeInfo[0]=minPrizeInfoOfPassMatchMap;
		passMatchPrizeMap.put(wonMatchCount, minMaxPrize);
		passMatchPrizeInfoMap.put(wonMatchCount, minMaxPrizeInfo);
	}	
	
	/**
	 * 计算场次过关的最大奖金组合信息
	 * @param wonMatchCount
	 * @param matchItemMap
	 * @param matchkeys
	 * @return
	 */
	@SuppressWarnings({ "unchecked", "rawtypes" })
	private void countPassMatchInfoOfMax(final Integer wonMatchCount,final Map<String,List<JczqMatchItem>> matchItemMap,final Object[] danInfoArr,final Map<String,Object[]> minMaxSpOfMatchMap,final Map<String,List<Item>> excludeMap_max){
		
		Map<Integer,Integer> unitsOfPassMatchMap = Maps.newHashMap();
		Map<Integer,Object[]> maxPrizeInfoOfPassMatchMap = Maps.newHashMap();
		double[] minMaxPrize = passMatchPrizeMap.get(wonMatchCount);
		if(minMaxPrize==null){
			minMaxPrize = new double[]{0,0};
		}
		Object[] minMaxPrizeInfo = passMatchPrizeInfoMap.get(wonMatchCount);
		if(minMaxPrizeInfo==null){
			minMaxPrizeInfo = new Object[2];
		}
		for(int passMatch : this.passMatchList){
			if(passMatch>wonMatchCount)break;
			int combCount = 0;
			CombinationObj combMatchkeys = new CombinationObj(passMatch,(List)danInfoArr[0],(List)danInfoArr[1],(Integer)danInfoArr[2],(Integer)danInfoArr[3]);
			List<List<String>> matchkeysList = combMatchkeys.getResultList();
			List<String> prizeInfoList = Lists.newArrayList();
			double totalPrizeOfPassMatch = 0.0;
			for(List<String> matchkeysT : matchkeysList){
				int units=1;
				for(String matchkey : matchkeysT){
					int excludeUnit = this.isExcludeMatchItem(matchkey, excludeMap_max, matchItemMap)?-1:0;
					units *= matchItemMap.get(matchkey).size()+excludeUnit;
				}
				combCount += units;
				double[] minMaxPrizeOfItem = this.countComOfMatchsPrize(matchItemMap, matchkeysT, minMaxSpOfMatchMap);
				minMaxPrize[1]+=minMaxPrizeOfItem[1];
				Object[] wonInfoArr = this.buildWonContent(matchkeysT,true);
				prizeInfoList.addAll((List<String>)wonInfoArr[0]);
				totalPrizeOfPassMatch+=(Double)wonInfoArr[1];
			}
			unitsOfPassMatchMap.put(passMatch, combCount);
			Object[] prizeInfoArr = new Object[2];
			prizeInfoArr[0] = prizeInfoList;
			prizeInfoArr[1] = totalPrizeOfPassMatch;
			maxPrizeInfoOfPassMatchMap.put(passMatch, prizeInfoArr);
		}
		minMaxPrizeInfo[1]=maxPrizeInfoOfPassMatchMap;
		passMatchWonUnitsMap.put(wonMatchCount, unitsOfPassMatchMap);
		passMatchPrizeMap.put(wonMatchCount, minMaxPrize);
		passMatchPrizeInfoMap.put(wonMatchCount, minMaxPrizeInfo);
	}	
	
	/**
	 * 检查场次是否因为胜平负及让球胜平负而排除一个中奖项(最大中奖排他项)
	 * @param matchkey
	 * @param excludeMap_max
	 * @return
	 */
	private boolean isExcludeMatchItem(String matchkey,final Map<String,List<Item>> excludeMap_max,final Map<String,List<JczqMatchItem>> matchItemMap){
		if(excludeMap_max==null)return false;
		List<Item> excludeItems = null;
		PlayType playType = null;
		for(PlayType pt : playTypes){
			excludeItems = excludeMap_max.get(matchkey+pt);
			if(excludeItems!=null){
				playType=pt;
				break;
			}
		}
		if(excludeItems==null || playType==null)return false;
		List<JczqMatchItem> matchItems = matchItemMap.get(matchkey);
		JczqMatchItem matchItemOfExclude = null;
		for(JczqMatchItem matchItem : matchItems){
			if(matchItem.getPlayType()==playType){
				matchItemOfExclude = matchItem;
			}
		}
		if(matchItemOfExclude==null)return false;
		
		int selectedCountOfExclude = 0;
		for(Item item : excludeItems){
			if(matchItemOfExclude.hasSelect(item)){
				selectedCountOfExclude++;
			}
		}
		if(selectedCountOfExclude==matchItemOfExclude.selectedCount()){//选中项都被排除
			return true;
		}
		return false;
	}
	
	/**
	 * 构建场次组合的中奖详情
	 */
	private Object[] buildWonContent(final List<String> matchkeys,final boolean isMax){
		List<String> contentList = Lists.newArrayList();
		StringBuffer prizeContent = new StringBuffer();
		double countPrize = 0.0;
		if(isMax){
			Object[][] matchArr = new Object[matchkeys.size()][];
			int index=0;
			for(String matchkey : matchkeys){
				Object[] minMaxSp= this.minMaxSpOfMatchMap.get(matchkey);
				Map<PlayType,Double> maxSpOfMatch = (Map<PlayType,Double>)minMaxSp[1];
				Object[] matchItemArr = new Object[maxSpOfMatch.size()];
				Iterator<Entry<PlayType,Double>> itor = maxSpOfMatch.entrySet().iterator();
				int f=0;
				while(itor.hasNext()){
					Entry<PlayType,Double> entry = itor.next();
					matchItemArr[f]=entry.getValue();
					f++;
				}
				matchArr[index] = matchItemArr;
				index++;
			}
			//组合后投注内容数组
			Object[][] objectsArrays = Compound2Single.assembleArraysToPlanerArray(matchArr);
			for (Object[] objArrays : objectsArrays) {
				double prize = 2.0 * this.multiple;
				prizeContent.setLength(0);
				for(Object item: objArrays){
					prize *= (Double)item;
					prizeContent.append((Double)item).append("*");
				}
				prizeContent.append(2.0).append("*").append(multiple).append("=").append(NumUtils.format(prize, 2, 2));
				if(prize>0){
					contentList.add(prizeContent.toString());
					countPrize+=prize;
				}					
			}			
		}else{
			double prize = 2.0 * this.multiple;
			for(String matchkey : matchkeys){
				Object[] minMaxSp= this.minMaxSpOfMatchMap.get(matchkey);
				double minSpOfMatch = (Double)minMaxSp[0];
				prize *= minSpOfMatch;
				prizeContent.append(minSpOfMatch).append("*");
			}
			prizeContent.append(2.0).append("*").append(multiple).append("=").append(NumUtils.format(prize, 2, 2));
			if(prize>0){
				contentList.add(prizeContent.toString());
				countPrize+=prize;
			}
		}
		Object[] wonInfoArr = new Object[2];
		wonInfoArr[0]=contentList;
		wonInfoArr[1]=countPrize;
		return wonInfoArr;
	}
	
	/**
	 * 计算每个场次组合的最大最小奖金
	 * @param matchItemMap
	 * @param matchkeys
	 */
	private double[] countComOfMatchsPrize(final Map<String,List<JczqMatchItem>> matchItemMap,final List<String> matchkeys, final Map<String,Object[]> minMaxSpOfMatchMap){		
		Object[] minMaxSp = null;
		double minPrize = 2.0*multiple;
		double maxPrize = 2.0*multiple;
		for(String matchkey : matchkeys){
			minMaxSp = minMaxSpOfMatchMap.get(matchkey);
			
			//最小奖金
			double minSpOfMatch = (Double)minMaxSp[0];
			minPrize *= minSpOfMatch;
			
			//最大奖金
			Map<PlayType,Double> maxSpOfPlayTypeMap = (Map<PlayType,Double>)minMaxSp[1];
			Iterator<Entry<PlayType,Double>> itorMaxSp = maxSpOfPlayTypeMap.entrySet().iterator();
			double sumSp = 0.0;
			while(itorMaxSp.hasNext()){
				sumSp += itorMaxSp.next().getValue();
			}
			maxPrize *= sumSp;
		}
		double[] minMaxPrize = new double[2];
		minMaxPrize[0] = minPrize;
		minMaxPrize[1] = maxPrize;
		return minMaxPrize;
	}

	public Map<Integer, Map<Integer, Integer>> getPassMatchWonUnitsMap() {
		return passMatchWonUnitsMap;
	}

	public void setPassMatchWonUnitsMap(
			Map<Integer, Map<Integer, Integer>> passMatchWonUnitsMap) {
		this.passMatchWonUnitsMap = passMatchWonUnitsMap;
	}

	public Map<Integer, double[]> getPassMatchPrizeMap() {
		return passMatchPrizeMap;
	}

	public void setPassMatchPrizeMap(Map<Integer, double[]> passMatchPrizeMap) {
		this.passMatchPrizeMap = passMatchPrizeMap;
	}

	public Map<Integer, Object[]> getPassMatchPrizeInfoMap() {
		return passMatchPrizeInfoMap;
	}

	public void setPassMatchPrizeInfoMap(
			Map<Integer, Object[]> passMatchPrizeInfoMap) {
		this.passMatchPrizeInfoMap = passMatchPrizeInfoMap;
	}

	public Map<String, Object[]> getMinMaxSpOfMatchMap() {
		return minMaxSpOfMatchMap;
	}

	public void setMinMaxSpOfMatchMap(Map<String, Object[]> minMaxSpOfMatchMap) {
		this.minMaxSpOfMatchMap = minMaxSpOfMatchMap;
	}

	public List<Integer> getPassMatchList() {
		return passMatchList;
	}

	public Integer getMultiple() {
		return multiple;
	}

	public List<PassType> getPassTypes() {
		return passTypes;
	}
	
	public int getCost() {
		return this.multiple * this.units * 2;
	}
}
