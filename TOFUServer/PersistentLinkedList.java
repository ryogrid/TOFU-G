/* ************************************************************** *
 *                                                                *
 * Copyright (c) 2005, Ryo Kanbayashi, All rights reserved.       *
 *                                                                *
 *                                                                *
 * This software is distributed under the modified BSD License.   *
 * ************************************************************** */

import java.io.*;
import java.io.IOException;
import java.io.Serializable;
import java.util.*;
import java.util.Hashtable;

//���ʈȏ�ɂȂ�����t�@�C���֏����o��LinkedList
//LinkedList���p���͂��Ă��邪�S�Ẵ��\�b�h�������͂��Ă��Ȃ��̂Ŏg�p�ɂ͒��ӂ�v��
//�g�p�̃��\�b�h�͈ȉ�
//addFirst(Serializable),addLast(Serializable),getFirst(),getLast(),remove(Object),get(int),remove(int),size(),iterator(),����Iterator�̊e��C�e���[�V����
public class PersistentLinkedList implements Cloneable, Serializable {
    private final int MAX_WASTE_SIZE=10485760;      //�t�@�C�����̃S�~�̏������s���������l�B�Ƃ肠�����P�O�l
    private int nowAllWasteArea = 0;    //���݂̖��ʗ̈�T�C�Y     
    private int iMaxCacheSize; //�L���b�V���̍ő听���T�C�Y
    private int iCacheSize; //���݂̃L���b�V���T�C�Y

    /**
     * 
     * @uml.property name="cache"
     * @uml.associationEnd inverse="this$0:PersistentLinkedList$CacheEntry" qualifier=
     * "new:java.lang.Integer PersistentLinkedList$CacheEntry" multiplicity="(0 1)"
     */
    private Hashtable cache = new Hashtable();//�L���b�V��

    /**
     * 
     * @uml.property name="rows"
     * @uml.associationEnd elementType="PersistentLinkedList$StubEntry" multiplicity="(0
     * -1)"
     */
    private LinkedList rows = new LinkedList();//LinkedList�̎ʂ��A�}������e�G���g����StubEntry�I�u�W�F�N�g�������X�g�A����

    /**
     * 
     * @uml.property name="list"
     * @uml.associationEnd inverse="this$0:PersistentLinkedList$StubEntry" qualifier="key:java.lang.Integer
     * PersistentLinkedList$CacheEntry" multiplicity="(0 -1)"
     */
    private LinkedList list = new LinkedList(); //�p�x�ɂ�郊�X�g

