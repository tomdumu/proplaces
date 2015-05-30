package gerber.uchicago.edu;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;

/**
 * Created by Adam Gerber on 5/27/2014.
 * University of Chicago
 */
public class FavActionUtility {

    private Context mContext;

    public FavActionUtility(Context context) {
        mContext = context;
    }

    public void navigateTo(String strAddress, String strCity) throws Exception {
        Intent intentNavTo = new Intent(Intent.ACTION_VIEW,
                Uri.parse("google.navigation:q=" + strAddress + " " + strCity));
          mContext.startActivity(intentNavTo);
    }

    public void mapOf(String strAddress, String strCity) throws Exception {
        Intent intentMapOf = new Intent(Intent.ACTION_VIEW,
                Uri.parse("geo:0,0?q=" + strAddress + " " + strCity));
        mContext.startActivity(intentMapOf);
    }

    public void dial(String strPhone) throws Exception {
        Intent intentDial = new Intent(Intent.ACTION_CALL);
        intentDial.setData(Uri.parse("tel:" + strPhone));
        mContext.startActivity(intentDial);
    }

    public void yelpSite(String strYelpUrl)throws Exception {
        Intent intentYelp = new Intent(Intent.ACTION_VIEW);
        intentYelp.setData(Uri.parse(strYelpUrl));
        mContext.startActivity(intentYelp);
    }


    public void share(String strTitle, String strBody)throws Exception {

        Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
        sharingIntent.setType("text/plain");
        sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, strTitle);
        sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, strBody);
        mContext.startActivity(Intent.createChooser(sharingIntent, "Choose client"));


    }


    public void showErrorMessageInDialog(final String strErrorMessage){
        final AlertDialog.Builder builder = new AlertDialog.Builder(mContext);
        builder.setTitle("Error")
                .setMessage(strErrorMessage)
                .setCancelable(false);

        builder.setPositiveButton("Close", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int id) {

                dialog.cancel();
            }
        });

        AlertDialog alert = builder.create();
        alert.show();

    }


}
