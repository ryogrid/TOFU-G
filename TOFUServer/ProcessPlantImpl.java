import java.awt.image.BufferedImage;
import java.io.Serializable;

import raja.ui.*;


//クライアントから計算結果が返却された時にどのような処理を行うかを定義するクラス
public class ProcessPlantImpl extends ProcessPlant implements Serializable{
    private transient ImageFrame displayFrame;
    private transient BufferedImage imageContaindFrame;
    
	public ProcessPlantImpl(ResultDatabase giveDB,ServerMain parent){
		super(giveDB,parent);
	}
	
	//与えられた計算結果によって何らかの処理を行う
	public synchronized void doSomeThing(CulculateData food) {
        RajaData rfood = (RajaData) food;
        
        BufferedImage resultImage = new BufferedImage(rfood.resolution.width,rfood.resolution.height,BufferedImage.TYPE_3BYTE_BGR);
        SubimageResult r = rfood.alternativeImageResult;
        
        if(((rfood.jobCount%rfood.allPartCount)==1)&&(displayFrame!=null)){   //新しい画像の一番最初の部分だった場合
            displayFrame.dispose();
            displayFrame =null;
        }
        
        if(displayFrame==null){
            //BufferdImageへ変換
            while (r.hasNextPixel()) {
                Pixel p = r.getNextPixel();
                resultImage.setRGB(p.getX(), p.getY(), p.getRGB().getColor().getRGB());
            }
            
            displayFrame = new ImageFrame(rfood.assinedCamera + "枚目",(BufferedImage)resultImage,100);
            displayFrame.setLocation(300, 300);
            displayFrame.pack();
            displayFrame.setUpdating(true);
            displayFrame.setVisible(true);
            imageContaindFrame = (BufferedImage)resultImage;
            displayFrame.show();
            return;
        }
        
        //画像をマージする
        while (r.hasNextPixel()) {
            Pixel p = r.getNextPixel();
            //ジョブ担当分のピクセルだった場合
            if(((p.getX() >= rfood.assinedPart.horizonalStart)&&(p.getX() <= (rfood.assinedPart.horizonalStart + rfood.assinedPart.horizonalWidth -1)))&&
                    ((p.getY() >= (rfood.assinedPart.verticalStart))&&(p.getY() <= (rfood.assinedPart.verticalStart + rfood.assinedPart.verticalWidth -1)))){
                imageContaindFrame.setRGB(p.getX(), p.getY(), p.getRGB().getColor().getRGB());
            }
        }
	}
}
