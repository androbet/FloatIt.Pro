/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 11/11/20 10:43 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */
package net.geekstools.floatshort.PRO

import android.app.Service
import android.content.BroadcastReceiver
import android.content.Intent
import android.os.IBinder
import android.util.TypedValue
import net.geekstools.floatshort.PRO.Utils.Functions.Debug
import net.geekstools.floatshort.PRO.Utils.Functions.FileIO
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClassLegacy
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable

class BindServices : Service() {

    lateinit var functionsClassLegacy: FunctionsClassLegacy
    lateinit var fileIO: FileIO

    var broadcastReceiverAction: BroadcastReceiver? = null

    private companion object {
        var triggerWifiBroadcast = false
    }

    override fun onDestroy() {
        super.onDestroy()

        broadcastReceiverAction?.let {
            unregisterReceiver(it)
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Debug.PrintDebug("*** Bind Service StartId $startId ***")

        if (startId == 1) {
            startForeground(333, functionsClassLegacy.bindServiceNotification())

            PublicVariable.floatingSizeNumber = functionsClassLegacy.readDefaultPreference("floatingSize", 39)
            PublicVariable.floatingViewsHW = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, PublicVariable.floatingSizeNumber.toFloat(), applicationContext.resources.displayMetrics).toInt()

        }
        return START_STICKY
    }

    override fun onCreate() {
        super.onCreate()

        functionsClassLegacy = FunctionsClassLegacy(applicationContext)
        fileIO = FileIO(applicationContext)

        functionsClassLegacy.loadSavedColor()
        functionsClassLegacy.checkLightDarkTheme()
    }

    override fun onBind(intent: Intent?): IBinder? {

        return null
    }
}