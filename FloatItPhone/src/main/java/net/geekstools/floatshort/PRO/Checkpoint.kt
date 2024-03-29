/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 8/29/20 3:58 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO

import android.Manifest
import android.app.Activity
import android.app.ActivityOptions
import android.app.AlertDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.view.WindowManager
import android.widget.Toast
import net.geekstools.floatshort.PRO.Utils.Functions.Debug.Companion.PrintDebug
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClassLegacy
import net.geekstools.floatshort.PRO.Utils.Functions.PreferencesIO
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable
import net.geekstools.floatshort.PRO.databinding.CheckPointBinding

class Checkpoint : Activity() {

    private val functionsClassLegacy: FunctionsClassLegacy by lazy {
        FunctionsClassLegacy(applicationContext)
    }

    private val preferencesIO: PreferencesIO by lazy {
        PreferencesIO(applicationContext)
    }

    companion object {
        private const val PermissionRequestCodeIntent = 666
    }

    private lateinit var checkPointBinding: CheckPointBinding

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        when (requestCode) {
            PermissionRequestCodeIntent -> {

                if (Build.VERSION.SDK_INT >= 26) {

                    if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                            && Settings.canDrawOverlays(applicationContext)) {

                        preferencesIO.savePreference(".Configuration", "Permissions", true)

                        startActivity(Intent(applicationContext, Configurations::class.java))

                        this@Checkpoint.finish()

                    }
                }

            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        checkPointBinding = CheckPointBinding.inflate(layoutInflater)
        setContentView(checkPointBinding.root)

        Toast.makeText(applicationContext,
                getString(R.string.wait),
                Toast.LENGTH_LONG).show()

        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)

        if (Build.VERSION.SDK_INT >= 26) {
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
                    || checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

                window.statusBarColor = PublicVariable.vibrantColor
                window.navigationBarColor = PublicVariable.vibrantColor

            } else {

                window.statusBarColor = Color.TRANSPARENT
                window.navigationBarColor = Color.TRANSPARENT

            }

        } else {

            window.statusBarColor = PublicVariable.vibrantColor
            window.navigationBarColor = PublicVariable.vibrantColor

        }

        if (intent.hasExtra(getString(R.string.splitIt))) {

            functionsClassLegacy.AccessibilityServiceDialogue(this@Checkpoint)

        } else {

            val permissionsList: ArrayList<String> = ArrayList<String>()
            permissionsList.add(Manifest.permission.INTERNET)
            permissionsList.add(Manifest.permission.CHANGE_WIFI_STATE)
            permissionsList.add(Manifest.permission.ACCESS_WIFI_STATE)
            permissionsList.add(Manifest.permission.ACCESS_NETWORK_STATE)
            permissionsList.add(Manifest.permission.WAKE_LOCK)
            permissionsList.add(Manifest.permission.BLUETOOTH)
            permissionsList.add(Manifest.permission.BLUETOOTH_ADMIN)
            permissionsList.add(Manifest.permission.RECEIVE_BOOT_COMPLETED)
            permissionsList.add(Manifest.permission.VIBRATE)

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {

                permissionsList.add(Manifest.permission.USE_BIOMETRIC)

            }

            if (functionsClassLegacy.returnAPI() >= Build.VERSION_CODES.O) {

                permissionsList.add(Manifest.permission.READ_EXTERNAL_STORAGE)
                permissionsList.add(Manifest.permission.WRITE_EXTERNAL_STORAGE)

            }

            requestPermissions(permissionsList.toTypedArray(), PermissionRequestCodeIntent)

            if (!Settings.canDrawOverlays(applicationContext)) {

                canOverlyPermission()

            }

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            PermissionRequestCodeIntent -> {

                if (Settings.canDrawOverlays(applicationContext)) {
                    PrintDebug("*** Overlay Permission Granted ***")
                    startActivity(Intent(applicationContext, Configurations::class.java),
                            ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
                }

            }
        }

    }

    private fun canOverlyPermission() {

        val alertDialog = AlertDialog.Builder(this, R.style.GeeksEmpire_Dialogue_Light)
        alertDialog.setTitle(Html.fromHtml("<font color='" + PublicVariable.vibrantColor.toString() + "'>" +
                resources.getString(R.string.permTitle) + "</font>"))
        alertDialog.setMessage(Html.fromHtml("<font color='" + PublicVariable.darkMutedColorString.toString() + "'>" +
                        resources.getString(R.string.permDesc) + "</font>"))
        alertDialog.setIcon(getDrawable(R.drawable.ic_launcher))
        alertDialog.setCancelable(true)

        alertDialog.setPositiveButton(getString(R.string.grant)) { dialog, which ->

            startService(Intent(applicationContext, BindServices::class.java))

            val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                    Uri.parse("package:$packageName"))
            startActivityForResult(intent, PermissionRequestCodeIntent)

            Toast.makeText(applicationContext, resources.getString(R.string.overlayPermission), Toast.LENGTH_LONG).show()

            dialog.dismiss()
        }

        alertDialog.setOnCancelListener {

            this@Checkpoint.finish()
        }

        alertDialog.setOnDismissListener { dialog ->

            dialog.dismiss()
        }

        alertDialog.show()

    }
}