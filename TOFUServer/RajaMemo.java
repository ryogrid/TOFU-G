import java.io.Serializable;

//ClientMemo�̎����N���X�A�N���C�A���g�ŕێ����Ă����f�[�^���`���Ă���
public class RajaMemo extends ClientMemo implements Serializable {
	private static final long serialVersionUID = 1L;
	//(�v����)�N���C�A���g���ŕێ����Ă����ė~�����f�[�^������΂����ɐ錾���邱��
	
	public int getVersion() {
		return this.version;
	}
	
	public void setVersion(int version) {
		this.version = version;
	}
	
	public void setInterruption(boolean flag) {
		this.shouldInterrupt = flag;
	}
	
	public boolean getInterruption(){
		return this.shouldInterrupt;
	}
}
