package six.com.common;

import java.awt.image.ColorModel;
import java.awt.image.PixelGrabber;

import javax.swing.ImageIcon;

public class Pic {
	private int height,width;//图片的高和宽 w h
	private int[] alpha, r, g, b; //存alpha通道数值 和RGB数值
	private int[] array;//原图像像素存储数组pix
	private int[] grayArray;  //灰度化图像存储数组 RGB转灰度(可直接输出图像）
	private int[] grayArray1; //灰度存储数组 RGB转灰度（不可直接输出图像）
	private int[] frequencyArray1 = new int[256];;//统计灰度分布的存储数组frequency
	private int[] poiseGrayArray;//9*hp aim
	private int[] frequencyArray2;//计算处理后的图像灰度分布数组
	private int[] aimPic;//目标图像的像素存储数组
	private int[] aimPic1;//别人程序中的算法 看不懂-。-就直接拿来用
	private int[] removedNoise1; //去噪后的数组（均值去噪）8领域 边界不处理
	private int[] removedNoise2;
	private int[] removedNoise3;
	private int[] removedNoise1_1;//去噪后的数组（中值去噪）8领域 边界不处理（不可直接输出图像）
	private int[] removedNoise2_2;
	private int[] removedNoise3_3;
	private String path = null;
    private ImageIcon file;
    public ImageIcon getFile(){
		return file;
	}
	public String getPath(){
		return path;
	}
	public int[] getArray() {
		return array;
	}
	public int[] getGrayArray() {
		return grayArray;
	}
	public int[] getGrayArray1() {
		return grayArray1;
	}
	public int[] getPoiseGrayArray() {
		return poiseGrayArray;
	}
	public int[] getFrequencyArray1() {
		return frequencyArray1;
	}
	public int[] getFrequencyArray2() {
		return frequencyArray2;
	}
	public int[] getAimPic1(){
		return aimPic1;
	}
	public int[] getAimPic(){
		return aimPic;
	}
	public int[] getRemovedNoise1(){
		return removedNoise1;
	}
	public int[] getRemovedNoise1_1(){
		return removedNoise1_1;
	}
	public int[] getRemovedNoise2(){
		return removedNoise2;
	}
	public int[] getRemovedNoise2_2(){
		return removedNoise2_2;
	}
	public int[] getRemovedNoise3(){
		return removedNoise3;
	}
	public int[] getRemovedNoise3_3(){
		return removedNoise3_3;
	}
	public int getHeight() {
		return height;
	}
	public int getWidth() {
		return width;
	}
	public void setFile(){
		this.file =new ImageIcon(this.path);
	}
	public void setPath(String path){
		this.path = path;
	}
	public void setHeight() {
		this.height=file.getImage().getHeight(null);
	}
	public void setWidth() {
		this.width=file.getImage().getWidth(null);
	}
	public void setArray() {
		this.array = new int[width * height];
		PixelGrabber pg = null;
		try {
			pg = new PixelGrabber(this.file.getImage(), 0, 0, this.width, this.height, this.array, 0, this.width);
			if (pg.grabPixels() != true)
				try {
					throw new java.awt.AWTException("pg error" + pg.status());
				} catch (Exception eq) {
					eq.printStackTrace();
				}
		} catch (Exception ex) {
			ex.printStackTrace();

		}
	}
	public void setGrayArray() {
		this.grayArray = new int[this.height * this.width];
		this.grayArray1 = new int[this.height * this.width];
		this.alpha = new int[this.height * this.width];
		ColorModel colorModel = ColorModel.getRGBdefault();
		r = new int[this.height * this.width];
		g = new int[this.height * this.width]; 
		b = new int[this.height * this.width];
		int i, j, k,gray;
		for (i = 0; i < this.height; i++) {
			for (j = 0; j < this.width; j++) {
				k = i * this.width + j;
				r[k] = colorModel.getRed(this.array[k]);
				g[k] = colorModel.getGreen(this.array[k]);
				b[k] = colorModel.getBlue(this.array[k]);
				alpha[k] = colorModel.getAlpha(this.array[k]);
				gray = (int) (r[k] * 0.3 + g[k] * 0.59 + b[k] * 0.11);
				this.grayArray[i * this.width + j] = (255 << 24) | (gray << 16) | (gray << 8) | gray;
				this.grayArray1[i * this.width + j] = gray;
			}
		}
	}
	public void setPoiseGrayArray() {
		double[] statistical = new double[256];
		double[] hp = new double[256];
		this.poiseGrayArray = new int[256];
		for(int i=0;i<this.frequencyArray1.length;i++){
			statistical[i] = (this.frequencyArray1[i]/((double)(this.width*this.height)));//求占百分比
			if(i==0) hp[0]=statistical[0];//求累计分布
			else hp[i]=hp[i-1]+statistical[i];
		}
		for(int i=0;i<hp.length;i++){
			this.poiseGrayArray[i] = (int)(255*hp[i]);
		}
	}
	public void setFrequencyArray1() {
		for (int i = 0; i < this.grayArray1.length; i++) {
			this.frequencyArray1[this.grayArray1[i]]++;
		}
	}
	public void setFrequencyArray2() {
		this.frequencyArray2 = new int[256];
		for (int i = 0; i < this.frequencyArray1.length; i++) {
			for(int j = 0; j<this.poiseGrayArray.length; j++){
			    if(this.poiseGrayArray[j]==i){		    	
				    this.frequencyArray2[i]=this.frequencyArray2[i]+this.frequencyArray1[j];
			    }
			}
		}
	}
	public void setAimPic(){
		int[] a = new int [this.height*this.width];
		this.aimPic = new int[this.height*this.width];
		for (int i = 0; i < this.array.length; i++){
			a[i] = this.poiseGrayArray[this.grayArray1[i]];//下面式子的简写-。-（poiseGrayArray其实就是个映射函数）
			this.aimPic[i] = 255<<24|a[i]<<16|a[i]<<8|a[i];
			/*for(int j = 0; j < this.poiseGrayArray.length; j++){
				if(this.grayArray[i]==j){
					this.aimPic[i] = this.poiseGrayArray[j];
				}
			}*/	
		}
	}
	public void setRemovedNoise1(){
		this.removedNoise1 = new int[this.height*this.width];
		this.removedNoise1_1 = new int[this.height*this.width];
		int r,g,b,gray;
		for(int i = 0; i < this.height; i++){
			for(int j = 1; j <= this.width; j++){
				r=0;g=0;b=0;
				if(i==0||i==this.height-1||j==1||j==this.width){
					r= this.r[i*this.width+j-1];
					g= this.g[i*this.width+j-1];
					b= this.b[i*this.width+j-1];
					gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
					this.removedNoise1[i*this.width+j-1] = (255 << 24) | (r << 16) | (g << 8) | b;
					this.removedNoise1_1[i*this.width+j-1]=gray;
					//this.removedNoise[i*this.width+j-1] = (255 << 24) | (this.r[i*this.width+j-1] << 16) | (this.g[i*this.width+j-1] << 8) | this.b[i*this.width+j-1];
				}
				else{
					for(int m = i-1; m <= i+1; m++)
						for(int n = j-1; n <= j+1; n++) {
							r =r+this.r[m*this.width+n-1];
							g =g+this.g[m*this.width+n-1];
							b =b+this.b[m*this.width+n-1];
						}
					r = r/9;	
					g = g/9;
					b = b/9;
					gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
					this.removedNoise1[i*this.width+j-1] =(255 << 24) | (r << 16) | (g << 8) | b;
					this.removedNoise1_1[i*this.width+j-1]=gray;
					//this.removedNoise[i*this.width+j-1] =(255 << 24) | (this.r[i*this.width+j-1] << 16) | (this.g[i*this.width+j-1] << 8) | this.b[i*this.width+j-1];
				}				
			}
		}
	}
	public void setRemovedNoise3(){
		this.removedNoise3 = new int[this.height*this.width];
		this.removedNoise3_3 = new int[this.height*this.width];
		int r,g,b,gray;
		for(int i = 0; i < this.height; i++){
			for(int j = 1; j <= this.width; j++){
				r=0;g=0;b=0;
				if(i==0||i==this.height-1||j==1||j==this.width){
					r= this.r[i*this.width+j-1];
					g= this.g[i*this.width+j-1];
					b= this.b[i*this.width+j-1];
					gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
					this.removedNoise3[i*this.width+j-1] = (255 << 24) | (r << 16) | (g << 8) | b;
					this.removedNoise3_3[i*this.width+j-1]=gray;
					//this.removedNoise[i*this.width+j-1] = (255 << 24) | (this.r[i*this.width+j-1] << 16) | (this.g[i*this.width+j-1] << 8) | this.b[i*this.width+j-1];
				}
				else{
					if(this.r[i*this.width+j-1]*0.3+this.g[i*this.width+j-1]*0.59+this.b[i*this.width+j-1]*0.11>200){
					    for(int m = i-1; m <= i+1; m++)
						    for(int n = j-1; n <= j+1; n++) {
						    	
							    r =r+this.r[m*this.width+n-1];
							    g =g+this.g[m*this.width+n-1];
						    	b =b+this.b[m*this.width+n-1];
						    }
				        r = (r-this.r[i*this.width+j-1])/8;	
				        g = (g-this.g[i*this.width+j-1])/8;
				        b = (b-this.b[i*this.width+j-1])/8;
					    gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
					    this.removedNoise3[i*this.width+j-1] =(255 << 24) | (r << 16) | (g << 8) | b;
					    this.removedNoise3_3[i*this.width+j-1]=gray;
					}
					else{
						r = this.r[i*this.width+j-1];	
						g = this.g[i*this.width+j-1];
						b = this.b[i*this.width+j-1];
						gray = (int) (r * 0.3 + g * 0.59 + b * 0.11);
						this.removedNoise3[i*this.width+j-1] =(255 << 24) | (r << 16) | (g << 8) | b;
						this.removedNoise3_3[i*this.width+j-1]=gray;
						}
					//this.removedNoise[i*this.width+j-1] =(255 << 24) | (this.r[i*this.width+j-1] << 16) | (this.g[i*this.width+j-1] << 8) | this.b[i*this.width+j-1];
				}				
			}
		}
	}
	public void setRemovedNoise2(){
		this.removedNoise2 = new int[this.height*this.width];
		this.removedNoise2_2 = new int[this.height*this.width];
		int[] a ;
		int b, c, d,gray;
		for(int i = 0; i < this.height; i++){
			for(int j = 1; j <= this.width; j++){
				a = new int[9];
				b = 0 ;d = 0;
				if(i==0||i==this.height-1||j==1||j==this.width){
					this.removedNoise2[i*this.width+j-1] = this.array[i*this.width+j-1];
					this.removedNoise2_2[i*this.width+j-1] = this.grayArray1[i*this.width+j-1];
				}
				else{
					for(int m = i-1; m <= i+1; m++)
						for(int n = j-1; n <= j+1; n++) {
							a[b++] = this.grayArray1[m*this.width+n-1];
						}
					for(int m = 0; m < 9 ; m++ )
						for(int n = m; n < 9; n++){
							if(a[m] < a[n]){
								c = a[m]; a[m] = a[n]; a[n] = c;
							}
						}
					for(int m = i-1; m <= i+1; m++)
						for(int n = j-1; n <= j+1; n++) {
							if(a[4]==this.grayArray1[m*this.width+n-1])
								d = m*this.width+n-1;
						}
					this.removedNoise2[i*this.width+j-1] = this.array[d];
					this.removedNoise2_2[i*this.width+j-1] = this.grayArray1[d];
				}
			}
		}
	}
	private void setAimPic1(){
		  int[] srcPixArray = this.grayArray;
		  this.aimPic1 = new int[this.height*this.width];
	      int[] histogram=new int[256];
	      int[] dinPixArray=new int[this.width*this.height];
	        
	      for(int i=0;i<this.height;i++){
	       for(int j=0;j<this.width;j++){
	        int grey=srcPixArray[i*this.width+j]&0xff;
	        histogram[grey]++;
	        this.frequencyArray1[grey]++;
	       }
	      }
	      double a=(double)255/(this.width*this.height);
	      double[] c=new double[256];
	      c[0]=(a*histogram[0]);
	      for(int i=1;i<256;i++){
	       c[i]=c[i-1]+(int)(a*histogram[i]);
	      }
	      for(int i=0;i<this.height;i++){
	       for(int j=0;j<this.width;j++){
	        int grey=srcPixArray[i*this.width+j]&0x0000ff;
	        int hist=(int)c[grey];
	      
	        this.aimPic1[i*this.width+j]=dinPixArray[i*this.width+j]=255<<24|hist<<16|hist<<8|hist;
	       }
	      }
	}
	public Pic(){
		this.path = null;
		this.height = 0;
		this.width = 0;
		this.array = null;
		this.grayArray = null; 
	    this.grayArray1 = null;
		this.frequencyArray2 = null;
		this.poiseGrayArray = null;
		this.frequencyArray2 = null;
		this.aimPic = null;
		this.removedNoise1 = null;
		this.removedNoise2 = null;
		this.removedNoise3 = null;
		this.file = null;
		this.aimPic1 = null;
	}
	public Pic(String path){
		setPath(path);
		setFile();
		setHeight(); //取得图片高
        setWidth();  //取得图片宽
        setArray();  //取得图片像素数组（原）
        setGrayArray();        //将彩色数组灰度化（原）
        setFrequencyArray1();//统计灰度分布（原）
        setPoiseGrayArray();         //目标图像灰度级（统计数组）9*hp
        setFrequencyArray2();//目标图像的灰度分布
        setAimPic();                 //创建灰度增强后的图片数组
        setRemovedNoise1();//创建图像均值去噪后的数组
        setRemovedNoise2();
        setRemovedNoise3();
        setAimPic1();
	}
	public int[] change(int [] Source){//为了在屏幕中正常显示 缩小数组
		int max = 0;
		for (int i = 0; i < Source.length; i++) {
			if(Source[i]>max){
				max = Source[i];
			}
		}
		for (int i = 0; i < Source.length; i++) {
			Source[i] = (int) ( 1.0*Source[i]/max *300);
		}
		return Source;
	}
	public static void main(String[] argv){  //Pic类的测试函数 已经全部通过了 ioi
	    //Pic pic = new Pic("d:\\int.jpg");
		//System.out.println(pic.height+"***"+pic.width);
		//System.out.println(pic.path);
		/*for(int i=0;i<(pic.width*pic.height);i++){
	    	System.out.println(pic.array[i]+"###"+i);
		}*/
	    /*for(int i = 0; i<pic.grayArray.length; i++){
	    	System.out.println(pic.grayArray[i]+"###"+i);
	    }*/
		/*for(int i = 0; i<pic.frequencyArray1.length; i++){
	    	System.out.println(pic.frequencyArray1[i]+"###"+i);
	    }*/
		/*for(int i = 0; i<pic.poiseGrayArray.length; i++){
	    	System.out.println(pic.poiseGrayArray[i]+"###"+i);
	    }*/
		/*for(int i = 0; i<pic.frequencyArray2.length; i++){
	    	System.out.println(pic.frequencyArray2[i]+"###"+i);
	    }*/
		/*for(int i = 0; i<pic.aimPic.length; i++){
	    	System.out.println(pic.aimPic[i]+"###"+i);
	    }*/
		/*for(int i = 0; i<pic.removedNoise.length; i++){
	    	System.out.println(pic.removedNoise[i]+"###"+pic.array[i]+"###"+i);
		}*/
		/*for(int i = 0; i<pic.aimPic1.length; i++){
	    	System.out.println(pic.aimPic1[i]+"###"+i);
	    }*/
	    /*for(int i = 0; i<pic.alpha.length; i++){
	    	System.out.println(pic.alpha[i]+"###"+i);
	    }*/
		/*for(int i = 0; i<pic.r.length; i++){
	    	System.out.println(pic.r[i]+"###"+pic.g[i]+"###"+pic.b[i]+"###"+i);
	    }*/
	    
	}   
}
