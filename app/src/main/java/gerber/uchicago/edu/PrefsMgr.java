package gerber.uchicago.edu;

/**
 * Created by ag on 5/19/2015.
 */
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class PrefsMgr {




    private static SharedPreferences sSharedPreferences;

    public static void setString(Context context, String locale, String code) {
        sSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sSharedPreferences.edit();
        editor.putString(locale, code);
        editor.commit();

    }

    public static String getString(Context context, String locale) {
        sSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        return sSharedPreferences.getString(locale, null);

    }

    public static void setBooleanArray(Context context, String key, boolean[] array ) {
        sSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sSharedPreferences.edit();


        for (int nC = 0; nC <array.length ; nC++) {
            editor.putBoolean(key + nC, array[nC]);
        }

        editor.commit();
    }

    public static boolean[] getBooleanArray(Context context, String key, int size) {
        sSharedPreferences =
                PreferenceManager.getDefaultSharedPreferences(context);

        boolean[] array = new boolean[size];
        for (int nC = 0; nC < array.length; nC++) {
            array[nC] = sSharedPreferences.getBoolean(key + nC, false);
        }
        return array;

    }


}