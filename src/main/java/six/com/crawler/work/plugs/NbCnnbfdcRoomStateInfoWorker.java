package six.com.crawler.work.plugs;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import six.com.crawler.entity.Page;
import six.com.crawler.entity.ResultContext;
import six.com.crawler.utils.ArrayListUtils;
import six.com.crawler.work.AbstractCrawlWorker;
import six.com.crawler.work.space.WorkSpace;

/**
 * 
 * @author 38342
 * @version v1.0 date:20170301
 */
public class NbCnnbfdcRoomStateInfoWorker extends AbstractCrawlWorker {

	WorkSpace<Page> roomInfoQueue;
	String iframeCss = "td>iframe[id=mapbarframe]";

	private Map<String, String> roomStates = new HashMap<String, String>();

	@Override
	protected void insideInit() {
		roomInfoQueue = getManager().getWorkSpaceManager().newWorkSpace("nb_cnnbfdc_room_info", Page.class);
	}

	@Override
	protected void beforeDown(Page doingPage) {
	}

	@Override
	protected void beforeExtract(Page doingPage) {
		Element iframe = doingPage.getDoc().select(iframeCss).first();
		if (null == iframe) {
			throw new RuntimeException("don't find iframe:" + iframeCss);
		}
		String src = iframe.attr("src");
		Page iframePage = new Page(doingPage.getSiteCode(), 1, src, src);
		iframePage.setReferer(doingPage.getFinalUrl());
		getDowner().down(iframePage);
		doingPage.setPageSrc(iframePage.getPageSrc());
		if (roomStates.isEmpty()) {
			String allCssQuery = "table[width='600']>tbody>tr>td>font";
			Elements elements2 = doingPage.getDoc().select(allCssQuery);
			if (null == elements2) {
				throw new RuntimeException("don't find state node:" + allCssQuery);
			}
			for (Element ets : elements2) {
				String key = ets.attr("color");
				String value = ets.ownText();
				value = StringUtils.remove(value, ":");
				value = StringUtils.remove(value, " ");
				roomStates.put(key, value);
			}
		}
		String unitId = doingPage.getMeta("unitId").get(0);
		List<String> unitIds = new ArrayList<String>();
		List<String> roomNos = new ArrayList<String>();
		String styleCssQuery = "table[id]>tbody>tr>td>table";
		Elements styleElements = doingPage.getDoc().select(styleCssQuery);
		List<String> roomStateList = new ArrayList<String>();
		List<String> roomIds = new ArrayList<String>();
		List<String> contractNos = new ArrayList<String>();

		Elements rooms = doingPage.getDoc().select("table[id]");
		for (Element room : rooms) {
			String roomId = room.attr("id").replaceAll("room", "");
			roomIds.add(roomId);
		}

		for (Element et : styleElements) {
			String bgColor = "";
			String style = et.attr("style");
			if (style != null) {
				String[] ss = style.split(";");
				for (String string : ss) {
					if (string.contains("background-color")) {
						bgColor = string.split(":")[1];
						break;
					}
				}
			}
			String roomState = roomStates.get(bgColor);
			roomState = roomState.replace(": ", "");
			Elements elements = et.select("tbody>tr:eq(1)>td>a");
			if (elements == null || elements.size() == 0) {
				elements = et.select("tbody>tr:eq(1)>td");
				String contractNo = et.attr("title");
				if (contractNo != null && contractNo.contains("鍚堝悓缂栧彿")) {
					contractNo = contractNo.split("锛�")[1];
				} else {
					contractNo = "";
				}
				contractNos.add(contractNo);
			} else {
				contractNos.add("");
			}
			String s = elements.first().ownText();
			roomNos.add(s);
			roomStateList.add(roomState);
			unitIds.add(unitId);
		}

		doingPage.getMetaMap().put("roomState", roomStateList);
		doingPage.getMetaMap().put("unitId", unitIds);
		doingPage.getMetaMap().put("roomNo", roomNos);
		doingPage.getMetaMap().put("roomId", roomIds);
		doingPage.getMetaMap().put("contractNo", contractNos);
	}

	@Override
	protected void afterExtract(Page doingPage, ResultContext resultContext) {
	}

	@Override
	protected void onComplete(Page doingPage, ResultContext resultContext) {
		String cssQuery = "table[id]";
		Elements roomStateElements = doingPage.getDoc().select(cssQuery);
		List<String> roomNos = resultContext.getExtractResult("roomNo");
		if (roomNos != null) {
			for (int i = 0; i < roomNos.size(); i++) {
				String id = roomStateElements.get(i).attr("id");
				id = id.replace("room", "");
				String pageUrl = "http://newhouse.cnnbfdc.com/openRoomData.aspx?roomId=" + id;
				Page roomPage = new Page(doingPage.getSiteCode(), 1, pageUrl, pageUrl);
				roomPage.getMetaMap().put("projectId", doingPage.getMeta("projectId"));
				roomPage.getMetaMap().put("projectName", doingPage.getMeta("projectName"));
				roomPage.getMetaMap().put("unitName", doingPage.getMeta("unitName"));
				roomPage.getMetaMap().put("unitId", doingPage.getMeta("unitId"));
				roomPage.getMetaMap().put("roomId", ArrayListUtils.asList(id));
				roomPage.setReferer(doingPage.getFinalUrl());
				if (!roomInfoQueue.isDone(roomPage.getPageKey())) {
					roomInfoQueue.push(roomPage);
				}
			}
		}
	}

	@Override
	public boolean insideOnError(Exception t, Page doingPage) {
		return false;
	}
}
