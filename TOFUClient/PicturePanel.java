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

//�w�i�ɉ摜��\�����邽�߂�JPanel(setImg�𗘗p����_����������_�j
public class PicturePanel extends JPanel {
	private Image img = null;

	//�w�i�摜���Z�b�g����
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
