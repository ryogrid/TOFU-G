import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.StyleContext.SmallAttributeSet;
import raja.renderer.*;
import raja.renderer.Renderer;

//�v�Z�����ۂɍs���N���X�B�v�Z�̎�ʂɏ]���ă��[�U�[���������Ă���
public class RajaWorker extends Culculater {
	private static final long serialVersionUID = 1L;
//�K�v�ȕϐ�������΃t�B�[���h���������錾���Ă��悢
	
//	������I���̏ꍇ�͂P�A�r�����f���܂ߐ���I�����Ȃ������ꍇ�͂O��Ԃ�
	public int culculate(CulculateData data, ClientMemo memo) {
        RajaData job = (RajaData)data;
        RajaMemo rmemo = (RajaMemo)memo;
        
        RayTracer rayTracer;
        if (job.exact) {
            rayTracer = new AdvancedRayTracer(job.scene.getWorld(), job.depth, 0);
        }
        else {
            rayTracer = new AdvancedRayTracer(job.scene.getWorld(), job.depth);
        }
        
        DistributeSampler sampler = new DistributeSampler(job.antialias);
        sampler.setAssignedPart(job.assinedPart);
        
        Renderer renderer = new BasicRenderer(job.assinedCamera, job.resolution, rayTracer, sampler, BufferedImage.TYPE_3BYTE_BGR);
        renderer.run();
        
        job.alternativeImageResult = sampler.getResult();
        return 1;
    }
	
	public String getString() {
        return "�摜�̌v�Z|�ǁL�D�M|��<�I��";
	}
	
	public void setVersion(int version) {
        	this.version =version;
    	}
    
    	public int getVersion() {
            return 0;
    	}
}
