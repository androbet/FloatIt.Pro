/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 3/28/20 12:48 PM
 * Last modified 3/28/20 12:38 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Utils.RemoteTask.Remove;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

import androidx.annotation.Nullable;

import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFolders;
import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFoldersForBluetooth;
import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFoldersForGps;
import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFoldersForNfc;
import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFoldersForTime;
import net.geekstools.floatshort.PRO.Folders.FloatingServices.FloatingFoldersForWifi;
import net.geekstools.floatshort.PRO.R;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForApplications;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForBluetooth;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForFrequentlyApplications;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForGps;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForHIS;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForNfc;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForTime;
import net.geekstools.floatshort.PRO.Shortcuts.FloatingServices.FloatingShortcutsForWifi;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClass;

public class RemoveAll extends Service {

    FunctionsClass functionsClass;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        /*Apps*/
        Intent App_Unlimited_Shortcuts = new Intent(getApplicationContext(), FloatingShortcutsForApplications.class);
        App_Unlimited_Shortcuts.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(App_Unlimited_Shortcuts);

        Intent App_Unlimited_Wifi = new Intent(getApplicationContext(), FloatingShortcutsForWifi.class);
        App_Unlimited_Wifi.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(App_Unlimited_Wifi);

        Intent App_Unlimited_Bluetooth = new Intent(getApplicationContext(), FloatingShortcutsForBluetooth.class);
        App_Unlimited_Bluetooth.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(App_Unlimited_Bluetooth);

        Intent App_Unlimited_Gps = new Intent(getApplicationContext(), FloatingShortcutsForGps.class);
        App_Unlimited_Gps.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(App_Unlimited_Gps);

        Intent App_Unlimited_Nfc = new Intent(getApplicationContext(), FloatingShortcutsForNfc.class);
        App_Unlimited_Nfc.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(App_Unlimited_Nfc);

        Intent App_Unlimited_Time = new Intent(getApplicationContext(), FloatingShortcutsForTime.class);
        App_Unlimited_Time.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(App_Unlimited_Time);

        /*Categories*/
        Intent Category_Unlimited_Category = new Intent(getApplicationContext(), FloatingFolders.class);
        Category_Unlimited_Category.putExtra("categoryName", getString(R.string.remove_all_floatings));
        startService(Category_Unlimited_Category);

        Intent Category_Unlimited_Wifi = new Intent(getApplicationContext(), FloatingFoldersForWifi.class);
        Category_Unlimited_Wifi.putExtra("categoryName", getString(R.string.remove_all_floatings));
        startService(Category_Unlimited_Wifi);

        Intent Category_Unlimited_Bluetooth = new Intent(getApplicationContext(), FloatingFoldersForBluetooth.class);
        Category_Unlimited_Bluetooth.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(Category_Unlimited_Bluetooth);

        Intent Category_Unlimited_Gps = new Intent(getApplicationContext(), FloatingFoldersForGps.class);
        Category_Unlimited_Gps.putExtra("categoryName", getString(R.string.remove_all_floatings));
        startService(Category_Unlimited_Gps);

        Intent Category_Unlimited_Nfc = new Intent(getApplicationContext(), FloatingFoldersForNfc.class);
        Category_Unlimited_Nfc.putExtra("categoryName", getString(R.string.remove_all_floatings));
        startService(Category_Unlimited_Nfc);

        Intent Category_Unlimited_Time = new Intent(getApplicationContext(), FloatingFoldersForTime.class);
        Category_Unlimited_Time.putExtra("categoryName", getString(R.string.remove_all_floatings));
        startService(Category_Unlimited_Time);

        /*HIS*/
        Intent App_Unlimited_HIS = new Intent(getApplicationContext(), FloatingShortcutsForHIS.class);
        App_Unlimited_HIS.putExtra("packageName", getString(R.string.remove_all_floatings));
        startService(App_Unlimited_HIS);

        /*Widgets*/
        Intent Widget_Unlimited_Floating = new Intent(getApplicationContext(), net.geekstools.floatshort.PRO.Widgets.FloatingServices.WidgetUnlimitedFloating.class);
        Widget_Unlimited_Floating.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(Widget_Unlimited_Floating);

        /*Frequently*/
        Intent App_Unlimited_Shortcuts_Frequently = new Intent(getApplicationContext(), FloatingShortcutsForFrequentlyApplications.class);
        App_Unlimited_Shortcuts_Frequently.putExtra(getString(R.string.remove_all_floatings), getString(R.string.remove_all_floatings));
        startService(App_Unlimited_Shortcuts_Frequently);

        stopSelf();
        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        functionsClass = new FunctionsClass(getApplicationContext());
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
