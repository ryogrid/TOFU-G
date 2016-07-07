import java.awt.image.BufferedImage;
import java.io.Serializable;

import raja.ui.*;


//�N���C�A���g����v�Z���ʂ��ԋp���ꂽ���ɂǂ̂悤�ȏ������s�������`����N���X
public class ProcessPlantImpl extends ProcessPlant implements Serializable{
    private transient ImageFrame displayFrame;
    private transient BufferedImage imageContaindFrame;
    
	public ProcessPlantImpl(ResultDatabase giveDB,ServerMain parent){
		super(giveDB,parent);
	}
	
	//�^����ꂽ�v�Z���ʂɂ���ĉ��炩�̏������s��
	public synchronized void doSomeThing(CulculateData food) {
        RajaData rfood = (RajaData) food;
        
        BufferedImage resultImage = new BufferedImage(rfood.resolution.width,rfood.resolution.height,BufferedImage.TYPE_3BYTE_BGR);
        SubimageResult r = rfood.alternativeImageResult;
        
        if(((rfood.jobCount%rfood.allPartCount)==1)&&(displayFrame!=null)){   //�V�����摜�̈�ԍŏ��̕����������ꍇ
            displayFrame.dispose();
            displayFrame =null;
        }
        
        if(displayFrame==null){
            //BufferdImage�֕ϊ�
            while (r.hasNextPixel()) {
                Pixel p = r.getNextPixel();
                resultImage.setRGB(p.getX(), p.getY(), p.getRGB().getColor().getRGB());
            }
            
            displayFrame = new ImageFrame(rfood.assinedCamera + "����",(BufferedImage)resultImage,100);
            displayFrame.setLocation(300, 300);
            displayFrame.pack();
            displayFrame.setUpdating(true);
            displayFrame.setVisible(true);
            imageContaindFrame = (BufferedImage)resultImage;
            displayFrame.show();
            return;
        }
        
        //�摜���}�[�W����
        while (r.hasNextPixel()) {
            Pixel p = r.getNextPixel();
            //�W���u�S�����̃s�N�Z���������ꍇ
            if(((p.getX() >= rfood.assinedPart.horizonalStart)&&(p.getX() <= (rfood.assinedPart.horizonalStart + rfood.assinedPart.horizonalWidth -1)))&&
                    ((p.getY() >= (rfood.assinedPart.verticalStart))&&(p.getY() <= (rfood.assinedPart.verticalStart + rfood.assinedPart.verticalWidth -1)))){
                imageContaindFrame.setRGB(p.getX(), p.getY(), p.getRGB().getColor().getRGB());
            }
        }
	}
}
