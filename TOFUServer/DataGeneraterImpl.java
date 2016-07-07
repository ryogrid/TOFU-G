import java.io.*;

import raja.io.ObjectReader;
import raja.renderer.*;

//�N���C�A���g�ɔz�z���邽�߂�job�𐶐�����N���X
public class DataGeneraterImpl extends DataGenerater implements Serializable {
	private int jobCount = 1;     //���ɔz�z����job�̔ԍ�(���̂����ڂɂ����邩)
    
    private final boolean exact = true;
    private final float scale = 2;
    private final int antialias = 5;
    private final int first = 1;
    private int last = 0;
    private final int depth = 5; 
    private Resolution resolution;
    private Camera[] travelling;
    private Scene scene;
    
    private final int VERTICAL_DIVIDE = 5;
    private final int HORIZONAL_DIVIDE = 5;      //���e������   EDGE�`�͒[�����p�̔��[�Ȓ���  
    private final int NORMAL_VERTICAL_SIZE;   
    private final int EDGE_VERTICAL_SIZE;
    private final int NORMAL_HORIZONAL_SIZE;
    private final int EDGE_HORIZONAL_SIZE;
    private final int ONE_SCENE_JOB_COUNT;    //���ʂ�������(��job)�ɂȂ邩
    
    private int nowVertical = 1; //���Ɋ���U��c�̋��ԍ�(�P����X�^�[�g)
    private int nowHorizonal = 1;   //���Ɋ���U�鉡�̋��ԍ�(�P����X�^�[�g)
    private int nowCameraNum = first;   //���Ɋ���U��J����(�V�[��)�ԍ�
	
    public DataGeneraterImpl(){
        try {
            Reader in = new InputStreamReader(new FileInputStream("./scene.raj"));
            ObjectReader reader = new ObjectReader(in);
            scene = (Scene) reader.readObject();
            reader.close();

            InputStream inputStream = new BufferedInputStream(new FileInputStream("./cameraAnglesFile"));
            ObjectInput objectInput = new ObjectInputStream(inputStream);
            travelling = (Camera[]) objectInput.readObject();
            objectInput.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        
        int wholeScreenWidth = (int) Math.round(384 * scale);
        int wholeScreenHeight = (int) Math.round(288 * scale);
        resolution = new Resolution(wholeScreenWidth,wholeScreenHeight );
        
        //�������Ȃǂ��v�Z
        int vertical_mod =  wholeScreenHeight % VERTICAL_DIVIDE;
        NORMAL_VERTICAL_SIZE = (wholeScreenHeight-vertical_mod)/VERTICAL_DIVIDE;
        EDGE_VERTICAL_SIZE  = NORMAL_VERTICAL_SIZE + vertical_mod; 
        int horizonal_mod =  wholeScreenWidth % HORIZONAL_DIVIDE;
        NORMAL_HORIZONAL_SIZE = (wholeScreenWidth-horizonal_mod)/HORIZONAL_DIVIDE;
        EDGE_HORIZONAL_SIZE  = NORMAL_HORIZONAL_SIZE + horizonal_mod;
        ONE_SCENE_JOB_COUNT = VERTICAL_DIVIDE*HORIZONAL_DIVIDE;
    }
    
	//�V����CulculateData�i�܂�job�j�𐶐����ĕԂ�
	public CulculateData generateData() {
        RajaData newJob = new RajaData();
        newJob.exact = exact;
        newJob.antialias = antialias;
        newJob.depth = depth;
        newJob.last = last;
        newJob.resolution = resolution;
        newJob.scale = scale;
        newJob.scene = scene;
        newJob.assinedCamera = travelling[nowCameraNum];
        
        AssignedPartDescriptor toAssigne =new AssignedPartDescriptor();
        toAssigne.verticalStart = (nowVertical - 1)*NORMAL_VERTICAL_SIZE;
        toAssigne.horizonalStart = (nowHorizonal-1)*NORMAL_HORIZONAL_SIZE;

        newJob.setJobName((nowCameraNum-1) + "�Ԗڂ̃V�[����" + jobCount + "�ԕ����摜:"+ nowHorizonal + "���" + nowVertical + "�s��");
        
        if(nowHorizonal == HORIZONAL_DIVIDE){  //�[��������
            toAssigne.horizonalWidth = EDGE_HORIZONAL_SIZE;
            nowHorizonal = 0;
            nowVertical++;
        }else{
            toAssigne.horizonalWidth = NORMAL_HORIZONAL_SIZE;
            nowHorizonal++;            
        }
        
        if(nowVertical == VERTICAL_DIVIDE){    //�[��������
            toAssigne.verticalWidth = EDGE_VERTICAL_SIZE;
        }else{
            toAssigne.verticalWidth = NORMAL_VERTICAL_SIZE;
        }
        
        newJob.assinedPart = toAssigne;
        newJob.jobCount = jobCount;
        
        if((jobCount % ONE_SCENE_JOB_COUNT)==0){   //���̏�ڂւ̋��ڂ������ꍇ
            nowVertical = 1;
            nowHorizonal = 1;
            nowCameraNum ++;
        }
        
        newJob.allPartCount = ONE_SCENE_JOB_COUNT;
        
        jobCount++;
        return newJob;
	}
}
