package six.com.common;
/*
 * Swing version.
 */

import java.awt.*;
import java.awt.event.*;
import java.awt.image.MemoryImageSource;
import java.util.*;
import javax.swing.*;


public class DealPic3 extends JFrame{
	Container contentPane = getContentPane();
	private JLabel label1, label2;
	public Pic pic = new Pic();
	private FileDialog fd;
	JTextArea textArea_1 = new JTextArea();
	private JScrollPane textArea1=new JScrollPane(textArea_1);	
	JTextArea textArea_2 = new JTextArea();
	private JScrollPane textArea2=new JScrollPane(textArea_2);
	//private DrawPanel drawPanel1 = new DrawPanel();
	//private DrawPanel drawPanel2 = new DrawPanel();
	private static int [] Source = new int [256]; 
    public DealPic3() {
        contentPane.setLayout(new GridLayout(2,2));
        fd = new FileDialog(this);
        //菜单
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("文件");
        JMenu m2 = new JMenu("去噪");
        JMenuItem mi1 = new JMenuItem("导入");
        //JMenuItem mi2 = new JMenuItem("灰度化");
        //JMenuItem mi3 = new JMenuItem("灰度均衡化");
        JMenuItem mi4_0 = new JMenuItem("原图");
        JMenuItem mi4_1 = new JMenuItem("均值");
        JMenuItem mi4_2 = new JMenuItem("中值");
        JMenuItem mi4_3 = new JMenuItem("自创");
        mb.add(m1);
        mb.add(m2);
        m1.add(mi1);
        //m2.add(mi2);
        //m2.add(mi3);
        m2.add(mi4_0);
        m2.add(mi4_1);
        m2.add(mi4_2);
        m2.add(mi4_3);
        this.setJMenuBar(mb);
        mi1.addActionListener(new OpenListener());
        //mi2.addActionListener(new RGBtoGrayActionListener());
        //mi3.addActionListener(new BalanceActionListener());Noise
        mi4_0.addActionListener(new Noise());
        mi4_1.addActionListener(new RemovedNoise1());
        mi4_2.addActionListener(new RemovedNoise2());
        mi4_3.addActionListener(new RemovedNoise3());
        //图片的载入
		label1 = new JLabel("");
		label2 = new JLabel("");
		JScrollPane pane2 = new JScrollPane(label2);
		JScrollPane pane1 = new JScrollPane(label1);
        //界面显示
        contentPane.add(pane1,"1");
        contentPane.add(this.textArea1,"2");
        this.textArea1.setVisible(true);
        contentPane.add(pane2,"3");
        contentPane.add(this.textArea2,"4");
        this.textArea1.setVisible(true);
        //关闭窗口
        addWindowListener(new WindowAdapter() {
            public void windowClosing(WindowEvent e) {
                     System.exit(0);
            }
        });
    }

    public static void main(String args[]) {
        DealPic3 window = new DealPic3();
        
        window.setTitle("~zk~");
        window.setSize(1024, 768);
        window.setLocationRelativeTo(null);
        window.pack();
        window.setVisible(true);
    }
	private void showImage(int[] srcPixArray,JLabel imageLabel,Pic pic1){
	    Image pic=createImage(new MemoryImageSource(pic1.getWidth(),pic1.getHeight(),srcPixArray,0,pic1.getWidth()));
	    ImageIcon ic=new ImageIcon(pic);
	    imageLabel.setIcon(ic);
	    imageLabel.repaint();
	} 
	private class OpenListener implements ActionListener{

		@SuppressWarnings("deprecation")
		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			fd.setVisible(true);
			fd.setMode(FileDialog.LOAD);
			fd.setLocationRelativeTo(null);
			
			pic = new Pic(fd.getDirectory()+fd.getFile());
			showImage(pic.getArray(), label1, pic);
		}
		
	}
	private class RGBtoGrayActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			showImage(pic.getGrayArray(), label2, pic);
			//drawPanel1 = new DrawPanel(pic.getFrequencyArray1());
			//drawPanel1.repaint();
			//drawPanel1.setVisible(true);
		}
		
	}
	private class BalanceActionListener implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			showImage(pic.getAimPic(), label2, pic);
			//showImage(pic.getAimPic1(), label1, pic);//用来和那个程序处理结果的比较 后来法相我的效果比较好 ioi
			//drawPanel2 = new DrawPanel(pic.getFrequencyArray2());
			//drawPanel2.repaint();
			//drawPanel2.setVisible(true);
		}
		
	}
	private class Noise implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Integer a;String p;			
			int[]ss=pic.getGrayArray1();
			for(int i=0;i<ss.length;i++){
				a=ss[i];
				p=a.toString(a);			
			    textArea_1.append(p);
			    textArea_1.append("  ");
			    if((i+1)%pic.getWidth()==0)
			    textArea_1.append("\n");
			}
		}		
	}
	private class RemovedNoise1 implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Integer a;String p;
			showImage(pic.getRemovedNoise1(), label2, pic);
			int[]ss=pic.getRemovedNoise1_1();
			for(int i=0;i<ss.length;i++){
				a=ss[i];
				p=a.toString();
			    textArea_2.append(p);
			    textArea_2.append("  ");
			    if((i+1)%pic.getWidth()==0)
			    textArea_2.append("\n");
			}
		}		
	}
	private class RemovedNoise2 implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Integer a;String p;
			showImage(pic.getRemovedNoise2(), label2, pic);
			int[]ss=pic.getRemovedNoise2_2();
			for(int i=0;i<ss.length;i++){
				a=ss[i];
				p=a.toString(a);			
			    textArea_2.append(p);
			    textArea_2.append("  ");
			    if((i+1)%pic.getWidth()==0)
			    textArea_2.append("\n");
			}
		}
	}
	private class RemovedNoise3 implements ActionListener{

		@Override
		public void actionPerformed(ActionEvent e) {
			// TODO Auto-generated method stub
			Integer a;String p;
			showImage(pic.getRemovedNoise3(), label2, pic);
			int[]ss=pic.getRemovedNoise3_3();
			for(int i=0;i<ss.length;i++){
				a=ss[i];
				p=a.toString(a);			
			    textArea_2.append(p);
			    textArea_2.append("  ");
			    if((i+1)%pic.getWidth()==0)
			    textArea_2.append("\n");
			}
		}
	}
}


