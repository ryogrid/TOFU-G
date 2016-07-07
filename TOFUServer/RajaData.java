import java.awt.Image;
import java.util.Calendar;

import raja.renderer.*;

//CulculateDataの実装クラス。サーバとクライアント間を行き交う、計算用のデータを定義する
public class RajaData extends CulculateData {
	private static final long serialVersionUID = 1L;
    
    public int jobCount;
    public int allPartCount;     //一枚の画像の全区域を足した数
    public boolean exact;
    public float scale;
    public int antialias;
    private final int first =1;
    public int last;
    public int depth; 
    public Resolution resolution;
    public Camera assinedCamera;
    public Scene scene;

    public SubimageResult alternativeImageResult;
    
    
    public AssignedPartDescriptor assinedPart;
    
//    public Image resultImage; 
    
	public double getNecessaryTime() {
		return super.necessaryTime;
	}
	
	public void setNecessaryTime(double necessaryTime) {
		super.necessaryTime = necessaryTime;
	}
	
	public Calendar getCompleteDate() {
		return super.completeDate;
	}

	public void setCompleteDate(Calendar completeDate) {
		super.completeDate = completeDate;
	}
	
	public Calendar getDistributeDate() {
		return super.distributeDate;
	}
	
	public void setDistributeDate(Calendar distributeDate) {
		super.distributeDate = distributeDate;
	}
	
	public byte[] getSigniture() {
		return super.signiture;
	}
	
	public void setSigniture(byte[] signiture) {
		super.signiture = signiture;
	}

	public String getJobName() {
		return super.jobName;
	}

	public void setJobName(String jobName) {
		super.jobName = jobName;
	}
	
	public String getResultDescription() {
		return super.resultDescription;
	}
	
	public void setResultDescription(String resultDescription) {
		super.resultDescription = resultDescription;
	}
	
	public int getVersion() {
		return super.version;
	}
	
	public boolean isCulculateComplete() {
		return super.culculateComplete;
	}
	
	public void setCulculateComplete(boolean culculateComplete) {
		super.culculateComplete = culculateComplete;
	}
	
	public void setVersion(int version) {
		super.version = version;
	}
	
	public boolean isResultEqual(CulculateData target) {
        return true;
	}
}