    /**
     * 
     * @uml.property name="of"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private ObjectFile of; //�e�I�u�W�F�N�g������t�@�C��

    private String sTmpName; //�t�@�C����
    private String filePath = null;     //�ۑ�����t�@�C���̃p�X
    private int elementCount = 0;        //size()���\�b�h�ŕԂ����߂̗v�f��
    
    public synchronized void addFirst(Serializable o) throws IOException{
        StubEntry e = new StubEntry();

        rows.addFirst(e);
        elementCount++;

        //add�����v�f�ȍ~��stubEntry��CacheEntry��index�ɂ��}�b�s���O��␳����
        int size = rows.size()-1;     //+1�͂��ł�remove���Ă��܂���idx�Ԗڂ̗v�f�̕�
        for(int i=size;i>=0;i--){      //�L���b�V���ł̃L�[���P�Â��炷
            CacheEntry adjustTgt = (CacheEntry) cache.remove(new Integer(i));
            if(adjustTgt!=null){   //�l�������ƕԂ��Ă����B�܂�L���b�V���Ɋ܂܂�Ă����ꍇ
            	Integer newIndex =new Integer(i+1);
            	adjustTgt.key = newIndex;
                cache.put(newIndex,adjustTgt);
            }
        }
        
        if (iCacheSize < iMaxCacheSize) {
            e.inCache = true;
            CacheEntry ce = new CacheEntry();
            ce.o = o;
            ce.key = new Integer(0);
            Object before = cache.put(ce.key, ce);
            if(before==null){
            	iCacheSize++;                
            }else{
                list.remove(before);
            }
            
            list.addFirst(ce);
        } else {
            if (of == null) {
                openTempFile();
            }
            e.filePointer = of.writeObject(o);
        }
    }
    
//    private synchronized void writeObject(java.io.ObjectOutputStream s) throws IOException
//    {
//        System.out.println("PersistentLinkedList�̏������݂��n�߂܂���");
//        s.defaultWriteObject();
//	}
//    
//    private void readObject(java.io.ObjectInputStream s) throws java.io.IOException, ClassNotFoundException  {
//        s.defaultReadObject();
//    }
    
    public synchronized void addLast(Serializable o) throws IOException {
        StubEntry e = new StubEntry();

        rows.addLast(e);
        elementCount++;

        if (iCacheSize < iMaxCacheSize) {
            e.inCache = true;
            CacheEntry ce = new CacheEntry();
            ce.o = o;
            ce.key = new Integer(rows.size() - 1);
            Object before = cache.put(ce.key, ce);
            if(before==null){
                iCacheSize++;
            }else{
                list.remove(before);                
            }
            
            list.addFirst(ce);
        } else {
            if (of == null) {
                openTempFile();
            }
            e.filePointer = of.writeObject(o);
        }
    }
    
    public synchronized Object getFirst() {
        return get(0);
    }
    
    public synchronized Object getLast() {
        return get(size()-1);
    }
    
    public synchronized boolean remove(Object o) {
        for(int i=0;i<elementCount;i++){
            if(o.equals(get(i))){
                remove(i);
                return true;
            }
        }
        return false;
    }
    
    //�z��̍ŏ��G���g����\����������N���X
    class StubEntry implements Serializable {
        boolean inCache;//�G���g�����L���b�V���ɂ��邩
//        boolean isArive=true;    //���̗v�f�͗L���ł��邩�B�����ł���΃t�@�C�����Ŗ��ʂȗ̈���g�p���Ă���\������B�����͌�ň�|����B
        long filePointer = -1;//�G���g�����f�B�X�N�ɂ���΁A�ȑO�͂��̈ʒu�ɂ�����
    }

    //�L���b�V���̃G���g����\����������N���X
    class CacheEntry implements Serializable {
        Integer key; //�����Ƀ}�b�s���O�A�z��̃C���f�b�N�X�ɑΉ�
        Object o; //�L���b�V���ɓ����Object
    }

    public PersistentLinkedList(int iCacheSieze,String path) {
        this.iMaxCacheSize = iCacheSieze;
        this.filePath = path;
    }

    private synchronized final void openTempFile() throws IOException {
        boolean bInValid = true;

        if(filePath!=null){
            of = new ObjectFile(filePath);
        }else{
            throw new IOException("file path haven't been given!!");
        }
    }

    public synchronized final void add(Serializable o) throws IOException {
        StubEntry e = new StubEntry();

        rows.add(e);
        elementCount++;

        if (iCacheSize < iMaxCacheSize) {
            e.inCache = true;
            CacheEntry ce = new CacheEntry();
            ce.o = o;
            ce.key = new Integer(rows.size() - 1);
            Object before = cache.put(ce.key, ce);
            if(before==null){
                iCacheSize++;
            }else{
                list.remove(before);
            }
            
            list.addFirst(ce);
        } else {
            if (of == null) {
                openTempFile();
            }
            e.filePointer = of.writeObject(o);
        }
    }

    //�I�u�W�F�N�g���L���b�V���܂��̓t�@�C��������o��
    public synchronized Object get(int idx) {

        if (idx < 0 || idx >= rows.size()) {
            throw new IndexOutOfBoundsException("Index: " + idx
                    + "out of bounds.");
        }

        StubEntry e = (StubEntry) rows.get(idx);

        Object o = null;

        if (e.inCache) {
            CacheEntry ce = null;//�G���g�����擾
            ce = (CacheEntry) cache.get(new Integer(idx));

            if (ce == null) {
                System.out.println("Element at idx " + idx + "is NULL");
            }

            if ((ce != null && ce.o == null)) {
                System.out.println("Cache Element's object at idx" + idx
                        + " Not in cache!");
            }

            o = ce.o;

            list.remove(ce);
            //���X�g�̍ŏ��Ɉړ�
            list.addFirst(ce);
        } else {
            e.inCache = true; //�L���b�V���ɂ���

            //���o���ăL���b�V���Ɉړ�����
            try {
                o = of.readObject(e.filePointer);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            //�L���b�V�������E���ǂ����`�F�b�N
            if (iCacheSize >= iMaxCacheSize) {
                CacheEntry leastUsed = (CacheEntry) list.getLast(); //���X�g�̏I�[����苎��

                list.remove(leastUsed);

                //���o�����I�u�W�F�N�g���L���b�V���ɉ�����
                CacheEntry ce = new CacheEntry();
                ce.o = o;
                ce.key = new Integer(idx);
                Object before = cache.put(ce.key, ce);
                if(before == null){
                    iCacheSize++;
                }else{
                    list.remove(before);
                }

                list.addFirst(ce);

                //�폜�̂��߂�StubEntry�𓾂�
                StubEntry outStubEntry = (StubEntry) rows.get(leastUsed.key
                        .intValue());

                //�I�u�W�F�N�g���L���V�����瓾��
                CacheEntry outCacheEntry = (CacheEntry) cache
                        .remove(leastUsed.key);
                iCacheSize--;
                
                if (outCacheEntry == null) {
                    throw new RuntimeException("Cache Entry at "
                            + leastUsed.key.intValue() + " is Null!");
                }

                if (outCacheEntry != null && outCacheEntry.o == null) {
                    throw new RuntimeException("Cache object at "
                            + leastUsed.key.intValue() + " is Null!");
                }

                Object outObject = outCacheEntry.o;
                outStubEntry.inCache = false;

                if (outStubEntry.filePointer == -1) { //�L���b�V���ɍs����
                    outStubEntry.filePointer = of
                            .writeObject((Serializable) outObject);
                } else {
                    //���łɃt�@�C���ɂ���
                    
                    int iCurrentSize=0;
                    ByteArrayOutputStream baos = null;
                    ObjectOutputStream oos=null;
                    try{
	                    iCurrentSize = of
	                            .getObjectLength(outStubEntry.filePointer);
	
	                    baos = new ByteArrayOutputStream();
	                    oos = new ObjectOutputStream(baos);
	                    oos.writeObject((Serializable) outObject);
	                    oos.flush();
                    }catch(IOException err){
                        err.printStackTrace();
                    }
                    
                    int datalen = baos.size();

                    if (datalen <= iCurrentSize) {
                        of.reWriteObject(outStubEntry.filePointer, baos
                                .toByteArray());
                    } else {
                        outStubEntry.filePointer = of
                                .writeObject((Serializable) outObject);
                    }

                    baos = null;
                    oos = null;
                    outObject = null;
                }
            } else {
                CacheEntry ce = new CacheEntry();
                ce.o = o;
                ce.key = new Integer(idx);
                Object before = cache.put(ce.key, ce);
                if(before == null){
                    iCacheSize++;
                }else{
                    list.remove(before);                    
                }

        		list.addFirst(ce);
            }
        }
        return o;
    }
    
    //�I���������s��
    public synchronized void finalize()throws IOException{
        if(of!=null){
            of.close();
        }
    }
    
    public synchronized Object remove(int idx){
        
        if (idx < 0 || idx >= rows.size()) {
            throw new IndexOutOfBoundsException("Index: " + idx
                    + "out of bounds.");
        }

        StubEntry e = (StubEntry) rows.remove(idx);
        elementCount--;
        
        Object o = null;

        if (e.inCache) {
            CacheEntry ce = null;//�G���g�����擾
            ce = (CacheEntry) cache.remove(new Integer(idx));
            iCacheSize--;

            if (ce == null) {
                System.out.println("Element at idx " + idx + "is NULL");
            }

            if ((ce != null && ce.o == null)) {
                System.out.println("Cache Element's object at idx" + idx
                        + " Not in cache!");
            }

            o = ce.o;

        	list.remove(ce);
            
            if(e.filePointer!=-1){  //�t�@�C�����ɂ������
                int size=0;
                try {
                    size = of.getObjectLength(e.filePointer);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                nowAllWasteArea+=size;
            }
            
            
        } else {    //�L���b�V���ɂ͖����A�܂�t�@�C���̒��ɍs���Ă���
            try {
                o = of.readObject(e.filePointer);
            } catch (IOException e1) {
                e1.printStackTrace();
            } catch (ClassNotFoundException e1) {
                e1.printStackTrace();
            }
            
            int size=0;
            try {
                size = of.getObjectLength(e.filePointer);
            } catch (IOException e2) {
                e2.printStackTrace();
            }
            nowAllWasteArea+=size;
            //���ʂȗ̈�͌�ňꊇ���ď�������            
        }
        
        //remove�����v�f�ȍ~��stubEntry��CacheEntry��index�ɂ��}�b�s���O��␳����
        int size = rows.size()+1;     //+1�͂��ł�remove���Ă��܂���idx�Ԗڂ̗v�f�̕�
        for(int i=idx+1;i<size;i++){      //�L���b�V���ł̃L�[���P�Â��炷
            CacheEntry adjustTgt = (CacheEntry) cache.remove(new Integer(i));
            if(adjustTgt!=null){   //�l�������ƕԂ��Ă����B�܂�L���b�V���Ɋ܂܂�Ă����ꍇ
            	Integer newIndex =new Integer(i-1);
            	adjustTgt.key = newIndex;
                cache.put(newIndex,adjustTgt);
            }
        }
        
        if(nowAllWasteArea>=MAX_WASTE_SIZE){      //�S�~���\���ɂ��܂��Ă��܂�����
            doGerbageClean();
            nowAllWasteArea=0; //�S�~���Z�b�g
        }
        return o;
    }
    
    //���܂��Ă��܂��������ȗ̈�����ׂĎ�菜�����f�[�^�t�@�C���𐶐�����Brows�̒��̖����������v�f��null�̗v�f�ɂȂ�̂Œ���
    private synchronized void doGerbageClean(){
        of.doGerbageClean(rows);
    }

    public synchronized int size(){
        return elementCount;
    }
    
    public synchronized Iterator iterator() {
        return new PersitentItr();
    }
    
    //�I���W�i����Itrator�B�����́A�e���瓾��Itrator�����b�s���O
    private class PersitentItr implements Iterator,Serializable {
        int cursor = 0;
        int lastRet = -1;
        Iterator innerItr=null;

        /**
         * 
         * @uml.property name="lastGot"
         * @uml.associationEnd multiplicity="(0 1)"
         */
        StubEntry lastGot = null; //next()�̌Ăяo���ōŌ�ɓ���ꂽStubEntry

