package gerber.uchicago.edu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Created by Edwin on 15/02/2015.
 */
public class ScrollFrag extends Fragment {




    public static final String LAYOUT_KEY = "LAYOUT";

    public static ScrollFrag getInstance(Bundle bundle){

        ScrollFrag scrollFrag = new ScrollFrag();

        scrollFrag.setArguments(bundle);

        return scrollFrag;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        int nLayout = 0;
        try {
            nLayout = getArguments().getInt(LAYOUT_KEY);
        } catch (Exception e) {
           // e.printStackTrace();
            nLayout = R.layout.frag_scroll_layout_new;
        }



        StateMgr.getInstance().put(nLayout, this);

        View v = inflater.inflate(nLayout,container,false);
        return v;
    }


}