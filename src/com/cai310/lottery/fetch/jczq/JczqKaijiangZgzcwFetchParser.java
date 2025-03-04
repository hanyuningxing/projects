package com.cai310.lottery.fetch.jczq;

import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.tags.Div;
import org.htmlparser.tags.TableColumn;
import org.htmlparser.tags.TableRow;
import org.htmlparser.tags.TableTag;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.cai310.lottery.Constant;
import com.cai310.lottery.support.jczq.JczqUtil;
import com.cai310.utils.DateUtil;
import com.cai310.utils.HttpClientUtil;
import com.google.common.collect.Maps;

public class JczqKaijiangZgzcwFetchParser {
	 
	private final static Logger logger = Logger.getLogger( JczqKaijiangFetchParser.class);
	private final String charset = (StringUtils.isNotBlank(getCharset())) ? getCharset() : Constant.DEFAULT_ENCODE;
	 
	protected  Map parserHTML(String matchTime) {
		// TODO Auto-generated method stub
	 
		Map<String,JczqKaijiang> map = Maps.newHashMap();
		Date date = DateUtil.strToDate(matchTime+"","yyyyMMdd");
 
	 
		parse(matchTime,date,map);
	 
		return  map;
	}
	public void parse(String matchTimeStr,Date matchTime,Map<String,JczqKaijiang> map ){
		String sourceUrl = getSourceUrl(matchTimeStr);
		if (StringUtils.isBlank(sourceUrl)) {
			this.logger.warn("抓取的目标网址为空.");
			return;
		}

		
		String html = fetchHTML(sourceUrl, charset);
		if (StringUtils.isBlank(html)) {
			this.logger.warn("抓取回来的内容为空，目标网址：[{}]."+sourceUrl);
			return;
		}
		Double rqspf,spf,zjq,bqc;
		 
		JczqKaijiang k  =null;
		String divHtml  = getDivBy(html,charset,"class","jczq-kjing").getChildrenHTML();
		TableTag table = getTable(divHtml,charset);
		TableRow [] trs = table.getRows();
		String dayOfWeekStr,matchKey;
		for(int i=5;i<trs.length;i++){
			k =new JczqKaijiang();
			TableColumn[] tcs = trs[i].getColumns();
			dayOfWeekStr = tcs[0].getChildrenHTML().trim().substring(1,2);
			matchKey = tcs[0].getChildrenHTML().trim().substring(2);
		 
			Integer matchDate = JczqUtil.getMatchDate(dayOfWeekStr, matchTime);
			k.setMatchKey(matchKey);
			k.setPeriod(matchDate+"");
			try{
				rqspf = Double.valueOf((tcs[8]).getChildrenHTML().trim()); 
			k.setRqspf(rqspf);
			}catch(Exception e){
				k.setRqspf(0);
			}
			try{
				spf = Double.valueOf((tcs[10]).getChildrenHTML().trim()); 
				k.setSpf(spf);
			}catch(Exception e){
				k.setSpf(0);
			}
			try{
				zjq = Double.valueOf(( tcs[13]).getChildrenHTML().trim()); 
				k.setZjq(zjq);
			}catch(Exception e){
				k.setZjq(0);
			}
			try{
				bqc = Double.valueOf(( tcs[15] ).getChildrenHTML().trim()); 
			k.setBqc(bqc);
			}catch(Exception e){
				k.setBqc(0);
			} 
 
			map.put(matchDate+matchKey, k);
		}
	}
	
	
	
	protected TableTag getTable(String html, String charset) {		
		Parser parser = Parser.createParser(html, charset);		
		NodeFilter tableFilter = new NodeClassFilter(TableTag.class);		
		NodeFilter[] filters = new NodeFilter[] { tableFilter };
		OrFilter filter = new OrFilter(filters);
		NodeList nodeList;
		try {
			nodeList = parser.parse(filter);
		} catch (ParserException e) {
			this.logger.warn("解析HTML出错.", e);
			return null;
		}
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.elementAt(i) instanceof TableTag) {
				TableTag tag = (TableTag) nodeList.elementAt(i);		 
			 
					return tag;
				 
			}
		}
		return null;
	}
	 
	public String getCharset() {
		// TODO Auto-generated method stub
		return "gbk";
	}
	/**
	 * 
	 * @param period
	 * 期号：例子  2013-10-07
	 * @return
	 */
	public Map fetch(String period) {
		
	 
		return parserHTML(period);
	}

	protected String fetchHTML(String url, String charset) {
		return HttpClientUtil.getRemoteSource(url, charset);
	}

 
	public String getSourceUrl(String matchTime) {
		// TODO Auto-generated method stub
		return "http://cp.zgzcw.com/dc/getKaijiangFootBall.action?startTime="+matchTime;
	}


	public static void main(String[] args) {//20131012001
		JczqKaijiangZgzcwFetchParser parser = new JczqKaijiangZgzcwFetchParser();
		Map rateData = parser.fetch("20141015");
		Iterator it = rateData.entrySet().iterator();
		while(it.hasNext()){
			try{
			Entry<String, JczqKaijiang> entry = (Entry<String,JczqKaijiang>) it.next();
			JczqKaijiang k= entry.getValue();
		 
			System.out.println("key"+entry.getKey()+"|"+"【"+k.getMatchKey()+" "+k.getPeriod()+"】" +k.getRqspf()+"  "+k.getSpf()+"  "+k.getZjq()
								+"  "+k.getBqc());
			
			}catch(Exception e){
				  
			}
		}
 
		
	}
	public static  Div  getDivBy(String html, String charset,String attributeName,String attributeValue) {		
		Parser parser = Parser.createParser(html, charset);		
		NodeFilter tableFilter = new NodeClassFilter(Div.class);		
		NodeFilter[] filters = new NodeFilter[] { tableFilter };
		OrFilter filter = new OrFilter(filters);
		NodeList nodeList;
		try {
			nodeList = parser.parse(filter);
		} catch (ParserException e) {
			return null;
		}
		for (int i = 0; i < nodeList.size(); i++) {
			if (nodeList.elementAt(i) instanceof Div) {
				Div tag = (Div) nodeList.elementAt(i);
				if(attributeName==null){return tag;}
 				if(attributeValue.equals(tag.getAttribute(attributeName))){					
 					return tag;	 
 				}
 				 
			}
		}
		return null;
	}
	
}
