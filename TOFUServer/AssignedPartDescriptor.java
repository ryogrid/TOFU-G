import java.io.Serializable;

public class AssignedPartDescriptor implements Serializable {
    public long verticalStart;   //縦での担当分の開始地点
    public long verticalWidth;  //縦の担当幅
    public long horizonalStart;   //横での担当分の開始地点
    public long horizonalWidth;   //横での担当幅
}
