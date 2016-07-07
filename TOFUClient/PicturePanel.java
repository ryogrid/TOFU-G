/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.awt.*;
import java.awt.Image;
import javax.swing.JPanel;

//背景に画像を表示するためのJPanel(setImgを利用する点だけが相違点）
public class PicturePanel extends JPanel {
	private Image img = null;

	//背景画像をセットする
	public void setImg(Image img){
		this.img = img;
	}
	
	public void paintComponent(Graphics g){
		super.paintComponent(g);
		if(img != null){
			g.drawImage(img,0,0,this.getWidth(),this.getHeight(),this);
		}
	}
}
