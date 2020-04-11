/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 3/28/20 12:48 PM
 * Last modified 3/28/20 10:35 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Utils.RemoteTask.Create;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.os.IBinder;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.firebase.appindexing.FirebaseAppIndex;

import net.geekstools.floatshort.PRO.BindServices;
import net.geekstools.floatshort.PRO.SecurityServices.Authentication.Utils.FunctionsClassSecurity;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClass;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClassDebug;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClassRunServices;
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable;
import net.geekstools.floatshort.PRO.Utils.UI.CustomIconManager.LoadCustomIcons;

public class RecoveryShortcuts extends Service {

    FunctionsClass functionsClass;
    FunctionsClassRunServices functionsClassRunServices;
    FunctionsClassSecurity functionsClassSecurity;

    String packageName;
    String[] appData;

    boolean runService = true;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        PublicVariable.size = functionsClass.readDefaultPreference("floatingSize", 39);
        PublicVariable.HW = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PublicVariable.size, this.getResources().getDisplayMetrics());

        if (getApplicationContext().getFileStreamPath(".uFile").exists()) {

            boolean authenticatedFloatIt = (intent.getBooleanExtra("AuthenticatedFloatIt", false));

            try {
                if (functionsClass.securityServicesSubscribed() && !FunctionsClassSecurity.AuthOpenAppValues.getAlreadyAuthenticating() && !authenticatedFloatIt) {
                    FunctionsClassSecurity.AuthOpenAppValues.setAuthComponentName(getPackageName());
                    FunctionsClassSecurity.AuthOpenAppValues.setAuthSecondComponentName(getPackageName());
                    FunctionsClassSecurity.AuthOpenAppValues.setAuthRecovery(true);

                    functionsClassSecurity.openAuthInvocation();
                    FunctionsClassSecurity.AuthOpenAppValues.setAlreadyAuthenticating(true);

                    IntentFilter intentFilter = new IntentFilter();
                    intentFilter.addAction("RECOVERY_AUTHENTICATED");
                    BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                        @Override
                        public void onReceive(Context context, Intent intent) {
                            if (intent.getAction().equals("RECOVERY_AUTHENTICATED")) {
                                appData = functionsClass.readFileLine(".uFile");
                                FirebaseAppIndex.getInstance().removeAll();

                                if (functionsClass.customIconsEnable()) {
                                    LoadCustomIcons loadCustomIcons = new LoadCustomIcons(getApplicationContext(), functionsClass.customIconPackageName());
                                    loadCustomIcons.load();
                                    FunctionsClassDebug.Companion.PrintDebug("*** Total Custom Icon ::: " + loadCustomIcons.getTotalIconsNumber());
                                }

                                for (String anAppData : appData) {
                                    runService = true;
                                    if (PublicVariable.FloatingShortcutsList != null) {
                                        for (int check = 0; check < PublicVariable.FloatingShortcutsList.size(); check++) {
                                            if (anAppData.equals(PublicVariable.FloatingShortcutsList.get(check))) {
                                                runService = false;
                                            }
                                        }
                                    }

                                    if (runService == true) {
                                        try {
                                            packageName = anAppData;
                                            functionsClassRunServices.runUnlimitedShortcutsServicePackage(packageName);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }

                                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                    stopForeground(Service.STOP_FOREGROUND_REMOVE);
                                    stopForeground(true);
                                }
                                stopSelf();
                            }
                        }
                    };
                    try {
                        registerReceiver(broadcastReceiver, intentFilter);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    appData = functionsClass.readFileLine(".uFile");
                    FirebaseAppIndex.getInstance().removeAll();

                    if (functionsClass.customIconsEnable()) {
                        LoadCustomIcons loadCustomIcons = new LoadCustomIcons(getApplicationContext(), functionsClass.customIconPackageName());
                        loadCustomIcons.load();
                        FunctionsClassDebug.Companion.PrintDebug("*** Total Custom Icon ::: " + loadCustomIcons.getTotalIconsNumber());
                    }

                    for (String anAppData : appData) {
                        runService = true;
                        if (PublicVariable.FloatingShortcutsList != null) {
                            for (int check = 0; check < PublicVariable.FloatingShortcutsList.size(); check++) {
                                if (anAppData.equals(PublicVariable.FloatingShortcutsList.get(check))) {
                                    runService = false;
                                }
                            }
                        }

                        if (runService == true) {
                            try {
                                packageName = anAppData;
                                functionsClassRunServices.runUnlimitedShortcutsServicePackage(packageName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }

                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        stopForeground(Service.STOP_FOREGROUND_REMOVE);
                        stopForeground(true);
                    }
                    stopSelf();
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (PublicVariable.allFloatingCounter == 0) {
                    if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getBoolean("stable", true) == false) {
                        stopService(new Intent(getApplicationContext(), BindServices.class));
                    }
                }
            }
        } else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                stopForeground(Service.STOP_FOREGROUND_REMOVE);
                stopForeground(true);
            }
            stopSelf();
        }
        if (PublicVariable.allFloatingCounter == 0) {
            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("stable", true) == false) {
                stopService(new Intent(getApplicationContext(), BindServices.class));
            }
        }

        return Service.START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        functionsClass = new FunctionsClass(getApplicationContext());
        functionsClassRunServices = new FunctionsClassRunServices(getApplicationContext());
        functionsClassSecurity = new FunctionsClassSecurity(getApplicationContext());

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(333, functionsClass.bindServiceNotification(),Service.STOP_FOREGROUND_REMOVE);
        } else {
            startForeground(333, functionsClass.bindServiceNotification());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(new Intent(getApplicationContext(), BindServices.class));
        } else {
            startService(new Intent(getApplicationContext(), BindServices.class));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            stopForeground(Service.STOP_FOREGROUND_REMOVE);
            stopForeground(true);
        }
        stopSelf();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
