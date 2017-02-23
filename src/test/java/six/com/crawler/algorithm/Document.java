package six.com.crawler.algorithm;

import java.util.Date;

/**
 * @author six
 * @date 2016年8月1日 下午5:10:45
 */
public class Document {
	private String title;// 标题
	private String author;// 作者
	private Date publishDate;// 出版日期
	private String content;// 内容
	
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
	}
	public Date getPublishDate() {
		return publishDate;
	}
	public void setPublishDate(Date publishDate) {
		this.publishDate = publishDate;
	}
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
