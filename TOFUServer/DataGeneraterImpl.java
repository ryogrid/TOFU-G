import java.io.*;

import raja.io.ObjectReader;
import raja.renderer.*;

//クライアントに配布するためのjobを生成するクラス
public class DataGeneraterImpl extends DataGenerater implements Serializable {
	private int jobCount = 1;     //次に配布するjobの番号(次のが何個目にあたるか)
    
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
    private final int HORIZONAL_DIVIDE = 5;      //↓各分割幅   EDGE～は端っこ用の半端な長さ  
    private final int NORMAL_VERTICAL_SIZE;   
    private final int EDGE_VERTICAL_SIZE;
    private final int NORMAL_HORIZONAL_SIZE;
    private final int EDGE_HORIZONAL_SIZE;
    private final int ONE_SCENE_JOB_COUNT;    //一場面が何分割(何job)になるか
    
    private int nowVertical = 1; //次に割り振る縦の区域番号(１からスタート)
    private int nowHorizonal = 1;   //次に割り振る横の区域番号(１からスタート)
    private int nowCameraNum = first;   //次に割り振るカメラ(シーン)番号
	
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
        
        //分割幅などを計算
        int vertical_mod =  wholeScreenHeight % VERTICAL_DIVIDE;
        NORMAL_VERTICAL_SIZE = (wholeScreenHeight-vertical_mod)/VERTICAL_DIVIDE;
        EDGE_VERTICAL_SIZE  = NORMAL_VERTICAL_SIZE + vertical_mod; 
        int horizonal_mod =  wholeScreenWidth % HORIZONAL_DIVIDE;
        NORMAL_HORIZONAL_SIZE = (wholeScreenWidth-horizonal_mod)/HORIZONAL_DIVIDE;
        EDGE_HORIZONAL_SIZE  = NORMAL_HORIZONAL_SIZE + horizonal_mod;
        ONE_SCENE_JOB_COUNT = VERTICAL_DIVIDE*HORIZONAL_DIVIDE;
    }
    
	//新しいCulculateData（つまりjob）を生成して返す
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

        newJob.setJobName((nowCameraNum-1) + "番目のシーンの" + jobCount + "番部分画像:"+ nowHorizonal + "列の" + nowVertical + "行目");
        
        if(nowHorizonal == HORIZONAL_DIVIDE){  //端だったら
            toAssigne.horizonalWidth = EDGE_HORIZONAL_SIZE;
            nowHorizonal = 0;
            nowVertical++;
        }else{
            toAssigne.horizonalWidth = NORMAL_HORIZONAL_SIZE;
            nowHorizonal++;            
        }
        
        if(nowVertical == VERTICAL_DIVIDE){    //端だったら
            toAssigne.verticalWidth = EDGE_VERTICAL_SIZE;
        }else{
            toAssigne.verticalWidth = NORMAL_VERTICAL_SIZE;
        }
        
        newJob.assinedPart = toAssigne;
        newJob.jobCount = jobCount;
        
        if((jobCount % ONE_SCENE_JOB_COUNT)==0){   //次の場目への境目だった場合
            nowVertical = 1;
            nowHorizonal = 1;
            nowCameraNum ++;
        }
        
        newJob.allPartCount = ONE_SCENE_JOB_COUNT;
        
        jobCount++;
        return newJob;
	}
}
