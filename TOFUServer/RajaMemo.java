import java.io.Serializable;

//ClientMemoの実装クラス、クライアントで保持しておくデータを定義しておく
public class RajaMemo extends ClientMemo implements Serializable {
	private static final long serialVersionUID = 1L;
	//(要実装)クライアント側で保持しておいて欲しいデータがあればここに宣言すること
	
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
