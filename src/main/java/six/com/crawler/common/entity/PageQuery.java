package six.com.crawler.common.entity;

import java.util.List;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2017年3月10日 上午11:45:02 
* 分页插件
*/
public class PageQuery <T>{
	
	/**
	 * 分页数据list
	 */
	private List<T> list;
	/**
	 * 分页页面索引 从0开始
	 */
	private int pageIndex;
	/**
	 * 分页页面显示数量
	 */
	private int pageSize;
	/**
	 * 总页面数量
	 */
	private int totalPage;
	/**
	 *  总数据数量
	 */
	private int totalSize;
	
	
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getTotalPage() {
		return totalPage;
	}
	public void setTotalPage(int totalPage) {
		this.totalPage = totalPage;
	}
	public int getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}
}