        public PersitentItr(){
            innerItr = rows.iterator();
        }
        
        public boolean hasNext() {
            return innerItr.hasNext();
        }

        public Object next() {
            try {
                StubEntry e = (StubEntry) innerItr.next();
                lastGot = e;   //�Ō�ɓ������̂�remove()�̎��Ȃǂ̂��߂ɕێ����Ă���

                Object o = null;

                if (e.inCache) {
                    CacheEntry ce = null;//�G���g�����擾
                    ce = (CacheEntry) cache.get(new Integer(cursor));

                    if (ce == null) {
                        System.out.println("Element at idx " + cursor + "is NULL");
                    }

                    if ((ce != null && ce.o == null)) {
                        System.out.println("Cache Element's object at idx" + cursor
                                + " Not in cache!");
                    }

                    o = ce.o;

                    list.remove(ce);
                    list.addFirst(ce);
                } else {
                    e.inCache = true; //�L���b�V���ɂ���

                    //���o���ăL���b�V���Ɉړ�����
                    try {
                        o = of.readObject(e.filePointer);
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    //�L���b�V�������E���ǂ����`�F�b�N
                    if (iCacheSize >= iMaxCacheSize) {
                        CacheEntry leastUsed = (CacheEntry) list.getLast(); //���X�g�̏I�[����苎��
                        list.remove(leastUsed);

                        //���o�����I�u�W�F�N�g���L���b�V���ɉ�����
                        CacheEntry ce = new CacheEntry();
                        ce.o = o;
                        ce.key = new Integer(cursor);
                        Object before = cache.put(ce.key, ce);
                        if(before==null){
                            iCacheSize++;
                        }else{
                            list.remove(before);
                        }

                        list.addFirst(ce);

                        //�폜�̂��߂�StubEntry�𓾂�
                        StubEntry outStubEntry = (StubEntry) rows.get(leastUsed.key
                                .intValue());

                        //�I�u�W�F�N�g���L���V�����瓾��
                        CacheEntry outCacheEntry = (CacheEntry) cache
                                .remove(leastUsed.key);
                        iCacheSize--;
                        
                        if (outCacheEntry == null) {
                            throw new RuntimeException("Cache Entry at "
                                    + leastUsed.key.intValue() + " is Null!");
                        }

                        if (outCacheEntry != null && outCacheEntry.o == null) {
                            throw new RuntimeException("Cache object at "
                                    + leastUsed.key.intValue() + " is Null!");
                        }

                        Object outObject = outCacheEntry.o;
                        outStubEntry.inCache = false;

                        if (outStubEntry.filePointer == -1) { //�L���b�V���ɍs����
                            outStubEntry.filePointer = of
                                    .writeObject((Serializable) outObject);
                        } else {
                            //���łɃt�@�C���ɂ���
                            
                            int iCurrentSize=0;
                            ByteArrayOutputStream baos = null;
                            ObjectOutputStream oos=null;
                            try{
        	                    iCurrentSize = of
        	                            .getObjectLength(outStubEntry.filePointer);
        	
        	                    baos = new ByteArrayOutputStream();
        	                    oos = new ObjectOutputStream(baos);
        	                    oos.writeObject((Serializable) outObject);
        	                    oos.flush();
                            }catch(IOException err){
                                err.printStackTrace();
                            }
                            
                            int datalen = baos.size();

                            if (datalen <= iCurrentSize) {
                                of.reWriteObject(outStubEntry.filePointer, baos
                                        .toByteArray());
                            } else {
                                outStubEntry.filePointer = of
                                        .writeObject((Serializable) outObject);
                            }

                            baos = null;
                            oos = null;
                            outObject = null;
                        }
                    } else {
                        CacheEntry ce = new CacheEntry();
                        ce.o = o;
                        ce.key = new Integer(cursor);
                        Object before = cache.put(ce.key, ce);
                        if(before==null){
                            iCacheSize++;
                        }else{
                            list.remove(before);
                        }
                        
                        list.addFirst(ce);
                    }
                }
                
                lastRet = cursor++;
                return o;
            } catch(IndexOutOfBoundsException e) {
                throw new NoSuchElementException();
            }
        }

        public void remove() {
            if (lastRet == -1)
                throw new IllegalStateException();

            try {
                innerItr.remove();
                StubEntry e = lastGot;
                elementCount--;
                
                Object o = null;

                if (e.inCache) {
                    CacheEntry ce = null;//�G���g�����擾
                    ce = (CacheEntry) cache.remove(new Integer(lastRet));
                    iCacheSize--;

                    if (ce == null) {
                        System.out.println("Element at lastRet " + lastRet + "is NULL");
                    }

                    if ((ce != null && ce.o == null)) {
                        System.out.println("Cache Element's object at lastRet" + lastRet
                                + " Not in cache!");
                    }

                    o = ce.o;
                    
                    list.remove(ce);
                    
                    if(e.filePointer!=-1){  //�t�@�C�����ɂ������
                        int size=0;
                        try {
                            size = of.getObjectLength(e.filePointer);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        nowAllWasteArea+=size;
                    }
                    
                    
                } else {    //�L���b�V���ɂ͖����A�܂�t�@�C���̒��ɍs���Ă���
                    try {
                        o = of.readObject(e.filePointer);
                    } catch (IOException e1) {
                        e1.printStackTrace();
                    } catch (ClassNotFoundException e1) {
                        e1.printStackTrace();
                    }
                    
                    int size=0;
                    try {
                        size = of.getObjectLength(e.filePointer);
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
                    nowAllWasteArea+=size;
                    //���ʂȗ̈�͌�ňꊇ���ď�������            
                }
                
                //remove�����v�f�ȍ~��stubEntry��CacheEntry��index�ɂ��}�b�s���O��␳����
                int size = rows.size()+1;     //+1�͂��ł�remove���Ă��܂���idx�Ԗڂ̗v�f�̕�
                for(int i=lastRet+1;i<size;i++){      //�L���b�V���ł̃L�[���P�Â��炷
                    CacheEntry adjustTgt = (CacheEntry) cache.remove(new Integer(i));
                    if(adjustTgt!=null){   //�l�������ƕԂ��Ă����B�܂�L���b�V���Ɋ܂܂�Ă����ꍇ
                    	Integer newIndex =new Integer(i-1);
                    	adjustTgt.key = newIndex;
                        cache.put(newIndex,adjustTgt);
                    }
                }
                
                if(nowAllWasteArea>=MAX_WASTE_SIZE){      //�S�~���\���ɂ��܂��Ă��܂�����
                    doGerbageClean();
                    nowAllWasteArea=0; //�S�~���Z�b�g
                }
                
                if (lastRet < cursor)
                    cursor--;
                lastRet = -1;
            } catch(IndexOutOfBoundsException e) {
                throw new ConcurrentModificationException();
            }
        }
    }
}