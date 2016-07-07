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
//    volatile boolean nowWaiting = false;   //���݃t�@�C���̏������݂�ҋ@���Ă��邩
    volatile int nowWaitingCount = 0;      //���݃t�@�C���̏������݂̑ҋ@���Ă���X���b�h��
    volatile Object monitor = new Object();
    
    //���������삵�Ă���Ƃ��āA�J�E���^�𑝉�����B
    //�������݂��s���Ă���ꍇ�ɂ́Await����
    //���̃��\�b�h���g�p�����ꍇ�A�����I����ɂ��Ȃ炸cancelChildMoving����Ԃ���
    public void informChildMoving(){
        System.out.println("informChildMoving�̒���monitor�̃��b�N�𓾂邽�߂Ƀu���b�N");
        synchronized(monitor){
            
            //�������݂̃`�����X�ŁA�������ݑҋ@����Ă���ꍇ�A���j�^�[�����邽�߂ɑҋ@
            while((nowMovingCount==0)&&(nowWaitingCount != 0)){
                try {
                    System.out.println("�t�@�C���̏������݂�D�悷�邽�߂�informChildMoving���ŏ���u���b�N���܂�");
                    monitor.notifyAll();
                    monitor.wait(100);
                } catch (InterruptedException e) {
                    
                }
            }
            
            while(nowWriting == true){  //	�t�@�C���̏������݂��s���Ă���ꍇ�A�������݂��I���܂őҋ@
                try {
                    System.out.println("�t�@�C���������ݒ��̂��߂�informChildMoving���Ńu���b�N");
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
        System.out.println("cancelChildMoving�̒���monitor�̃��b�N�𓾂邽�߂Ƀu���b�N");
        synchronized(monitor){
            nowMovingCount = nowMovingCount-1;
System.out.println("cancel�ɂ���� nowMovingCount=" + nowMovingCount);
            if(nowMovingCount == 0){   //�ЂƂ̃X���b�h���������s���ĂȂ��ꍇ
                monitor.notifyAll();   //�������ݑ҂��őҋ@���Ă���X���b�h�����
            }
            
//            if(nowWriting == true){   //�������݂��s���Ă���Œ��ɏ������I����X���b�h������͂��Ȃ��̂�
//                System.out.println("cancelChiledMoving�œ����G���[�I�I");
//                throw new RuntimeException();
//            }
        }
    }
    
    //�������s���Ă悢���`�F�b�N����
    //�����I�����finishDoing()��K���g�p���鎖
    public void checkCanDo(){
        System.out.println("checkCanDo�̒���monitor�̃��b�N�𓾂邽�߂Ƀu���b�N");
        synchronized(monitor){
            while((nowMovingCount != 0)||(nowWriting == true) ){
				try {
System.out.println("nowMovingCount=" + nowMovingCount + "�Ȃ̂ŏ������ݑҋ@���܂�");				    
				    System.out.println("checkCanDo�ɂ����nowWaitingCount=" + nowWaitingCount);
					nowWaitingCount = nowWaitingCount + 1;
					monitor.wait();
					System.out.println("checkCanDo���̃u���b�N�ЂƂ܂��˔j");
				} catch (InterruptedException e) {
					
				}
				System.out.println("checkCanDo�ɂ����nowWaitingCount=" + nowWaitingCount);
				nowWaitingCount = nowWaitingCount-1 ; //�ҋ@��Ԃ𔲂����ƕ\��
            }
            nowWriting = true;  //�������݂��s���Ă���ƕ\��
        }
    }
    
    //�r���I�ɂ����Ȃ�Ȃ���΂Ȃ�Ȃ��������I����������\������
    //checkCanDo()��p������K����������I�����Ɏg�p���邱��
    public void finishDoing(){
        System.out.println("finishDoing����monitor�̃��b�N�𓾂邽�߂Ƀu���b�N���Ă��܂�");
        synchronized(monitor){
            nowWriting = false;   //�������݂͏I�������ƕ\��
//            nowMovingCount = 0;    //�O�̂��߂O�ɂ��Ă���
            monitor.notifyAll();   //�������ݒ��ɑҋ@���Ă��X���b�h�B���������
        }
    }
}
