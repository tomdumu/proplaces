package gerber.uchicago.edu;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Created by Edwin on 15/02/2015.
 */
public class TabGridOld extends Fragment {

    public static final String ARGS_PLACE = "arg_place" ;
    public static final String ARGS_PEEPS = "arg_peeps" ;



    public static TabGridOld newInstance(int nId, boolean bPlace) {
        TabGridOld fragment = new TabGridOld();
        Bundle args = new Bundle();
        if (bPlace){
            args.putInt(ARGS_PLACE, nId);
        } else {
            args.putInt(ARGS_PEEPS, nId);
        }

        fragment.setArguments(args);
        return fragment;
    }


    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.tab_3,container,false);
        int nId =  getArguments().getInt(TabGridOld.ARGS_PLACE);
        TextView textView = (TextView) v.findViewById(R.id.textView);
        textView.setText(String.valueOf(nId));

        return v;
    }
}