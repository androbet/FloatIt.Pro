/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 1/3/20 12:15 AM
 * Last modified 1/3/20 12:14 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Util.Preferences

import android.app.Dialog
import android.content.SharedPreferences
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.Drawable
import android.text.Html
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.WindowManager
import android.widget.*
import androidx.fragment.app.FragmentActivity
import net.geekstools.floatshort.PRO.R
import net.geekstools.floatshort.PRO.Util.Functions.PublicVariable

fun setupShapes(activity: FragmentActivity, sharedPreferences: SharedPreferences) {
    val currentShape: Int = sharedPreferences.getInt("iconShape", 0)

    val layoutParams = WindowManager.LayoutParams()
    val dialogueWidth = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 377f, activity.resources.displayMetrics).toInt()
    val dialogueHeight = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 387f, activity.resources.displayMetrics).toInt()

    layoutParams.width = dialogueWidth
    layoutParams.height = dialogueHeight
    layoutParams.windowAnimations = android.R.style.Animation_Dialog
    layoutParams.flags = WindowManager.LayoutParams.FLAG_DIM_BEHIND
    layoutParams.dimAmount = 0.57f

    val dialog = Dialog(activity)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.setContentView(net.geekstools.floatshort.PRO.R.layout.icons_shapes_preferences)
    dialog.window!!.attributes = layoutParams
    dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    dialog.window!!.decorView.setBackgroundColor(Color.TRANSPARENT)
    dialog.setCancelable(true)

    val drawableTeardrop: Drawable = activity.applicationContext.getDrawable(R.drawable.droplet_icon)!!
    drawableTeardrop.setTint(PublicVariable.primaryColor)
    val drawableCircle: Drawable = activity.applicationContext.getDrawable(R.drawable.circle_icon)!!
    drawableCircle.setTint(PublicVariable.primaryColor)
    val drawableSquare: Drawable = activity.applicationContext.getDrawable(R.drawable.square_icon)!!
    drawableSquare.setTint(PublicVariable.primaryColor)
    val drawableSquircle: Drawable = activity.applicationContext.getDrawable(R.drawable.squircle_icon)!!
    drawableSquircle.setTint(PublicVariable.primaryColor)


    val dialogueTitle = dialog.findViewById<View>(R.id.dialogueTitle) as TextView
    dialogueTitle.text = Html.fromHtml("<font color='" + PublicVariable.colorLightDarkOpposite + "'>" + activity.applicationContext.getString(R.string.shapedDesc) + "</font>")
    dialogueTitle.setTextColor(PublicVariable.colorLightDarkOpposite)

    val dialogueView: View = dialog.findViewById<ScrollView>(R.id.dialogueView)
    dialogueView.backgroundTintList = ColorStateList.valueOf(PublicVariable.colorLightDark)

    val teardropShape = dialog.findViewById<RelativeLayout>(R.id.teardropShape)
    val circleShape = dialog.findViewById<RelativeLayout>(R.id.circleShape)
    val squareShape = dialog.findViewById<RelativeLayout>(R.id.squareShape)
    val squircleShape = dialog.findViewById<RelativeLayout>(R.id.squircleShape)

    val customIconPack = dialog.findViewById<Button>(R.id.customIconPack)
    val noShape = dialog.findViewById<Button>(R.id.noShape)

    val teardropImage = dialog.findViewById<ImageView>(R.id.teardropImage)
    val circleImage = dialog.findViewById<ImageView>(R.id.circleImage)
    val squareImage = dialog.findViewById<ImageView>(R.id.squareImage)
    val squircleImage = dialog.findViewById<ImageView>(R.id.squircleImage)

    val teardropText = dialog.findViewById<TextView>(R.id.teardropText)
    val circleText = dialog.findViewById<TextView>(R.id.circleText)
    val squareText = dialog.findViewById<TextView>(R.id.squareText)
    val squircleText = dialog.findViewById<TextView>(R.id.squircleText)

    teardropImage.setImageDrawable(drawableTeardrop)
    circleImage.setImageDrawable(drawableCircle)
    squareImage.setImageDrawable(drawableSquare)
    squircleImage.setImageDrawable(drawableSquircle)

    teardropText.setTextColor(PublicVariable.colorLightDarkOpposite)
    circleText.setTextColor(PublicVariable.colorLightDarkOpposite)
    squareText.setTextColor(PublicVariable.colorLightDarkOpposite)
    squircleText.setTextColor(PublicVariable.colorLightDarkOpposite)

    customIconPack.setTextColor(PublicVariable.colorLightDarkOpposite)
    noShape.setTextColor(PublicVariable.colorLightDarkOpposite)
}