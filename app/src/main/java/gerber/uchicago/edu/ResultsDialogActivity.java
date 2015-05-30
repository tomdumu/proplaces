package gerber.uchicago.edu;

import android.app.ListActivity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import java.util.ArrayList;


public class ResultsDialogActivity extends ListActivity {

    public static final String POSITION = "position";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);



        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        ArrayList<String> businessArrayList = (ArrayList<String>) getIntent().getSerializableExtra("simple_data_bundle_key");

        ArrayAdapter<String> modeAdapter = new ArrayAdapter<String>(this, R.layout.pop_layout, R.id.pop_text, businessArrayList);



        setListAdapter(modeAdapter);

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                Intent returnIntent = new Intent();
                returnIntent.putExtra(POSITION, position);
                setResult(RESULT_OK,returnIntent);
                finish();

            }
        });
    }



}
