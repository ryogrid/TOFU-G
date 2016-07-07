import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;

import javax.swing.*;
import javax.swing.border.*;
import javax.swing.text.StyleContext.SmallAttributeSet;
import raja.renderer.*;
import raja.renderer.Renderer;

//計算を実際に行うクラス。計算の種別に従ってユーザーが実装しておく
public class RajaWorker extends Culculater {
	private static final long serialVersionUID = 1L;
//必要な変数があればフィールドをいくつか宣言してもよい
	
//	※正常終了の場合は１、途中中断を含め正常終了しなかった場合は０を返す
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
        return "画像の計算|ι´Д｀|っ<終了";
	}
	
	public void setVersion(int version) {
        	this.version =version;
    	}
    
    	public int getVersion() {
            return 0;
    	}
}
