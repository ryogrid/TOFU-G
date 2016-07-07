/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.LinkedHashMap;
import java.util.LinkedList;

//�W���u�̍Ĕz�z�ȂǁA�W���u�S�̂̊Ǘ����s���N���X
public class JobManager implements Serializable{
	private final int QUEUE_DANGER_SIZE = 3;       //�W���u�̕⋋���s��Ȃ��Ă͂Ȃ�Ȃ��L���[�̃W���u��
	private final int QUEUE_FILL_SIZE = 10;        //�W���u��⋋���鎞�ɂ����܂Ŗ��߂邩

    /**
     * 
     * @uml.property name="redundancyCount" multiplicity="(0 1)"
     */
    private int redundancyCount = 2; //�璷���̂��߂ɁA�����W���u�������z�z���邩�B�f�t�H���g�͂Q

    /**
     * 
     * @uml.property name="ancestor"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private transient ServerMain ancestor = null;

    /**
     * 
     * @uml.property name="generater"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private volatile DataGeneraterImpl generater; //�W���u�̐����𐿂������N���X

    /**
     * 
     * @uml.property name="resultDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    private volatile ResultDatabase resultDB = null; //���������W���u��ێ�����N���X

/**
 * 
 * @uml.property name="distributeJobQueue"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	private volatile LinkedList distributeJobQueue = new LinkedList();     //�z�z�҂��̃W���u�̃L���[
public volatile PersistentLinkedList distributeJobQueue = new PersistentLinkedList(
    100,
    "./tmp/distributeJobQueue.tmp"); //�z�z�҂��̃W���u�̃L���[

/**
 * 
 * @uml.property name="waitingBackJobs"
 * @uml.associationEnd multiplicity="(1 1)"
 */
//	public volatile LinkedHashMap waitingBackJobs = new LinkedHashMap();   //�z�z����āA�Ԃ��Ă���̂�҂��Ă���job��ServerLogWraper�ŕێ�
public volatile PersistentLinkedHashMap waitingBackJobs = new PersistentLinkedHashMap(
    100,
    "./tmp/waitingBackJobs.tmp"); //�z�z����āA�Ԃ��Ă���̂�҂��Ă���job��ServerLogWraper�ŕێ�

    /**
     * 
     * @uml.property name="pastJobDB"
     * @uml.associationEnd multiplicity="(1 1)"
     */
    public volatile ServerPastJobDatabase pastJobDB = new ServerPastJobDatabase(); //�ߋ����O�̂��߂̃f�[�^�x�[�X�B���[�U�ƐڐG����̂�resultDB�̕�

	
	public JobManager(ServerMain ancestor,DataGeneraterImpl generater,ResultDatabase database){
		this.ancestor = ancestor;
		this.generater = generater;
		this.resultDB = database;
		fillDistributeJobQ(QUEUE_FILL_SIZE);        //�W���u���K�萔���߂ď�����
	}
	
	//DataGeneraterImpl��generateData���\�b�h�̃��b�p�[���\�b�h�B
	//�����N���C�A���g�ɓ����i�����͔z�z���Ȃ�
	public synchronized CulculateData generateJob(byte[] clientSigniture){
		CulculateData newJob = removeOneJobFromQueue();
		
		//�܂��ԋp�ҋ@�L���[�ɂ܂�Ă��Ȃ����
		if(isExistWaitingjob(newJob.getSigniture())!=true){
			newJob.setDistributeDate(Calendar.getInstance());    //�z�z�����������L�^���Ă���
			addNewWaitingJob(newJob);    //�ԋp�ҋ@�W���u�Ƃ��Ēǉ�
//			�z�z�����N���C�A���g�̃V�O�j�`�����L�^���Ă���
			getWaitingJob(newJob.getSigniture()).addDistributer(clientSigniture);
			return newJob;
		}else{   //�܂�Ă����ꍇ
			
			//�����W���u�������N���C�A���g�ɈȑO�z�z����Ă����ꍇ
			if(getWaitingJob(newJob.getSigniture()).checkFormerDistributer(clientSigniture)==true){
				addReJobToQueue(newJob); //���o���Ă��܂������̂�߂�
				CulculateData tmp = serchFreshJob(clientSigniture); //���z�z�̂��̂����o��
				tmp.setDistributeDate(Calendar.getInstance());
				addNewWaitingJob(tmp);     //�ԋp�ҋ@�W���u�Ƃ��Ēǉ�
//����Ӗ��s���Ȃ��Ƃ��Ă�\��				getWaitingJob(newJob.getSigniture()).addDistributer(clientSigniture);
				addDistributerToWaitingQ(tmp.getSigniture(),clientSigniture);
				return tmp;    
			}else{   //�ȑO�z�z����Ă��Ȃ���΁A���̂܂ܔz�z����
//				�z�z�����N���C�A���g�̃V�O�j�`�����L�^���Ă���
				getWaitingJob(newJob.getSigniture()).addDistributer(newJob.getSigniture());
				return newJob;
			}
		}
	}
	
