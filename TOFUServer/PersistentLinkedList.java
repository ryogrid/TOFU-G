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

//一定量以上になったらファイルへ書き出すLinkedList
//LinkedListを継承はしているが全てのメソッドを実装はしていないので使用には注意を要す
//使用可のメソッドは以下
//addFirst(Serializable),addLast(Serializable),getFirst(),getLast(),remove(Object),get(int),remove(int),size(),iterator(),得たIteratorの各種イテレーション
public class PersistentLinkedList implements Cloneable, Serializable {
    private final int MAX_WASTE_SIZE=10485760;      //ファイル中のゴミの処理を行うしきい値。とりあえず１０Ｍ
    private int nowAllWasteArea = 0;    //現在の無駄領域サイズ     
    private int iMaxCacheSize; //キャッシュの最大成長サイズ
    private int iCacheSize; //現在のキャッシュサイズ

    /**
     * 
     * @uml.property name="cache"
     * @uml.associationEnd inverse="this$0:PersistentLinkedList$CacheEntry" qualifier=
     * "new:java.lang.Integer PersistentLinkedList$CacheEntry" multiplicity="(0 1)"
     */
    private Hashtable cache = new Hashtable();//キャッシュ

    /**
     * 
     * @uml.property name="rows"
     * @uml.associationEnd elementType="PersistentLinkedList$StubEntry" multiplicity="(0
     * -1)"
     */
    private LinkedList rows = new LinkedList();//LinkedListの写し、挿入する各エントリのStubEntryオブジェクトだけをストアする

    /**
     * 
     * @uml.property name="list"
     * @uml.associationEnd inverse="this$0:PersistentLinkedList$StubEntry" qualifier="key:java.lang.Integer
     * PersistentLinkedList$CacheEntry" multiplicity="(0 -1)"
     */
    private LinkedList list = new LinkedList(); //頻度によるリスト

    /**
     * 
     * @uml.property name="of"
     * @uml.associationEnd multiplicity="(0 1)"
     */
    private ObjectFile of; //各オブジェクトがあるファイル

    private String sTmpName; //ファイル名
    private String filePath = null;     //保存するファイルのパス
    private int elementCount = 0;        //size()メソッドで返すための要素数
    
