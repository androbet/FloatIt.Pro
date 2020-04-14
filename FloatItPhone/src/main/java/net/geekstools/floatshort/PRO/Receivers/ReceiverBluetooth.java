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

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import net.geekstools.floatshort.PRO.Automation.RecoveryBluetooth;
import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFoldersForBluetooth;
import net.geekstools.floatshort.PRO.R;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForBluetooth;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClass;
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable;
import net.geekstools.floatshort.PRO.Utils.UI.CustomIconManager.LoadCustomIcons;

public class ReceiverBluetooth extends BroadcastReceiver {

    FunctionsClass functionsClass;

    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            functionsClass = new FunctionsClass(context);

            if (functionsClass.customIconsEnable()) {
                LoadCustomIcons loadCustomIcons = new LoadCustomIcons(context, functionsClass.customIconPackageName());
                loadCustomIcons.load();
            }

            final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
            if (bluetoothAdapter.isEnabled() == true
                    && PublicVariable.receiveBluetooth == false) {

                Intent bluetooth = new Intent(context, RecoveryBluetooth.class);
                bluetooth.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(bluetooth);

                PublicVariable.receiveBluetooth = true;

            } else if (bluetoothAdapter.isEnabled() == false) {
                Intent w = new Intent(context, FloatingShortcutsForBluetooth.class);
                w.putExtra(context.getString(R.string.remove_all_floatings), context.getString(R.string.remove_all_floatings));
                w.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(w);

                Intent c = new Intent(context, FloatingFoldersForBluetooth.class);
                c.putExtra(context.getString(R.string.remove_all_floatings), context.getString(R.string.remove_all_floatings));
                c.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startService(c);

                PublicVariable.receiveBluetooth = false;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