	//�z�z�������ɃW���u��ԋp�ҋ@�L���[�ɐV�K�ǉ����邽�߂̃��\�b�h
	//�㏑���ɂȂ�ꍇ�͉������Ȃ��̂Œ��ӁB�X�V����ꍇ�͕ʂ̃��\�b�h�𗘗p���邱��
	//���W���u�ɃV�O�j�`�����Z�b�g���Ă���g�p���邱��
	private void addNewWaitingJob(CulculateData job){
		if(waitingBackJobs.containsKey(job.getSigniture())==false){    //�܂����̃W���u���܂܂�Ă��Ȃ������ꍇ
			synchronized(waitingBackJobs){
			    ServerMain.fMonitor.informChildMoving();
			    
				try {
                    waitingBackJobs.put(byteArrayToString(job.getSigniture()),new ServerLogWraper(job,redundancyCount));
                } catch (IOException e) {
                    e.printStackTrace();
                    throw new RuntimeException();
                }finally{
                    ServerMain.fMonitor.cancelChildMoving();    
                }
			}
		}else{    //���łɂ��̂i�������܂܂�Ă����ꍇ
			//�������Ȃ�
		}
	}
	
	//�w�肵���W���u���҂���ԂƂ��ăL���[�ɂ܂�Ă��邩
	private boolean isExistWaitingjob(byte[] signiture){
		boolean tmp;
	    synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = waitingBackJobs.containsKey(byteArrayToString(signiture));    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
	    }
	    return tmp;
	}
	
	//�z�z�������ɃW���u��ԋp�ҋ@�L���[�ɒǉ����邽�߂̃��\�b�h
	//���W���u�ɃV�O�j�`�����Z�b�g���Ă���g�p���邱��
	private void addWaitingJob(ServerLogWraper job){
		synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    
			try {
                waitingBackJobs.put(byteArrayToString(job.getWrapped().getSigniture()),job);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
		}
	}
	
	//�z�z�҂��L���[����job���ЂƂ��o���A�L���[�̒��̎c��W���u�������Ȃ��Ȃ����ꍇ��[����
	private CulculateData removeOneJobFromQueue(){
		
	    CulculateData job;
	    synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
			try{
//				�W���u�����Ȃ��Ȃ��Ă���ꍇ�⋋����
				if(distributeJobQueue.size() < QUEUE_DANGER_SIZE){
					fillDistributeJobQ(QUEUE_FILL_SIZE);
				}
				
				job = (CulculateData) distributeJobQueue.getFirst();
				distributeJobQueue.remove(job);      //getFirst�͓��邾���Ŏ�菜���Ȃ��̂Ŏ�菜��
			}finally{
			    ServerMain.fMonitor.cancelChildMoving();    
			}
		}
		return job;
	}
	
	private ServerLogWraper removeWaitingJob(byte[] signiture){
		ServerLogWraper tmp;
	    synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp =  (ServerLogWraper) waitingBackJobs.remove(byteArrayToString(signiture));    
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
	    }
	    return tmp;
	}
	
