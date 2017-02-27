package six.com.crawler.work.extract;

import java.io.Serializable;

/**
 * @author six
 * @date 2016年8月24日 下午4:53:54
 */
public enum PathType implements Serializable {

	URL, NEXTPAGELINK, TITLE, PUBDATE, AUTHOR, CONTENT, REPLYCONTENT, REPLYAUTHOR, REPLYDATE, REPLYTYPE,

	// 房產字段
	/**
	 * 楼盘名称 所属区县 所在版块 物业地址 采集时间 来源地址 城市
	 */
	COMMON_STRING, BUILDINGNAME, AREA, SECTION, ESTATESADDRESS, COLLECTDATE, SOURCEADDRESS, CITY,

	/**
	 * 企业信息
	 * 
	 */
	TABLE,
	//开发商                          电话               省市区                          详细地址                   主页                法人代表                                         经营范围
 	DEVELOPERS, TELEPHONE, PROVINCIAL_CITYM, ADDRESS, HOME_PAGE, CORPORATE_REPRESENTATIVE,ORERATE_RANGE,
 	//创建日期        注册资本                                  营业期限                           发照日期          状态
 	FOUND_DATE,REGISTERED_CAPITAL,ORETATING_PERIOD,ISSUE_DATE,STATUS,
 	EMPTY;// 空

}
