package gerber.uchicago.edu;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by ag on 5/17/2015.
 */
public class StateMgr {

    private static Map<Integer, ScrollFrag> mPageReferenceMap;

    private static StateMgr ourInstance = new StateMgr();
    private static boolean sBooleanNew;

    public static StateMgr getInstance() {

        if(ourInstance == null) {
            ourInstance = new StateMgr();
            return ourInstance;
        } else {
            return ourInstance;
        }

    }

    private StateMgr() {


        mPageReferenceMap = new HashMap<Integer, ScrollFrag>();
        sBooleanNew = true;

    }

    public static ScrollFrag get(int key){
        return mPageReferenceMap.get(key);
    }
    public static ScrollFrag put(int key, ScrollFrag frag){
       return mPageReferenceMap.put(key, frag);
    }

    public static boolean isNew() {
        return sBooleanNew;
    }

    public static void setNew(boolean sBooleanNew) {
        StateMgr.sBooleanNew = sBooleanNew;
    }
}