//	�o�C�g�z���String�̕�����֕ϊ�
	private String byteArrayToString(byte[] array) {
		StringBuffer tmp = new StringBuffer();
		for (int i = 0; i < array.length; i++) {
			tmp.append(array[i]);
		}
		String result = tmp.toString();
		return result;
	}
	
	//�w�肳�ꂽ���ȏ�܂Łi���m�ɂ��̒l�ɂȂ邱�Ƃ͕ۏ؂��Ȃ��j�z�z�҂��W���u�̃L���[�𖄂߂�
	private void fillDistributeJobQ(int count){
		synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    try{
				//�w�肵�����ɂȂ�܂Ŗ����ɃW���u��������i�V�O�j�`���Ȃǂ̊e��v���p�e�B�͎��o�����ɕt���j
				while(distributeJobQueue.size() < count){
					CulculateData tmp = generater.generateData();
					//�����ƃV�X�e���^�C���ŃV�O�j�`���𐶐����ݒ�
					tmp.setSigniture(ServerMain.sDataToFile.generateHash(String.valueOf((long)(Math.random()*100000000000000000L))));    //job�̃V�O�j�`�����L�^���Ă���
					
					//�����W���u���w�肳�ꂽ�������ǉ�
					for(int i=0;i<redundancyCount;i++){
						try {
	                        distributeJobQueue.addLast(tmp);
	                    } catch (IOException e) {
	                        e.printStackTrace();
	                    }
					}
				}
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	}
	
	//�Ĕz�z�̃W���u��z�z�҂��L���[�̎n�߂ɒǉ�����
	public void addReJobToQueue(CulculateData reJob){
		synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    
			try {
                distributeJobQueue.addFirst(reJob);
            } catch (IOException e) {
                e.printStackTrace();
                throw new RuntimeException();
            }finally{
                ServerMain.fMonitor.cancelChildMoving();    
            }
		}
	}
	
	//�t�@�C���ɕۑ����Ă��������Ăї��p���鎞�ɎQ�Ƃ��Đݒ肷�邽�߂̃��\�b�h
	public void setReference(ServerMain ancestor,DataGeneraterImpl generater,ResultDatabase database){
		this.ancestor = ancestor;
		this.generater = generater;
		this.resultDB = database;
	}
	
	private ServerLogWraper getWaitingJob(byte[] signiture){
		Object tmp;
		synchronized(waitingBackJobs){
		    ServerMain.fMonitor.informChildMoving();
		    try{
		        tmp = waitingBackJobs.get(byteArrayToString(signiture));
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
		
		if(tmp!=null){
			return (ServerLogWraper) tmp;
		}else{
			return null;
		}
	}
	
	//�ԋp���ꂽjob�ɂ��ď������s��
	//������tmpIncluded�Ƀf�[�^��ێ�����ClientInfo��n������
	public void processCulculatedJob(CulculateData receiveJob,ClientInfo cInfo){
		
	    //�v�Z���ʂ̐M�������m�F���ꂽ�ꍇ(check�̓����ŏ������s���Ă���̂Œ���)
		if(culculatedJobCheck(receiveJob,cInfo)){
			ServerLogWraper tmp = removeWaitingJob(receiveJob.getSigniture());
		    if(tmp != null){   //�����Ǝ�菜�����A�܂�A�ُ�ł͂Ȃ�job�������ꍇ�i�]��̌v�Z���ʂƂ�����Ȃ��Ƃ������ł�����j
				removeAllSameJobsFromDisQ(receiveJob.getSigniture());       //�����W���u�͂��ׂĔz�z�҂��L���[�����菜���Ă���
				
				ancestor.sGUI.jobTb.writeJobResut(tmp);           //job�̌��ʂ�\�ɒǉ�
				pastJobDB.addOneLogWraper(tmp);
				resultDB.addOneResult(cInfo.tmpIncluded);   //�v�Z����(���̑��������܁j���i�[
		    }
			
			ServerMain.sDataToFile.plocPlant.doSomeThing(receiveJob);      //��߂�ꂽ���炩�̏������s��
		}else{ //�܂��M�������m�F����Ȃ��ꍇ
		    //�����ł͓��ɉ������Ȃ�
		}
	}
	
	//�N���C�A���g�ɂ���ĕԂ��ꂽ�v�Z���ʂ��`�F�b�N���A�����ŏ������s���B�߂�l=�����Ƃ��Ă悢���ǂ���
	public boolean culculatedJobCheck(CulculateData receiveJob,ClientInfo cInfo){
		ServerLogWraper waiting =  getWaitingJob(receiveJob.getSigniture());
		
		if(waiting != null){
			try {
				boolean checkResult = waiting.checkCompleted(receiveJob,new ContributerProperty(cInfo,receiveJob.getDistributeDate(),receiveJob.getCompleteDate()));
//				if(checkResult == true){   //�M�������m���߂�ꂽ�ꍇ�ԋp�ҋ@�L���[�����菜��
//					removeWaitingJob(receiveJob.getSigniture());
//				}
				
				return checkResult; 
			} catch (FindillegalResultException e) {
				//�v�Z���ʂ��H������Ă��܂����ꍇ�̏���
				ServerLogWraper tmp = removeWaitingJob(receiveJob.getSigniture());     //�ԋp�ҋ@�L���[�����菜��
				addReJobToQueue(tmp.getWrapped());   //�����ɕێ�����Ă���܂�����ȃf�[�^��z�z�҂��L���[�ɍĔz�z�Ƃ��Ēǉ�����
				return false;
			}
		}else{     //�߂��Ă����i�������ҋ@��ԂłȂ��ꍇ�i���łɊ����Ƃ���Ă��܂��Ă���Ƃ������j
			return true;
		}
	}
	
	//�Ώۂ̃N���C�A���g�ɑ΂��Ė��z�z�̃W���u��z�z�҂��L���[����T�������o���B
	//�����܂ōs���Ă��܂����ꍇ�ǉ���[���s���Ă��܂�
	private CulculateData serchFreshJob(byte[] signiture){
	    CulculateData tmpData;
	    synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    try{
				for(int i=0;i<distributeJobQueue.size();i++){
					ServerLogWraper tmp = getWaitingJob(((CulculateData)distributeJobQueue.get(i)).getSigniture());
					
					if(tmp!=null){  //�ԋp�ҋ@�L���[�Ɋ܂܂�Ă��Ă��ꂪ�����ƕԂ��Ă����ꍇ
					    if(tmp.checkFormerDistributer(signiture)==false){  //�Ώۂɑ΂��ĐV�N�Ȃi�������������ꍇ�����Ԃ�
					        return tmp.getWrapped();
						}
					}else{    //�ԋp�ҋ@�L���[�Ɋ܂܂�Ă��Ȃ��Ƃ������́A���z�z�Ƃ������Ȃ̂ł����Ԃ�
					    return (CulculateData) distributeJobQueue.remove(i);
					}
				}
				//�z�z�ҋ@�L���[�̒��ɖ��z�z�̃W���u���Ȃ������ꍇ
				int nowQueueCount = distributeJobQueue.size();
				fillDistributeJobQ(nowQueueCount+1);
				tmpData = (CulculateData) distributeJobQueue.get(nowQueueCount+1-1);   //�ǉ������W���u�̐擪�̕���z�z����
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	    return tmpData;
	}
	
	//�z�z�ҋ@�L���[�̒��Ŏw�肵���v�Z�f�[�^�i�V�O�j�`���j�������̂ɔz�z�N���C�A���g��ǉ�����
	//�����@jobSigniture:�W���u�̃V�O�j�`�� clientSigniture:�N���C�A���g�̃V�O�j�`��
	private void addDistributerToWaitingQ(byte jobSigniture[],byte[] clientSigniture ){
	    ServerLogWraper tmp = getWaitingJob(jobSigniture);
		if(tmp != null){
			tmp.addDistributer(clientSigniture);
		}
	}

    /**
     * 
     * @uml.property name="redundancyCount"
     */
    public int getRedundancyCount() {
        return redundancyCount;
    }

    /**
     * 
     * @uml.property name="redundancyCount"
     */
    public void setRedundancyCount(int redundancyCount) {
        this.redundancyCount = redundancyCount;
    }

	
	public Iterator getWaitingQIterator(){
//		Collection tmp = waitingBackJobs.values();
//		return tmp.iterator();
	    return ((PersistentLinkedHashMap)waitingBackJobs).iterator();
	}
	
	public Iterator getDistributeQIterator(){
		return distributeJobQueue.iterator();
	}
	
	//�����V�O�j�`��������job��z�z�҂��L���[���炷�ׂĎ�菜��
	public void removeAllSameJobsFromDisQ(byte signiture[]){
		synchronized(distributeJobQueue){
		    ServerMain.fMonitor.informChildMoving();
		    try{
				Iterator serchIterator = getDistributeQIterator();
				String strSigniture = byteArrayToString(signiture);
				ArrayList removeJobs = new ArrayList();     //��������f�[�^�̃C���f�b�N�X��ێ����Ă����i��ɂ܂Ƃ߂ď������邽��)
				while(serchIterator.hasNext()){  //�z�z�҂��L���[�𑖍����ē����V�O�j�`������������菜��
					CulculateData tmp = (CulculateData) serchIterator.next();
					if(byteArrayToString(tmp.getSigniture()).equals(strSigniture)){
						removeJobs.add(tmp);   //�܂Ƃ߂ď������邽�߂ɃX�g�b�N���Ă���
					}
				}
				
				serchIterator = getDistributeQIterator();   //�C�e���[�^�[���Ăѐ擪��
				//��v�������̂��܂Ƃ߂ď������Ă���
				Iterator removeJobsIterator = removeJobs.iterator();
				while(removeJobsIterator.hasNext()){
					distributeJobQueue.remove(removeJobsIterator.next());
				}
		    }finally{
		        ServerMain.fMonitor.cancelChildMoving();    
		    }
		}
	}
}
