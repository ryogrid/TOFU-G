/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

//�t�@�C���ۑ��̔r���������s�����߂̃��j�^�[
//�ۑ�����N���X�̓����̃f�[�^���ύX����Ă���ꍇ�Ƀ��b�N����
public class FileMonitor {
    volatile int nowMovingCount = 0;        //���݁A�ύX�������Ă���X���b�h��
    volatile boolean nowWriting = false;   //���݃t�@�C���̏������݂��s���Ă��邩
    volatile boolean nowWaiting = false;   //���݃t�@�C���̏������݂�ҋ@���Ă��邩
    volatile Object monitor = new Object();
    
    //���������삵�Ă���Ƃ��āA�J�E���^�𑝉�����B
    //�������݂��s���Ă���ꍇ�ɂ́Await����
    //���̃��\�b�h���g�p�����ꍇ�A�����I����ɂ��Ȃ炸cancelChildMoving����Ԃ���
    public void informChildMoving(){
        synchronized(monitor){
            
            //�������݂̃`�����X�ŁA�������ݑҋ@����Ă���ꍇ�A���j�^�[�����邽�߂ɑҋ@
            while((nowMovingCount==0)&&(nowWaiting == true)){
                try {
                    monitor.notifyAll();
                    monitor.wait();
                } catch (InterruptedException e) {
                    
                }
            }
            
            while(nowWriting == true){  //	�t�@�C���̏������݂��s���Ă���ꍇ�A�������݂��I���܂őҋ@
                try {
                    monitor.wait();
                } catch (InterruptedException e) {

                }
            }
            nowMovingCount = nowMovingCount+1;
System.out.println("inform�ɂ���� nowMovingCount =" + nowMovingCount);            
        }
    }
    
    
    //�ǉ����������̃J�E���^����菜��
    //informChildMoving���g�p�����ꍇ�A�����I����ɂ��Ȃ炸���̃��\�b�h���g�p���邱��
    public void cancelChildMoving(){
        synchronized(monitor){
            nowMovingCount = nowMovingCount-1;
System.out.println("cancel�ɂ���� nowMovingCount=" + nowMovingCount);
            if(nowMovingCount == 0){   //�ЂƂ̃X���b�h���������s���ĂȂ��ꍇ
                monitor.notifyAll();   //�������ݑ҂��őҋ@���Ă���X���b�h�����
            }
            
            if(nowWriting == true){   //�������݂��s���Ă���Œ��ɏ������I����X���b�h������͂��Ȃ��̂�
                System.out.println("cancelChiledMoving�œ����G���[�I�I");
                throw new RuntimeException();
            }
        }
    }
    
    //�������s���Ă悢���`�F�b�N����
    //�����I�����finishDoing()��K���g�p���鎖
    public void checkCanDo(){
        synchronized(monitor){
            while(nowMovingCount != 0){
				try {
System.out.println("nowMovingCount=" + nowMovingCount + "�Ȃ̂ŏ������ݑҋ@���܂�");				    
				    nowWaiting = true;
					monitor.wait();
				} catch (InterruptedException e) {
					
				}
            }
            nowWriting = true;  //�������݂��s���Ă���ƕ\��
            nowWaiting = false; //�ҋ@��Ԃ𔲂����ƕ\��
        }
        
    }
    
    //�r���I�ɂ����Ȃ�Ȃ���΂Ȃ�Ȃ��������I����������\������
    //checkCanDo()��p������K����������I�����Ɏg�p���邱��
    public void finishDoing(){
        synchronized(monitor){
            nowWriting = false;   //�������݂͏I�������ƕ\��
            nowWaiting = false;   //�O�̂��ߑҋ@���Ă���Ƃ����̂�false��
            monitor.notifyAll();   //�������ݒ��ɑҋ@���Ă��X���b�h�B���������
        }
    }
}
