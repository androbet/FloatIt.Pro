/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 8/26/20 4:58 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Utils.Functions

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager

class ApplicationsData(var context: Context) {

    fun appIsInstalled(packageName: String): Boolean {
        val packageManager: PackageManager = context.packageManager
        return try {
            packageManager.getPackageInfo(packageName, 0)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isSystemApplication(packageName: String): Boolean {
        val packageManager = context.packageManager

        return  try {
            val targetPkgInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_SIGNATURES)
            val sys = packageManager.getPackageInfo(
                    "android", PackageManager.GET_SIGNATURES)
            targetPkgInfo?.signatures != null && (sys.signatures[0] == targetPkgInfo.signatures[0])
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
            false
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    fun isDefaultLauncher(packageName: String): Boolean {
        val intent = Intent(Intent.ACTION_MAIN)
        intent.addCategory(Intent.CATEGORY_HOME)
        val defaultLauncher = context.packageManager.resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY)
        val defaultLauncherPackageName = defaultLauncher?.activityInfo?.packageName
        return (defaultLauncherPackageName == packageName)
    }

    fun canLaunch(packageName: String?): Boolean {
        return context.packageManager.getLaunchIntentForPackage(packageName!!) != null
    }
}