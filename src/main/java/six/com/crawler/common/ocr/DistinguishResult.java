package six.com.crawler.common.ocr;

import java.util.concurrent.atomic.AtomicInteger;

/** 
* @author  作者 
* @E-mail: 359852326@qq.com 
* @date 创建时间：2016年9月28日 下午12:11:54 
*/
public class DistinguishResult {

	private AtomicInteger score = new AtomicInteger(100);
	private volatile String result;

	public synchronized void setResult(String result) {
		this.result = result;
	}

	public String getResult() {
		return result;
	}
	
	public int getScore(){
		return score.get();
	}
	
	public void setScore(int score){
		this.score.set(score);
	}

}
