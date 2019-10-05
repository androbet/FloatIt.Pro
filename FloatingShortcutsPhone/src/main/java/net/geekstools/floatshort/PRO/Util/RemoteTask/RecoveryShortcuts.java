package net.geekstools.floatshort.PRO.Util.RemoteTask;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.util.TypedValue;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.firebase.appindexing.FirebaseAppIndex;

import net.geekstools.floatshort.PRO.BindServices;
import net.geekstools.floatshort.PRO.Util.Functions.FunctionsClass;
import net.geekstools.floatshort.PRO.Util.Functions.FunctionsClassDebug;
import net.geekstools.floatshort.PRO.Util.Functions.FunctionsClassSecurity;
import net.geekstools.floatshort.PRO.Util.Functions.PublicVariable;
import net.geekstools.floatshort.PRO.Util.UI.CustomIconManager.LoadCustomIcons;

public class RecoveryShortcuts extends Service {

    FunctionsClass functionsClass;
    FunctionsClassSecurity functionsClassSecurity;

    String packageName;
    String[] appData;

    boolean runService = true;

    @Override
    public int onStartCommand(Intent intent, int flags, final int startId) {
        PublicVariable.size = functionsClass.readDefaultPreference("floatingSize", 39);
        PublicVariable.HW = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PublicVariable.size, this.getResources().getDisplayMetrics());

        if (getApplicationContext().getFileStreamPath(".uFile").exists()) {
            try {
                if (functionsClass.securityServicesSubscribed() && !FunctionsClassSecurity.AuthOpenAppValues.getAlreadyAuthenticating()) {
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

                                if (functionsClass.loadCustomIcons()) {
                                    LoadCustomIcons loadCustomIcons = new LoadCustomIcons(getApplicationContext(), functionsClass.customIconPackageName());
                                    loadCustomIcons.load();
                                    FunctionsClassDebug.Companion.PrintDebug("*** Total Custom Icon ::: " + loadCustomIcons.getTotalIcons());
                                }

                                for (String anAppData : appData) {
                                    runService = true;
                                    if (PublicVariable.FloatingShortcuts != null) {
                                        for (int check = 0; check < PublicVariable.FloatingShortcuts.size(); check++) {
                                            if (anAppData.equals(PublicVariable.FloatingShortcuts.get(check))) {
                                                runService = false;
                                            }
                                        }
                                    }

                                    if (runService == true) {
                                        try {
                                            packageName = anAppData;
                                            functionsClass.runUnlimitedShortcutsService(packageName);
                                        } catch (Exception e) {
                                            e.printStackTrace();
                                        }
                                    }
                                }
                            }
                        }
                    };
                    registerReceiver(broadcastReceiver, intentFilter);

                } else {
                    appData = functionsClass.readFileLine(".uFile");
                    FirebaseAppIndex.getInstance().removeAll();

                    if (functionsClass.loadCustomIcons()) {
                        LoadCustomIcons loadCustomIcons = new LoadCustomIcons(getApplicationContext(), functionsClass.customIconPackageName());
                        loadCustomIcons.load();
                        FunctionsClassDebug.Companion.PrintDebug("*** Total Custom Icon ::: " + loadCustomIcons.getTotalIcons());
                    }

                    for (String anAppData : appData) {
                        runService = true;
                        if (PublicVariable.FloatingShortcuts != null) {
                            for (int check = 0; check < PublicVariable.FloatingShortcuts.size(); check++) {
                                if (anAppData.equals(PublicVariable.FloatingShortcuts.get(check))) {
                                    runService = false;
                                }
                            }
                        }

                        if (runService == true) {
                            try {
                                packageName = anAppData;
                                functionsClass.runUnlimitedShortcutsService(packageName);
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (PublicVariable.floatingCounter == 0) {
                    if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                            .getBoolean("stable", true) == false) {
                        stopService(new Intent(getApplicationContext(), BindServices.class));
                    }
                }
            }
        }
        if (PublicVariable.floatingCounter == 0) {
            if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
                    .getBoolean("stable", true) == false) {
                stopService(new Intent(getApplicationContext(), BindServices.class));
            }
        }

        return functionsClass.serviceMode();
    }

    @Override
    public void onCreate() {
        super.onCreate();

        functionsClass = new FunctionsClass(getApplicationContext());
        functionsClassSecurity = new FunctionsClassSecurity(getApplicationContext());

        if (functionsClass.returnAPI() >= 26) {
            startForeground(333, functionsClass.bindServiceNotification());
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }
}
