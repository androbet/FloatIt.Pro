/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 3/24/20 1:15 PM
 * Last modified 3/24/20 10:39 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.LocationManager;

import net.geekstools.floatshort.PRO.Automation.RecoveryGps;
import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFoldersForGps;
import net.geekstools.floatshort.PRO.R;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForGps;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClass;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClassDebug;
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable;
import net.geekstools.floatshort.PRO.Utils.UI.CustomIconManager.LoadCustomIcons;

public class ReceiverGPS extends BroadcastReceiver {

    FunctionsClass functionsClass;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            functionsClass = new FunctionsClass(context);

            if (functionsClass.customIconsEnable()) {
                LoadCustomIcons loadCustomIcons = new LoadCustomIcons(context, functionsClass.customIconPackageName());
                loadCustomIcons.load();
                FunctionsClassDebug.Companion.PrintDebug("*** Total Custom Icon ::: " + loadCustomIcons.getTotalIconsNumber());
            }

            final LocationManager locManager = (LocationManager) context.getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
            if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == true && PublicVariable.receiverGPS == false) {
                Intent gps = new Intent(context, RecoveryGps.class);
                gps.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(gps);
                PublicVariable.receiverGPS = true;
            } else if (locManager.isProviderEnabled(LocationManager.GPS_PROVIDER) == false) {
                Intent w = new Intent(context, FloatingShortcutsForGps.class);
                w.putExtra(context.getString(R.string.remove_all_floatings), context.getString(R.string.remove_all_floatings));
                w.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(w);

                Intent c = new Intent(context, FloatingFoldersForGps.class);
                c.putExtra("categoryName", context.getString(R.string.remove_all_floatings));
                c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(c);

                PublicVariable.receiverGPS = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
