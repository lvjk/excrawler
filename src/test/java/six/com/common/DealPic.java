package six.com.common;
/*
 * Swing version.
 */

import java.awt.*;
import java.awt.event.*;

import javax.swing.*;


public class DealPic extends JFrame implements ActionListener{
	Container contentPane = getContentPane();
	private JLabel label1,label2;
	public Pic pic = new Pic("e:\\int.jpg");
	private FileDialog fd;
	private Icon icon =  new ImageIcon(pic.getPath());
	private Icon icon1 =  new ImageIcon(pic.getPath());
	private DrawPanel drawPanel1 = null;
	private DrawPanel drawPanel2 = null;
    public DealPic() {
        contentPane.setLayout(new GridLayout(2,2));
        fd = new FileDialog(this);
        //菜单
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("文件");
        JMenuItem mi1 = new JMenuItem("导入");
        mb.add(m1);
        m1.add(mi1);
        this.setJMenuBar(mb);
        mi1.addActionListener(this);
       
        //图片的载入
		label1 = new JLabel(icon);
		label2 = new JLabel("");
        //界面显示
        contentPane.add(label1,"1");
        contentPane.add(new DrawPanel(pic.getFrequencyArray1()),"2");
        contentPane.add(label2,"3");
        contentPane.add(new DrawPanel(pic.getFrequencyArray2()),"4");
        //关闭窗口
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                     System.exit(0);
            }
        });
    }
    class DrawPanel extends JPanel{
    	int [] Source = new int [256]; 
    	public  DrawPanel(int[] a){
            setBackground(Color.white);
            System.arraycopy(a, 0, this.Source, 0, a.length);
        }
    	public void paintComponent(Graphics g){
    		super.paintComponent(g);
    		int[]grayArray = new int[256];  //原始图像灰度统计数组
    	    System.arraycopy(pic.change(this.Source), 0, grayArray, 0, pic.getFrequencyArray1().length);
    	    //Toolkit kit= Toolkit.getDefaultToolkit();
    	    //Dimension screenSize=kit.getScreenSize();
    	    //int x=(screenSize.width)/2;
            //int y=(screenSize.height)/2;
    	    int x=100;
            int y=350;
            g.drawLine(x, y, x, 20);
            g.drawLine(x, y, x+256, y);
            for (int i = 0; i < 256; i++) {
				g.drawLine(x+i, y, x+i, y-grayArray[i]);
            }
            /*for (int i = 0; i < 255; i++) {
				g.drawLine(x+i, y-grayArray[i], x+i+1, y-grayArray[i+1]);
            }*/
        }
    }
    public static void main(String args[]) {
        DealPic window = new DealPic();
        
        window.setTitle("灰度直方图均衡化");
        window.pack();
        window.setVisible(true);
    }
	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO Auto-generated method stub
		fd.show();
		pic.setPath(fd.getDirectory());
		pic.setFile();
		pic.setHeight(); //取得图片高
		pic.setWidth();  //取得图片宽
		pic.setArray();  //取得图片像素数组（原）
		pic.setGrayArray();        //将彩色数组灰度化（原）
		pic.setFrequencyArray1();//统计灰度分布（原）
		pic.setPoiseGrayArray();         //目标图像灰度级（统计数组）9*hp
		pic.setFrequencyArray2();//目标图像的灰度分布
        pic.setAimPic();                 //创建灰度增强后的图片数组*/
        
		
	}
}

