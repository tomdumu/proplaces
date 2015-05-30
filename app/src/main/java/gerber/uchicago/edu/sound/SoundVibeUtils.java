package gerber.uchicago.edu.sound;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Vibrator;

/**
 * Created by ag on 4/28/2015.
 */
public class SoundVibeUtils {


    private static MediaPlayer mp;



    public  static void vibrateMe(Context context){
        Vibrator v = (Vibrator) context.getSystemService(context.VIBRATOR_SERVICE);
        // Vibrate for 500 milliseconds
        v.vibrate(500);
    }


    public static  void playSound(Context context,  int id ){

        mp = MediaPlayer.create(context, id);
        mp.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {

            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO Auto-generated method stub
                mp.reset();
                mp.release();
                mp=null;
            }

        });
        mp.start();



    }
}