    public synchronized void addFirst(Serializable o) throws IOException{
        StubEntry e = new StubEntry();

        rows.addFirst(e);
        elementCount++;

        //addした要素以降のstubEntryとCacheEntryのindexによるマッピングを補正する
        int size = rows.size()-1;     //+1はすでにremoveしてしまったidx番目の要素の分
        for(int i=size;i>=0;i--){      //キャッシュでのキーを１つづつずらす
            CacheEntry adjustTgt = (CacheEntry) cache.remove(new Integer(i));
            if(adjustTgt!=null){   //値がちゃんと返ってきた。つまりキャッシュに含まれていた場合
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
//        System.out.println("PersistentLinkedListの書き込みを始めました");
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
    
    //配列の最小エントリを表現する内部クラス
    class StubEntry implements Serializable {
        boolean inCache;//エントリがキャッシュにあるか
//        boolean isArive=true;    //この要素は有効であるか。無効であればファイル内で無駄な領域を使用している可能性あり。それらは後で一掃する。
        long filePointer = -1;//エントリがディスクにあれば、以前はこの位置にあった
    }

    //キャッシュのエントリを表現する内部クラス
    class CacheEntry implements Serializable {
        Integer key; //成分にマッピング、配列のインデックスに対応
        Object o; //キャッシュに入れるObject
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

    //オブジェクトをキャッシュまたはファイルから取り出す
    public synchronized Object get(int idx) {

        if (idx < 0 || idx >= rows.size()) {
            throw new IndexOutOfBoundsException("Index: " + idx
                    + "out of bounds.");
        }

        StubEntry e = (StubEntry) rows.get(idx);

        Object o = null;

        if (e.inCache) {
            CacheEntry ce = null;//エントリを取得
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
            //リストの最初に移動
            list.addFirst(ce);
        } else {
            e.inCache = true; //キャッシュにある

            //取り出してキャッシュに移動する
            try {
                o = of.readObject(e.filePointer);
            } catch (ClassNotFoundException cnfe) {
                cnfe.printStackTrace();
            } catch (IOException e2) {
                e2.printStackTrace();
            }

            //キャッシュが限界かどうかチェック
            if (iCacheSize >= iMaxCacheSize) {
                CacheEntry leastUsed = (CacheEntry) list.getLast(); //リストの終端を取り去る

                list.remove(leastUsed);

                //取り出したオブジェクトをキャッシュに加える
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

                //削除のためのStubEntryを得る
                StubEntry outStubEntry = (StubEntry) rows.get(leastUsed.key
                        .intValue());

                //オブジェクトをキャシュから得る
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

                if (outStubEntry.filePointer == -1) { //キャッシュに行った
                    outStubEntry.filePointer = of
                            .writeObject((Serializable) outObject);
                } else {
                    //すでにファイルにある
                    
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
    
    //終了処理を行う
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
            CacheEntry ce = null;//エントリを取得
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
            
            if(e.filePointer!=-1){  //ファイル中にもあれば
                int size=0;
                try {
                    size = of.getObjectLength(e.filePointer);
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
                nowAllWasteArea+=size;
            }
            
            
        } else {    //キャッシュには無い、つまりファイルの中に行っている
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
            //無駄な領域は後で一括して処理する            
        }
        
        //removeした要素以降のstubEntryとCacheEntryのindexによるマッピングを補正する
        int size = rows.size()+1;     //+1はすでにremoveしてしまったidx番目の要素の分
        for(int i=idx+1;i<size;i++){      //キャッシュでのキーを１つづつずらす
            CacheEntry adjustTgt = (CacheEntry) cache.remove(new Integer(i));
            if(adjustTgt!=null){   //値がちゃんと返ってきた。つまりキャッシュに含まれていた場合
            	Integer newIndex =new Integer(i-1);
            	adjustTgt.key = newIndex;
                cache.put(newIndex,adjustTgt);
            }
        }
        
        if(nowAllWasteArea>=MAX_WASTE_SIZE){      //ゴミが十分にたまってしまったら
            doGerbageClean();
            nowAllWasteArea=0; //ゴミリセット
        }
        return o;
    }
    
    //たまってしまった無効な領域をすべて取り除いたデータファイルを生成する。rowsの中の無効だった要素はnullの要素になるので注意
    private synchronized void doGerbageClean(){
        of.doGerbageClean(rows);
    }

    public synchronized int size(){
        return elementCount;
    }
    
    public synchronized Iterator iterator() {
        return new PersitentItr();
    }
    
    //オリジナルのItrator。内部の、親から得たItratorをラッピング
    private class PersitentItr implements Iterator,Serializable {
        int cursor = 0;
        int lastRet = -1;
        Iterator innerItr=null;

        /**
         * 
         * @uml.property name="lastGot"
         * @uml.associationEnd multiplicity="(0 1)"
         */
        StubEntry lastGot = null; //next()の呼び出しで最後に得られたStubEntry

        public PersitentItr(){
            innerItr = rows.iterator();
        }
        
        public boolean hasNext() {
            return innerItr.hasNext();
        }

        public Object next() {
            try {
                StubEntry e = (StubEntry) innerItr.next();
                lastGot = e;   //最後に得たものをremove()の時などのために保持しておく

                Object o = null;

                if (e.inCache) {
                    CacheEntry ce = null;//エントリを取得
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
                    e.inCache = true; //キャッシュにある

                    //取り出してキャッシュに移動する
                    try {
                        o = of.readObject(e.filePointer);
                    } catch (ClassNotFoundException cnfe) {
                        cnfe.printStackTrace();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }

                    //キャッシュが限界かどうかチェック
                    if (iCacheSize >= iMaxCacheSize) {
                        CacheEntry leastUsed = (CacheEntry) list.getLast(); //リストの終端を取り去る
                        list.remove(leastUsed);

                        //取り出したオブジェクトをキャッシュに加える
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

                        //削除のためのStubEntryを得る
                        StubEntry outStubEntry = (StubEntry) rows.get(leastUsed.key
                                .intValue());

                        //オブジェクトをキャシュから得る
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

                        if (outStubEntry.filePointer == -1) { //キャッシュに行った
                            outStubEntry.filePointer = of
                                    .writeObject((Serializable) outObject);
                        } else {
                            //すでにファイルにある
                            
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
                    CacheEntry ce = null;//エントリを取得
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
                    
                    if(e.filePointer!=-1){  //ファイル中にもあれば
                        int size=0;
                        try {
                            size = of.getObjectLength(e.filePointer);
                        } catch (IOException e1) {
                            e1.printStackTrace();
                        }
                        nowAllWasteArea+=size;
                    }
                    
                    
                } else {    //キャッシュには無い、つまりファイルの中に行っている
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
                    //無駄な領域は後で一括して処理する            
                }
                
                //removeした要素以降のstubEntryとCacheEntryのindexによるマッピングを補正する
                int size = rows.size()+1;     //+1はすでにremoveしてしまったidx番目の要素の分
                for(int i=lastRet+1;i<size;i++){      //キャッシュでのキーを１つづつずらす
                    CacheEntry adjustTgt = (CacheEntry) cache.remove(new Integer(i));
                    if(adjustTgt!=null){   //値がちゃんと返ってきた。つまりキャッシュに含まれていた場合
                    	Integer newIndex =new Integer(i-1);
                    	adjustTgt.key = newIndex;
                        cache.put(newIndex,adjustTgt);
                    }
                }
                
                if(nowAllWasteArea>=MAX_WASTE_SIZE){      //ゴミが十分にたまってしまったら
                    doGerbageClean();
                    nowAllWasteArea=0; //ゴミリセット
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