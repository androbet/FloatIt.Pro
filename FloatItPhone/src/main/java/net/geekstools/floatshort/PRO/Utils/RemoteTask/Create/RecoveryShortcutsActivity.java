/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 4/29/20 6:37 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Utils.RemoteTask.Create;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import net.geekstools.floatshort.PRO.R;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClass;
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable;

public class RecoveryShortcutsActivity extends Activity {

    FunctionsClass functionsClass;

    Uri BASE_URL =
            Uri.parse("https://www.geeksempire.net/createshortcuts.html/");
    String packageName, setAppIndex, setAppIndexUrl;
    String[] appData;

    boolean runService = true;

    @Override
    protected void onCreate(Bundle Saved) {
        super.onCreate(Saved);

        try {
            if (getIntent().getAction().equals(Intent.ACTION_CREATE_SHORTCUT)) {
                FunctionsClass functionsClass = new FunctionsClass(getApplicationContext());

                Drawable shapeTempDrawable = functionsClass.shapesDrawables();
                if (shapeTempDrawable != null) {
                    shapeTempDrawable.setTint(PublicVariable.primaryColor);
                }
                LayerDrawable drawCategory = (LayerDrawable) getDrawable(R.drawable.draw_widget_shortcuts);
                drawCategory.setDrawableByLayerId(R.id.backgroundTemporary, shapeTempDrawable);
                final Bitmap shortcutApp = Bitmap
                        .createBitmap(drawCategory.getIntrinsicWidth(), drawCategory.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
                drawCategory.setBounds(0, 0, drawCategory.getIntrinsicWidth(), drawCategory.getIntrinsicHeight());
                drawCategory.draw(new Canvas(shortcutApp));

                Intent setCategoryRecovery = new Intent(getApplicationContext(), RecoveryShortcutsActivity.class);
                setCategoryRecovery.setAction("Remote_Recover_Shortcuts");
                setCategoryRecovery.addCategory(Intent.CATEGORY_DEFAULT);
                Intent intent = new Intent();
                intent.putExtra(Intent.EXTRA_SHORTCUT_INTENT, setCategoryRecovery);
                intent.putExtra(Intent.EXTRA_SHORTCUT_NAME, getString(R.string.recoveryShortcuts));
                intent.putExtra(Intent.EXTRA_SHORTCUT_ICON, shortcutApp);
                setResult(RESULT_OK, intent);
            } else if (getIntent().getAction().equals(Intent.ACTION_MAIN) || getIntent().getAction().equals(Intent.ACTION_VIEW)
                    || getIntent().getAction().equals("Remote_Recover_Shortcuts")) {

                Intent intent = new Intent(getApplicationContext(), RecoveryShortcuts.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(intent);
                } else {
                    startService(intent);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();

            Intent intent = new Intent(getApplicationContext(), RecoveryShortcuts.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            } else {
                startService(intent);
            }
        }
        finish();
    }
}
