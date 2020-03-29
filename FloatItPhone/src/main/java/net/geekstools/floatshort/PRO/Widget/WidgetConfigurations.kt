/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel on 3/28/20 5:49 PM
 * Last modified 3/28/20 5:44 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Widget

import android.animation.Animator
import android.animation.ValueAnimator
import android.annotation.SuppressLint
import android.app.Activity
import android.app.ActivityOptions
import android.appwidget.AppWidgetHost
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProviderInfo
import android.content.*
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.*
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.Html
import android.text.TextWatcher
import android.util.DisplayMetrics
import android.view.*
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.view.animation.OvershootInterpolator
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.core.view.ViewCompat
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.geeksempire.primepuzzles.GameView.UI.SwipeGestureListener
import net.geekstools.floatshort.PRO.Automation.Apps.AppAutoFeatures
import net.geekstools.floatshort.PRO.Folders.FoldersConfigurations
import net.geekstools.floatshort.PRO.Preferences.PreferencesActivity
import net.geekstools.floatshort.PRO.R
import net.geekstools.floatshort.PRO.SearchEngine.SearchEngineAdapter
import net.geekstools.floatshort.PRO.SecurityServices.Authentication.PinPassword.HandlePinPassword
import net.geekstools.floatshort.PRO.Shortcuts.ApplicationsView
import net.geekstools.floatshort.PRO.Utils.AdapterItemsData.AdapterItems
import net.geekstools.floatshort.PRO.Utils.AdapterItemsData.AdapterItemsSearchEngine
import net.geekstools.floatshort.PRO.Utils.Functions.*
import net.geekstools.floatshort.PRO.Utils.GeneralAdapters.RecycleViewSmoothLayoutGrid
import net.geekstools.floatshort.PRO.Utils.IAP.InAppBilling
import net.geekstools.floatshort.PRO.Utils.IAP.billing.BillingManager
import net.geekstools.floatshort.PRO.Utils.RemoteTask.Create.RecoveryFolders
import net.geekstools.floatshort.PRO.Utils.RemoteTask.Create.RecoveryShortcuts
import net.geekstools.floatshort.PRO.Utils.UI.CustomIconManager.LoadCustomIcons
import net.geekstools.floatshort.PRO.Utils.UI.Gesture.GestureConstants
import net.geekstools.floatshort.PRO.Utils.UI.Gesture.GestureListenerConstants
import net.geekstools.floatshort.PRO.Utils.UI.Gesture.GestureListenerInterface
import net.geekstools.floatshort.PRO.Widget.RoomDatabase.WidgetDataInterface
import net.geekstools.floatshort.PRO.Widget.RoomDatabase.WidgetDataModel
import net.geekstools.floatshort.PRO.Widget.WidgetsAdapter.ConfiguredWidgetsAdapter
import net.geekstools.floatshort.PRO.Widget.WidgetsAdapter.InstalledWidgetsAdapter
import net.geekstools.floatshort.PRO.Widget.WidgetsAdapter.WidgetSectionedGridRecyclerViewAdapter
import net.geekstools.floatshort.PRO.databinding.WidgetConfigurationsViewsBinding
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.LinkedHashMap
import kotlin.math.hypot
import kotlin.math.roundToInt

class WidgetConfigurations : AppCompatActivity(), GestureListenerInterface {

    private val functionsClass: FunctionsClass by lazy {
        FunctionsClass(applicationContext)
    }
    private val functionsClassSecurity: FunctionsClassSecurity by lazy {
        FunctionsClassSecurity(applicationContext)
    }
    private val functionsClassRunServices: FunctionsClassRunServices by lazy {
        FunctionsClassRunServices(applicationContext)
    }

    /*Search Engine*/
    lateinit var searchAdapterItems: ArrayList<AdapterItemsSearchEngine>
    /*Search Engine*/

    private val mapIndexFirstItem: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
    private val mapIndexLastItem: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
    private val mapIndexFirstItemInstalled: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()
    private val mapIndexLastItemInstalled: LinkedHashMap<String, Int> = LinkedHashMap<String, Int>()

    private val mapRangeIndex: LinkedHashMap<Int, String> = LinkedHashMap<Int, String>()
    private val mapRangeIndexInstalled: LinkedHashMap<Int, String> = LinkedHashMap<Int, String>()

    private val indexItems: NavigableMap<String, Int> = TreeMap<String, Int>()
    private val indexItemsInstalled: NavigableMap<String, Int> = TreeMap<String, Int>()

    private val indexListConfigured: ArrayList<String> = ArrayList<String>()
    private val indexListInstalled: ArrayList<String> = ArrayList<String>()

    private val installedWidgetsSections: ArrayList<WidgetSectionedGridRecyclerViewAdapter.Section> = ArrayList<WidgetSectionedGridRecyclerViewAdapter.Section>()
    private val configuredWidgetsSections: ArrayList<WidgetSectionedGridRecyclerViewAdapter.Section> = ArrayList<WidgetSectionedGridRecyclerViewAdapter.Section>()

    private lateinit var installedWidgetsRecyclerViewAdapter: RecyclerView.Adapter<InstalledWidgetsAdapter.ViewHolder>
    private lateinit var configuredWidgetsRecyclerViewAdapter: RecyclerView.Adapter<ConfiguredWidgetsAdapter.ViewHolder>

    private lateinit var configuredWidgetsSectionedGridRecyclerViewAdapter: WidgetSectionedGridRecyclerViewAdapter
    private lateinit var widgetSectionedGridRecyclerViewAdapter: WidgetSectionedGridRecyclerViewAdapter

    private lateinit var installedWidgetsRecyclerViewLayoutManager: GridLayoutManager
    private lateinit var configuredWidgetsRecyclerViewLayoutManager: GridLayoutManager

    private lateinit var widgetProviderInfoList: ArrayList<AppWidgetProviderInfo>
    private val installedWidgetsAdapterItems: ArrayList<AdapterItems> = ArrayList<AdapterItems>()
    private val configuredWidgetsAdapterItems: ArrayList<AdapterItems> = ArrayList<AdapterItems>()

    private lateinit var appWidgetManager: AppWidgetManager
    private lateinit var appWidgetHost: AppWidgetHost

    private val loadCustomIcons: LoadCustomIcons by lazy {
        LoadCustomIcons(applicationContext, functionsClass.customIconPackageName())
    }

    private val swipeGestureListener: SwipeGestureListener by lazy {
        SwipeGestureListener(applicationContext, this@WidgetConfigurations)
    }

    private lateinit var firebaseRemoteConfig: FirebaseRemoteConfig

    private var installedWidgetsLoaded: Boolean = false
    private var configuredWidgetAvailable: Boolean = false

    private lateinit var firebaseAuth: FirebaseAuth

    companion object {
        var alreadyAuthenticatedWidgets = false
    }

    private lateinit var widgetConfigurationsViewsBinding: WidgetConfigurationsViewsBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        widgetConfigurationsViewsBinding = WidgetConfigurationsViewsBinding.inflate(layoutInflater)
        setContentView(widgetConfigurationsViewsBinding.root)

        functionsClass.loadSavedColor()
        functionsClass.checkLightDarkTheme()

        if (!functionsClass.readPreference("WidgetsInformation", "Reallocated", true)
                && getDatabasePath(PublicVariable.WIDGET_DATA_DATABASE_NAME).exists()) {
            startActivity(Intent(applicationContext, WidgetsReallocationProcess::class.java),
                    ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
            finish()
            return
        }

        widgetConfigurationsViewsBinding.widgetPickerTitle.text = Html.fromHtml(getString(net.geekstools.floatshort.PRO.R.string.widgetPickerTitle))
        widgetConfigurationsViewsBinding.widgetPickerTitle.setTextColor(if (PublicVariable.themeLightDark) getColor(R.color.dark) else getColor(net.geekstools.floatshort.PRO.R.color.light))

        installedWidgetsRecyclerViewLayoutManager = RecycleViewSmoothLayoutGrid(applicationContext, functionsClass.columnCount(190), OrientationHelper.VERTICAL, false)
        widgetConfigurationsViewsBinding.installedWidgetList.layoutManager = installedWidgetsRecyclerViewLayoutManager

        configuredWidgetsRecyclerViewLayoutManager = RecycleViewSmoothLayoutGrid(applicationContext, functionsClass.columnCount(190), OrientationHelper.VERTICAL, false)
        widgetConfigurationsViewsBinding.configuredWidgetList.layoutManager = configuredWidgetsRecyclerViewLayoutManager

        if (functionsClass.appThemeTransparent()) {
            functionsClass.setThemeColorFloating(this, widgetConfigurationsViewsBinding.wholeWidget, true)
        } else {
            functionsClass.setThemeColorFloating(this, widgetConfigurationsViewsBinding.wholeWidget, false)
        }

        appWidgetManager = AppWidgetManager.getInstance(applicationContext)
        appWidgetHost = AppWidgetHost(applicationContext, System.currentTimeMillis().toInt())

        if (getDatabasePath(PublicVariable.WIDGET_DATA_DATABASE_NAME).exists()) {

            loadConfiguredWidgets().invokeOnCompletion {

            }
        } else {
            widgetConfigurationsViewsBinding.reconfigure.visibility = View.INVISIBLE

            widgetConfigurationsViewsBinding.actionButton.bringToFront()
            widgetConfigurationsViewsBinding.addWidget.bringToFront()

            widgetConfigurationsViewsBinding.addWidget.animate().scaleXBy(0.23f).scaleYBy(0.23f).setDuration(223).setListener(scaleUpListener)

            if (functionsClass.appThemeTransparent()) {
                widgetConfigurationsViewsBinding.loadingSplash.setBackgroundColor(Color.TRANSPARENT)
            } else {
                widgetConfigurationsViewsBinding.loadingSplash.setBackgroundColor(window.navigationBarColor)
            }

            val typeface = Typeface.createFromAsset(assets, "upcil.ttf")
            widgetConfigurationsViewsBinding.loadingText.setTypeface(typeface)

            if (PublicVariable.themeLightDark) {
                widgetConfigurationsViewsBinding.loadingProgress.indeterminateDrawable.setTint(PublicVariable.darkMutedColor)
                widgetConfigurationsViewsBinding.loadingText.setTextColor(getColor(R.color.dark))
            } else if (!PublicVariable.themeLightDark) {
                widgetConfigurationsViewsBinding.loadingProgress.indeterminateDrawable.setTint(PublicVariable.vibrantColor)
                widgetConfigurationsViewsBinding.loadingText.setTextColor(getColor(R.color.light))
            }

            widgetConfigurationsViewsBinding.switchFloating.bringToFront()

            widgetConfigurationsViewsBinding.textInputSearchView.bringToFront()
            widgetConfigurationsViewsBinding.searchView.bringToFront()
            widgetConfigurationsViewsBinding.searchIcon.bringToFront()
            widgetConfigurationsViewsBinding.searchFloatIt.bringToFront()
            widgetConfigurationsViewsBinding.searchClose.bringToFront()
        }

        val drawAddWidget = getDrawable(R.drawable.draw_pref_add_widget) as LayerDrawable
        val backAddWidget = drawAddWidget.findDrawableByLayerId(R.id.backgroundTemporary)
        val frontAddWidget = drawAddWidget.findDrawableByLayerId(R.id.frontTemporary).mutate()
        backAddWidget.setTint(getColor(R.color.default_color_game))
        frontAddWidget.setTint(getColor(R.color.light))
        widgetConfigurationsViewsBinding.addWidget.setImageDrawable(drawAddWidget)

        val drawPreferenceAction = getDrawable(R.drawable.draw_pref_action) as LayerDrawable
        val backPreferenceAction = drawPreferenceAction.findDrawableByLayerId(R.id.backgroundTemporary)
        backPreferenceAction.setTint(PublicVariable.primaryColorOpposite)
        widgetConfigurationsViewsBinding.actionButton.setImageDrawable(drawPreferenceAction)

        widgetConfigurationsViewsBinding.switchApps.setTextColor(getColor(R.color.light))
        widgetConfigurationsViewsBinding.switchCategories.setTextColor(getColor(R.color.light))
        if (PublicVariable.themeLightDark /*light*/ && functionsClass.appThemeTransparent() /*transparent*/) {
            widgetConfigurationsViewsBinding.switchApps.setTextColor(getColor(R.color.dark))
            widgetConfigurationsViewsBinding.switchCategories.setTextColor(getColor(R.color.dark))
        }

        widgetConfigurationsViewsBinding.switchCategories.setBackgroundColor(if (functionsClass.appThemeTransparent()) functionsClass.setColorAlpha(PublicVariable.primaryColor, 51f) else PublicVariable.primaryColor)
        widgetConfigurationsViewsBinding.switchCategories.setRippleColor(ColorStateList.valueOf(if (functionsClass.appThemeTransparent()) functionsClass.setColorAlpha(PublicVariable.primaryColorOpposite, 51f) else PublicVariable.primaryColorOpposite))

        widgetConfigurationsViewsBinding.switchApps.setBackgroundColor(if (functionsClass.appThemeTransparent()) functionsClass.setColorAlpha(PublicVariable.primaryColor, 51f) else PublicVariable.primaryColor)
        widgetConfigurationsViewsBinding.switchApps.rippleColor = ColorStateList.valueOf(if (functionsClass.appThemeTransparent()) functionsClass.setColorAlpha(PublicVariable.primaryColorOpposite, 51f) else PublicVariable.primaryColorOpposite)

        widgetConfigurationsViewsBinding.recoveryAction.setBackgroundColor(PublicVariable.primaryColorOpposite)
        widgetConfigurationsViewsBinding.recoveryAction.rippleColor = ColorStateList.valueOf(PublicVariable.primaryColor)

        widgetConfigurationsViewsBinding.automationAction.setBackgroundColor(PublicVariable.primaryColorOpposite)
        widgetConfigurationsViewsBinding.automationAction.rippleColor = ColorStateList.valueOf(PublicVariable.primaryColor)

        val drawRecoverFloatingCategories = getDrawable(R.drawable.draw_recovery)?.mutate() as LayerDrawable?
        val backgroundRecoverFloatingCategories = drawRecoverFloatingCategories?.findDrawableByLayerId(R.id.backgroundTemporary)?.mutate()
        backgroundRecoverFloatingCategories?.setTint(if (functionsClass.appThemeTransparent()) functionsClass.setColorAlpha(PublicVariable.primaryColor, 51f) else PublicVariable.primaryColor)
        widgetConfigurationsViewsBinding.recoverFloatingCategories.setImageDrawable(drawRecoverFloatingCategories)
        widgetConfigurationsViewsBinding.recoverFloatingApps.setImageDrawable(drawRecoverFloatingCategories)

        widgetConfigurationsViewsBinding.actionButton.setOnClickListener {
            functionsClass.doVibrate(33)

            if (!PublicVariable.actionCenter) {
                if (widgetConfigurationsViewsBinding.installedNestedScrollView.isShown) {
                    widgetConfigurationsViewsBinding.installedNestedIndexScrollView.visibility = View.INVISIBLE

                    if (!configuredWidgetAvailable) {
                        widgetConfigurationsViewsBinding.addWidget.animate().scaleXBy(0.23f).scaleYBy(0.23f).setDuration(223).setListener(scaleUpListener)
                    }

                    ViewCompat.animate(widgetConfigurationsViewsBinding.addWidget)
                            .rotation(0.0f)
                            .withLayer()
                            .setDuration(300L)
                            .setInterpolator(OvershootInterpolator(3.0f))
                            .start()

                    val startRadius = 0
                    val endRadius = hypot(functionsClass.displayX().toDouble(), functionsClass.displayY().toDouble()).toInt()

                    val circularReveal = ViewAnimationUtils.createCircularReveal(widgetConfigurationsViewsBinding.installedNestedScrollView,
                            ((widgetConfigurationsViewsBinding.addWidget.x + widgetConfigurationsViewsBinding.addWidget.width / 2).roundToInt()),
                            ((widgetConfigurationsViewsBinding.addWidget.y + widgetConfigurationsViewsBinding.addWidget.height / 2).roundToInt()),
                            endRadius.toFloat(), startRadius.toFloat())
                    circularReveal.duration = 864
                    circularReveal.start()
                    circularReveal.addListener(object : Animator.AnimatorListener {

                        override fun onAnimationStart(animator: Animator) {

                        }

                        override fun onAnimationEnd(animator: Animator) {
                            widgetConfigurationsViewsBinding.installedNestedScrollView.visibility = View.INVISIBLE
                        }

                        override fun onAnimationCancel(animator: Animator) {

                        }

                        override fun onAnimationRepeat(animator: Animator) {

                        }
                    })

                    if (functionsClass.appThemeTransparent()) {
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        if (PublicVariable.themeLightDark) {
                            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            }
                        }
                        val valueAnimator = ValueAnimator
                                .ofArgb(window.navigationBarColor, functionsClass.setColorAlpha(functionsClass.mixColors(PublicVariable.primaryColor, PublicVariable.colorLightDark, 0.03f), 180f))
                        valueAnimator.addUpdateListener { animator ->
                            window.statusBarColor = (animator.animatedValue) as Int
                            window.navigationBarColor = (animator.animatedValue) as Int
                        }
                        valueAnimator.start()
                    } else {
                        window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                        if (PublicVariable.themeLightDark) {
                            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            }
                        }
                        val colorAnimation = ValueAnimator
                                .ofArgb(getWindow().navigationBarColor, PublicVariable.colorLightDark)
                        colorAnimation.addUpdateListener { animator ->
                            window.navigationBarColor = (animator.animatedValue) as Int
                            window.statusBarColor = (animator.animatedValue) as Int
                        }
                        colorAnimation.start()
                    }
                }

                val finalRadius = hypot(functionsClass.displayX().toDouble(), functionsClass.displayY().toDouble()).toInt()
                val circularReveal = ViewAnimationUtils.createCircularReveal(widgetConfigurationsViewsBinding.recoveryAction,
                        widgetConfigurationsViewsBinding.actionButton.x.roundToInt(),
                        widgetConfigurationsViewsBinding.actionButton.y.roundToInt(),
                        finalRadius.toFloat(), functionsClass.DpToInteger(13).toFloat())
                circularReveal.duration = 777
                circularReveal.interpolator = AccelerateInterpolator()
                circularReveal.start()
                circularReveal.addListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetConfigurationsViewsBinding.recoveryAction.setVisibility(View.INVISIBLE)
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })

                functionsClass.openActionMenuOption(this@WidgetConfigurations, widgetConfigurationsViewsBinding.fullActionViews,
                        widgetConfigurationsViewsBinding.actionButton,
                        widgetConfigurationsViewsBinding.fullActionViews.isShown)

            } else {
                widgetConfigurationsViewsBinding.recoveryAction.visibility = View.VISIBLE

                val finalRadius = hypot(functionsClass.displayX().toDouble(), functionsClass.displayY().toDouble()).toInt()
                val circularReveal = ViewAnimationUtils.createCircularReveal(widgetConfigurationsViewsBinding.recoveryAction,
                        widgetConfigurationsViewsBinding.actionButton.x.roundToInt(),
                        widgetConfigurationsViewsBinding.actionButton.y.roundToInt(),
                        functionsClass.DpToInteger(13).toFloat(), finalRadius.toFloat())
                circularReveal.duration = 1300
                circularReveal.interpolator = AccelerateInterpolator()
                circularReveal.start()
                circularReveal.addListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetConfigurationsViewsBinding.recoveryAction.visibility = View.VISIBLE
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })

                functionsClass.closeActionMenuOption(this@WidgetConfigurations, widgetConfigurationsViewsBinding.fullActionViews,
                        widgetConfigurationsViewsBinding.actionButton)
            }
        }
        widgetConfigurationsViewsBinding.switchCategories.setOnClickListener {

            functionsClass.navigateToClass(this@WidgetConfigurations, FoldersConfigurations::class.java,
                    ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_left, R.anim.slide_to_right))
        }
        widgetConfigurationsViewsBinding.switchApps.setOnClickListener {

            startActivity(Intent(applicationContext, ApplicationsView::class.java),
                    ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_right, R.anim.slide_to_left).toBundle())
        }
        widgetConfigurationsViewsBinding.automationAction.setOnClickListener {

            Intent(applicationContext, AppAutoFeatures::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startActivity(this,
                        ActivityOptions.makeCustomAnimation(applicationContext, R.anim.up_down, android.R.anim.fade_out).toBundle())
            }
        }
        widgetConfigurationsViewsBinding.recoveryAction.setOnClickListener {

            Intent(applicationContext, RecoveryFolders::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(this)
            }

            val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
            widgetConfigurationsViewsBinding.recoverFloatingCategories.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    widgetConfigurationsViewsBinding.recoverFloatingCategories.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
        widgetConfigurationsViewsBinding.recoverFloatingCategories.setOnClickListener {

            Intent(applicationContext, RecoveryFolders::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(this)
            }

            val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
            widgetConfigurationsViewsBinding.recoverFloatingCategories.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {

                }

                override fun onAnimationEnd(animation: Animation) {
                    widgetConfigurationsViewsBinding.recoverFloatingCategories.setVisibility(View.INVISIBLE)
                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }
        widgetConfigurationsViewsBinding.recoverFloatingApps.setOnClickListener {

            Intent(applicationContext, RecoveryShortcuts::class.java).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(this)
            }

            val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
            widgetConfigurationsViewsBinding.recoverFloatingApps.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationStart(animation: Animation) {}
                override fun onAnimationEnd(animation: Animation) {
                    widgetConfigurationsViewsBinding.recoverFloatingApps.visibility = View.INVISIBLE
                }

                override fun onAnimationRepeat(animation: Animation) {}
            })
        }

        widgetConfigurationsViewsBinding.actionButton.setOnLongClickListener {

            Intent().apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.setClass(this@WidgetConfigurations, PreferencesActivity::class.java)
                startActivity(this,
                        ActivityOptionsCompat.makeSceneTransitionAnimation(this@WidgetConfigurations, widgetConfigurationsViewsBinding.actionButton, "transition").toBundle())
            }

            true
        }
        widgetConfigurationsViewsBinding.switchCategories.setOnLongClickListener {

            if (!widgetConfigurationsViewsBinding.recoverFloatingCategories.isShown) {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_show)
                widgetConfigurationsViewsBinding.recoverFloatingCategories.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {

                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        widgetConfigurationsViewsBinding.recoverFloatingCategories.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            } else {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
                widgetConfigurationsViewsBinding.recoverFloatingCategories.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {

                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        widgetConfigurationsViewsBinding.recoverFloatingCategories.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            }

            true
        }
        widgetConfigurationsViewsBinding.switchApps.setOnLongClickListener {

            if (!widgetConfigurationsViewsBinding.recoverFloatingApps.isShown) {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_show)
                widgetConfigurationsViewsBinding.recoverFloatingApps.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {

                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        widgetConfigurationsViewsBinding.recoverFloatingApps.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            } else {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
                widgetConfigurationsViewsBinding.recoverFloatingApps.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {

                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        widgetConfigurationsViewsBinding.recoverFloatingApps.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            }

            true
        }

        val drawFloatingLogo = getDrawable(R.drawable.draw_floating_widgets) as LayerDrawable
        val backFloatingLogo = drawFloatingLogo.findDrawableByLayerId(R.id.backgroundTemporary)
        backFloatingLogo.setTint(PublicVariable.primaryColorOpposite)
        widgetConfigurationsViewsBinding.loadingLogo.setImageDrawable(drawFloatingLogo)

        firebaseAuth = FirebaseAuth.getInstance()
    }

    override fun onStart() {
        super.onStart()

        widgetConfigurationsViewsBinding.reconfigure.setOnClickListener {

            startActivity(Intent(applicationContext, WidgetsReallocationProcess::class.java),
                    ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())

            this@WidgetConfigurations.finish()
        }

        widgetConfigurationsViewsBinding.addWidget.setOnClickListener {
            functionsClass.doVibrate(77)

            if (widgetConfigurationsViewsBinding.installedNestedScrollView.isShown) {
                widgetConfigurationsViewsBinding.installedNestedScrollView.visibility = View.INVISIBLE
                widgetConfigurationsViewsBinding.installedNestedIndexScrollView.visibility = View.INVISIBLE

                if (!configuredWidgetAvailable) {
                    widgetConfigurationsViewsBinding.addWidget.animate().scaleXBy(0.23f).scaleYBy(0.23f).setDuration(223).setListener(scaleUpListener)
                }

                ViewCompat.animate(widgetConfigurationsViewsBinding.addWidget)
                        .rotation(0.0f)
                        .withLayer()
                        .setDuration(300L)
                        .setInterpolator(OvershootInterpolator(3.0f))
                        .start()
                val xPosition = (widgetConfigurationsViewsBinding.addWidget.x + widgetConfigurationsViewsBinding.addWidget.width / 2).roundToInt()
                val yPosition = (widgetConfigurationsViewsBinding.addWidget.y + widgetConfigurationsViewsBinding.addWidget.height / 2).roundToInt()
                val startRadius = 0
                val endRadius = hypot(functionsClass.displayX().toDouble(), functionsClass.displayY().toDouble()).toInt()
                val circularReveal = ViewAnimationUtils.createCircularReveal(widgetConfigurationsViewsBinding.installedNestedScrollView,
                        xPosition, yPosition,
                        endRadius.toFloat(), startRadius.toFloat())
                circularReveal.duration = 864
                circularReveal.start()
                circularReveal.addListener(object : Animator.AnimatorListener {

                    override fun onAnimationStart(animator: Animator) {

                    }

                    override fun onAnimationEnd(animator: Animator) {
                        widgetConfigurationsViewsBinding.installedNestedScrollView.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(animator: Animator) {

                    }

                    override fun onAnimationRepeat(animator: Animator) {

                    }
                })

                if (functionsClass.appThemeTransparent()) {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    if (PublicVariable.themeLightDark) {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        }
                    }
                    val valueAnimator = ValueAnimator
                            .ofArgb(window.navigationBarColor, functionsClass.setColorAlpha(functionsClass.mixColors(PublicVariable.primaryColor, PublicVariable.colorLightDark, 0.03f), 180f))
                    valueAnimator.addUpdateListener { animator ->
                        window.statusBarColor = (animator.animatedValue) as Int
                        window.navigationBarColor = (animator.animatedValue) as Int
                    }
                    valueAnimator.start()
                } else {
                    window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                    window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                    if (PublicVariable.themeLightDark) {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                        }
                    }
                    val colorAnimation = ValueAnimator
                            .ofArgb(window.navigationBarColor, PublicVariable.colorLightDark)
                    colorAnimation.addUpdateListener { animator ->
                        window.navigationBarColor = (animator.animatedValue) as Int
                        window.statusBarColor = (animator.animatedValue) as Int
                    }
                    colorAnimation.start()
                }
            } else {
                if (PublicVariable.actionCenter) {
                    widgetConfigurationsViewsBinding.recoveryAction.visibility = View.VISIBLE

                    val finalRadius = hypot(functionsClass.displayX().toDouble(), functionsClass.displayY().toDouble()).toInt()
                    val circularReveal = ViewAnimationUtils.createCircularReveal(widgetConfigurationsViewsBinding.recoveryAction,
                            widgetConfigurationsViewsBinding.actionButton.x.roundToInt(),
                            widgetConfigurationsViewsBinding.actionButton.y.roundToInt(),
                            functionsClass.DpToInteger(13).toFloat(), finalRadius.toFloat())
                    circularReveal.duration = 1300
                    circularReveal.interpolator = AccelerateInterpolator()
                    circularReveal.start()
                    circularReveal.addListener(object : Animator.AnimatorListener {

                        override fun onAnimationStart(animation: Animator) {

                        }

                        override fun onAnimationEnd(animation: Animator) {
                            widgetConfigurationsViewsBinding.recoveryAction.visibility = View.VISIBLE
                        }

                        override fun onAnimationCancel(animation: Animator) {

                        }

                        override fun onAnimationRepeat(animation: Animator) {

                        }
                    })
                    functionsClass.closeActionMenuOption(this@WidgetConfigurations, widgetConfigurationsViewsBinding.fullActionViews, widgetConfigurationsViewsBinding.actionButton)
                }

                loadInstalledWidgets()
            }
        }

        widgetConfigurationsViewsBinding.addWidget.setOnLongClickListener {

            val appWidgetId = appWidgetHost.allocateAppWidgetId()
            val appWidgetProviderInfos = ArrayList<AppWidgetProviderInfo>()
            val bundleArrayList = ArrayList<Bundle>()

            val pickIntent = Intent(AppWidgetManager.ACTION_APPWIDGET_PICK)
            pickIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
            pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_INFO, appWidgetProviderInfos)
            pickIntent.putParcelableArrayListExtra(AppWidgetManager.EXTRA_CUSTOM_EXTRAS, bundleArrayList)
            startActivityForResult(pickIntent, InstalledWidgetsAdapter.SYSTEM_WIDGET_PICKER)

            true
        }
    }

    override fun onResume() {
        super.onResume()

        firebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default)
        firebaseRemoteConfig.fetch(0)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        firebaseRemoteConfig.activate().addOnSuccessListener {
                            if (firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()) > functionsClass.appVersionCode(packageName)) {
                                functionsClass.notificationCreator(
                                        getString(R.string.updateAvailable),
                                        firebaseRemoteConfig.getString(functionsClass.upcomingChangeLogSummaryConfigKey()),
                                        firebaseRemoteConfig.getLong(functionsClass.versionCodeRemoteConfigKey()).toInt()
                                )
                            } else {

                            }
                        }
                    } else {

                    }
                }

        if (functionsClass.readPreference(".Password", "Pin", "0") == "0" && functionsClass.securityServicesSubscribed()) {
            startActivity(Intent(applicationContext, HandlePinPassword::class.java)
                    .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                    ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        } else {
            if (!WidgetConfigurations.alreadyAuthenticatedWidgets) {
                if (functionsClass.securityServicesSubscribed()) {
                    FunctionsClassSecurity.AuthOpenAppValues.authComponentName = getString(R.string.securityServices)
                    FunctionsClassSecurity.AuthOpenAppValues.authSecondComponentName = packageName
                    FunctionsClassSecurity.AuthOpenAppValues.authWidgetConfigurations = true

                    functionsClassSecurity.openAuthInvocation()
                }
            }
        }
    }

    override fun onPause() {
        super.onPause()

        if (PublicVariable.actionCenter) {
            functionsClass.closeActionMenuOption(this@WidgetConfigurations, widgetConfigurationsViewsBinding.fullActionViews,
                    widgetConfigurationsViewsBinding.actionButton)
        }
    }

    override fun onBackPressed() {
        if (widgetConfigurationsViewsBinding.installedNestedScrollView.isShown) {
            functionsClass.doVibrate(77)

            widgetConfigurationsViewsBinding.installedNestedScrollView.visibility = View.INVISIBLE
            if (!configuredWidgetAvailable) {
                widgetConfigurationsViewsBinding.addWidget.animate().scaleXBy(0.23f).scaleYBy(0.23f).setDuration(223).setListener(scaleUpListener)
            }

            ViewCompat.animate(widgetConfigurationsViewsBinding.addWidget)
                    .rotation(0.0f)
                    .withLayer()
                    .setDuration(300L)
                    .setInterpolator(OvershootInterpolator(3.0f))
                    .start()
            val startRadius = 0
            val endRadius = hypot(functionsClass.displayX().toDouble(), functionsClass.displayY().toDouble()).toInt()
            val circularReveal = ViewAnimationUtils.createCircularReveal(widgetConfigurationsViewsBinding.installedNestedScrollView,
                    (widgetConfigurationsViewsBinding.addWidget.x + widgetConfigurationsViewsBinding.addWidget.width / 2).roundToInt(),
                    (widgetConfigurationsViewsBinding.addWidget.y + widgetConfigurationsViewsBinding.addWidget.height / 2).roundToInt(),
                    endRadius.toFloat(), startRadius.toFloat())
            circularReveal.duration = 864
            circularReveal.start()
            circularReveal.addListener(object : Animator.AnimatorListener {

                override fun onAnimationStart(animator: Animator) {

                }

                override fun onAnimationEnd(animator: Animator) {
                    widgetConfigurationsViewsBinding.installedNestedScrollView.visibility = View.INVISIBLE
                }

                override fun onAnimationCancel(animator: Animator) {

                }

                override fun onAnimationRepeat(animator: Animator) {

                }
            })

            if (functionsClass.appThemeTransparent()) {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                if (PublicVariable.themeLightDark) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    }
                }

                val valueAnimator = ValueAnimator
                        .ofArgb(window.navigationBarColor, functionsClass.setColorAlpha(functionsClass.mixColors(PublicVariable.primaryColor, PublicVariable.colorLightDark, 0.03f), 180f))
                valueAnimator.addUpdateListener { animator ->
                    window.statusBarColor = (animator.animatedValue) as Int
                    window.navigationBarColor = (animator.animatedValue) as Int
                }
                valueAnimator.start()
            } else {
                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                if (PublicVariable.themeLightDark) {
                    window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                    }
                }
                val colorAnimation = ValueAnimator
                        .ofArgb(window.navigationBarColor, PublicVariable.colorLightDark)
                colorAnimation.addUpdateListener { animator ->
                    window.navigationBarColor = (animator.animatedValue) as Int
                    window.statusBarColor = (animator.animatedValue) as Int
                }
                colorAnimation.start()
            }
        } else {

            functionsClass.overrideBackPressToMain(this@WidgetConfigurations, this@WidgetConfigurations)
        }
    }

    override fun onSwipeGesture(gestureConstants: GestureConstants, downMotionEvent: MotionEvent, moveMotionEvent: MotionEvent, initVelocityX: Float, initVelocityY: Float) {
        super.onSwipeGesture(gestureConstants, downMotionEvent, moveMotionEvent, initVelocityX, initVelocityY)

        when (gestureConstants) {
            is GestureConstants.SwipeHorizontal -> {
                when (gestureConstants.horizontalDirection) {
                    GestureListenerConstants.SWIPE_RIGHT -> {

                        functionsClass.navigateToClass(this@WidgetConfigurations, FoldersConfigurations::class.java,
                                ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_left, R.anim.slide_to_right))
                    }
                    GestureListenerConstants.SWIPE_LEFT -> {

                        functionsClass.navigateToClass(this@WidgetConfigurations, ApplicationsView::class.java,
                                ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_right, R.anim.slide_to_left))
                    }
                }
            }
        }
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent): Boolean {
        swipeGestureListener.onTouchEvent(motionEvent)

        return super.dispatchTouchEvent(motionEvent)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (resultCode == Activity.RESULT_OK) {
            when (requestCode) {
                InstalledWidgetsAdapter.WIDGET_CONFIGURATION_REQUEST -> {

                    CoroutineScope(Dispatchers.IO).launch {

                        val dataExtras = data!!.extras
                        val appWidgetId = dataExtras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)

                        val widgetDataModel = WidgetDataModel(
                                System.currentTimeMillis(),
                                appWidgetId,
                                InstalledWidgetsAdapter.pickedWidgetPackageName,
                                InstalledWidgetsAdapter.pickedWidgetClassNameProvider,
                                InstalledWidgetsAdapter.pickedWidgetConfigClassName,
                                functionsClass.appName(InstalledWidgetsAdapter.pickedWidgetPackageName),
                                InstalledWidgetsAdapter.pickedWidgetLabel,
                                false
                        )

                        val widgetDataInterface = Room.databaseBuilder(applicationContext, WidgetDataInterface::class.java, PublicVariable.WIDGET_DATA_DATABASE_NAME)
                                .fallbackToDestructiveMigration()
                                .addCallback(object : RoomDatabase.Callback() {

                                    override fun onCreate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                        super.onCreate(supportSQLiteDatabase)
                                    }

                                    override fun onOpen(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                        super.onOpen(supportSQLiteDatabase)

                                        loadConfiguredWidgets()
                                    }
                                })
                                .build()

                        widgetDataInterface.initDataAccessObject().insertNewWidgetDataSuspend(widgetDataModel)
                        widgetDataInterface.close()
                    }
                }
                InstalledWidgetsAdapter.SYSTEM_WIDGET_PICKER -> {
                    val extras = data!!.extras
                    val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                    val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

                    if (appWidgetInfo.configure != null) {

                        Intent(AppWidgetManager.ACTION_APPWIDGET_CONFIGURE).apply {
                            this.component = appWidgetInfo.configure
                            this.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
                            startActivityForResult(this, InstalledWidgetsAdapter.SYSTEM_WIDGET_PICKER_CONFIGURATION)
                        }

                    } else {
                        CoroutineScope(Dispatchers.IO).launch {

                            val widgetDataModel = WidgetDataModel(
                                    System.currentTimeMillis(),
                                    appWidgetId,
                                    appWidgetInfo.provider.packageName,
                                    InstalledWidgetsAdapter.pickedWidgetClassNameProvider,
                                    InstalledWidgetsAdapter.pickedWidgetConfigClassName,
                                    functionsClass.appName(appWidgetInfo.provider.packageName),
                                    appWidgetInfo.loadLabel(packageManager),
                                    false
                            )

                            val widgetDataInterface = Room.databaseBuilder(applicationContext, WidgetDataInterface::class.java, PublicVariable.WIDGET_DATA_DATABASE_NAME)
                                    .fallbackToDestructiveMigration()
                                    .addCallback(object : RoomDatabase.Callback() {

                                        override fun onCreate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                            super.onCreate(supportSQLiteDatabase)
                                        }

                                        override fun onOpen(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                            super.onOpen(supportSQLiteDatabase)

                                            loadConfiguredWidgets()
                                        }
                                    })
                                    .build()

                            widgetDataInterface.initDataAccessObject().insertNewWidgetDataSuspend(widgetDataModel)
                            widgetDataInterface.close()
                        }
                    }
                }
                InstalledWidgetsAdapter.SYSTEM_WIDGET_PICKER_CONFIGURATION -> {

                    CoroutineScope(Dispatchers.IO).launch {

                        val extras = data!!.extras
                        val appWidgetId = extras!!.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, -1)
                        val appWidgetInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)

                        val widgetDataModel = WidgetDataModel(
                                System.currentTimeMillis(),
                                appWidgetId,
                                appWidgetInfo.provider.packageName,
                                InstalledWidgetsAdapter.pickedWidgetClassNameProvider,
                                InstalledWidgetsAdapter.pickedWidgetConfigClassName,
                                functionsClass.appName(appWidgetInfo.provider.packageName),
                                appWidgetInfo.loadLabel(packageManager),
                                false
                        )
                        val widgetDataInterface = Room.databaseBuilder(applicationContext, WidgetDataInterface::class.java, PublicVariable.WIDGET_DATA_DATABASE_NAME)
                                .fallbackToDestructiveMigration()
                                .addCallback(object : RoomDatabase.Callback() {

                                    override fun onCreate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                        super.onCreate(supportSQLiteDatabase)
                                    }

                                    override fun onOpen(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                        super.onOpen(supportSQLiteDatabase)

                                        loadConfiguredWidgets()
                                    }
                                })
                                .build()
                        widgetDataInterface.initDataAccessObject().insertNewWidgetDataSuspend(widgetDataModel)
                        widgetDataInterface.close()
                    }
                }
            }
        }
    }

    fun forceLoadConfiguredWidgets() {
        configuredWidgetsAdapterItems.clear()
        configuredWidgetsSections.clear()

        loadConfiguredWidgets()
    }

    fun loadConfiguredWidgets() = CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {

        configuredWidgetsAdapterItems.clear()
        configuredWidgetsSections.clear()
        widgetConfigurationsViewsBinding.configuredWidgetList.removeAllViews()

        if (functionsClass.appThemeTransparent()) {
            widgetConfigurationsViewsBinding.loadingSplash.setBackgroundColor(Color.TRANSPARENT)
        } else {
            widgetConfigurationsViewsBinding.loadingSplash.setBackgroundColor(window.navigationBarColor)
        }

        val typeface = Typeface.createFromAsset(assets, "upcil.ttf")
        widgetConfigurationsViewsBinding.loadingText.typeface = typeface

        if (PublicVariable.themeLightDark) {

            widgetConfigurationsViewsBinding.loadingProgress
                    .indeterminateDrawable.setTint(PublicVariable.darkMutedColor)
            widgetConfigurationsViewsBinding.loadingText.setTextColor(getColor(R.color.dark))

        } else if (!PublicVariable.themeLightDark) {

            widgetConfigurationsViewsBinding.loadingProgress
                    .indeterminateDrawable.setTint(PublicVariable.vibrantColor)
            widgetConfigurationsViewsBinding.loadingText.setTextColor(getColor(R.color.light))

        }

        configuredWidgetsAdapterItems.clear()
        configuredWidgetsSections.clear()

        configuredWidgetAvailable = false

        if (functionsClass.loadCustomIcons()) {
            loadCustomIcons.load()
            FunctionsClassDebug.PrintDebug("*** Total Custom Icon ::: " + loadCustomIcons.totalIconsNumber)
        }

        val widgetDataInterface = Room.databaseBuilder(applicationContext, WidgetDataInterface::class.java, PublicVariable.WIDGET_DATA_DATABASE_NAME)
                .fallbackToDestructiveMigration()
                .addCallback(object : RoomDatabase.Callback() {

                    override fun onCreate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                        super.onCreate(supportSQLiteDatabase)
                    }

                    override fun onOpen(supportSQLiteDatabase: SupportSQLiteDatabase) {
                        super.onOpen(supportSQLiteDatabase)
                    }
                })
                .build()

        val widgetDataModels: List<WidgetDataModel> = widgetDataInterface.initDataAccessObject().getAllWidgetDataSuspend()

        if (widgetDataModels.isNotEmpty()) {
            var oldAppName = ""
            var widgetIndex = 0

            widgetDataModels.asFlow()
                    .onCompletion {

                        loadWidgetsIndexConfigured().await()

                        loadSearchEngineData().await()

                        loadInstalledCustomIconPackages().await()
                    }
                    .withIndex().collect { widgetDataModel ->
                        val appWidgetId: Int = widgetDataModel.value.WidgetId
                        val packageName: String = widgetDataModel.value.PackageName
                        val className: String = widgetDataModel.value.ClassNameProvider
                        val configClassName: String? = widgetDataModel.value.ConfigClassName

                        FunctionsClassDebug.PrintDebug("*** $appWidgetId *** PackageName: $packageName - ClassName: $className - Configure: $configClassName ***")

                        if (functionsClass.appIsInstalled(packageName)) {
                            val appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                            val newAppName = functionsClass.appName(packageName)
                            val appIcon = if (functionsClass.loadCustomIcons()) loadCustomIcons.getDrawableIconForPackage(packageName, functionsClass.shapedAppIcon(packageName)) else functionsClass.shapedAppIcon(packageName)
                            if (widgetIndex == 0) {
                                configuredWidgetsSections.add(WidgetSectionedGridRecyclerViewAdapter.Section(widgetIndex, newAppName, appIcon))
                                indexListConfigured.add(newAppName.substring(0, 1).toUpperCase())
                            } else {
                                if (oldAppName != newAppName) {
                                    configuredWidgetsSections.add(WidgetSectionedGridRecyclerViewAdapter.Section(widgetIndex, newAppName, appIcon))
                                    indexListConfigured.add(newAppName.substring(0, 1).toUpperCase())
                                }
                            }
                            oldAppName = functionsClass.appName(packageName)
                            indexListConfigured.add(newAppName.substring(0, 1).toUpperCase())
                            configuredWidgetsAdapterItems.add(AdapterItems(
                                    newAppName,
                                    packageName,
                                    className,
                                    configClassName,
                                    widgetDataModel.value.WidgetLabel,
                                    appIcon,
                                    appWidgetProviderInfo,
                                    appWidgetId,
                                    widgetDataModel.value.Recovery ?: false,
                                    SearchEngineAdapter.SearchResultType.SearchWidgets
                            ))

                            widgetIndex++
                        } else {
                            widgetDataInterface.initDataAccessObject().deleteByWidgetClassNameProviderWidgetSuspend(packageName, className)
                        }
                    }

            configuredWidgetsRecyclerViewAdapter = ConfiguredWidgetsAdapter(this@WidgetConfigurations,
                    configuredWidgetsAdapterItems,
                    appWidgetManager, appWidgetHost)
        }

        configuredWidgetAvailable = configuredWidgetsAdapterItems.isNotEmpty()

        widgetDataInterface.close()

        if (configuredWidgetAvailable) {

            widgetConfigurationsViewsBinding.reconfigure.visibility = View.VISIBLE

            val sectionsData = arrayOfNulls<WidgetSectionedGridRecyclerViewAdapter.Section>(configuredWidgetsSections.size)
            configuredWidgetsSectionedGridRecyclerViewAdapter = WidgetSectionedGridRecyclerViewAdapter(
                    applicationContext,
                    R.layout.widgets_sections,
                    widgetConfigurationsViewsBinding.configuredWidgetList,
                    configuredWidgetsRecyclerViewAdapter
            )
            configuredWidgetsSectionedGridRecyclerViewAdapter.setSections(configuredWidgetsSections.toArray(sectionsData))
            configuredWidgetsSectionedGridRecyclerViewAdapter.notifyDataSetChanged()
            widgetConfigurationsViewsBinding.configuredWidgetList.adapter = configuredWidgetsSectionedGridRecyclerViewAdapter

            delay(333)

            widgetConfigurationsViewsBinding.configuredWidgetNestedScrollView.scrollTo(0, 0)

            val animation = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out)
            widgetConfigurationsViewsBinding.loadingSplash.visibility = View.INVISIBLE
            widgetConfigurationsViewsBinding.loadingSplash.startAnimation(animation)
        } else {

            widgetConfigurationsViewsBinding.reconfigure.visibility = View.INVISIBLE

            installedWidgetsLoaded = false
            widgetConfigurationsViewsBinding.addWidget.animate().scaleXBy(0.23f).scaleYBy(0.23f).setDuration(223).setListener(scaleUpListener)
            widgetConfigurationsViewsBinding.loadingSplash.visibility = View.VISIBLE

            configuredWidgetsRecyclerViewAdapter.notifyDataSetChanged()
            configuredWidgetsSectionedGridRecyclerViewAdapter.notifyDataSetChanged()
        }
    }

    fun loadInstalledWidgets() = CoroutineScope(SupervisorJob() + Dispatchers.Main).launch {
        widgetConfigurationsViewsBinding.loadingInstalledWidgets.setColor(PublicVariable.primaryColor)
        widgetConfigurationsViewsBinding.installedNestedScrollView.setBackgroundColor(if (PublicVariable.themeLightDark) getColor(R.color.transparent_light_high_twice) else getColor(R.color.transparent_dark_high_twice))

        widgetConfigurationsViewsBinding.loadingInstalledWidgets.visibility = View.VISIBLE

        installedWidgetsAdapterItems.clear()
        installedWidgetsSections.clear()

        widgetProviderInfoList = appWidgetManager.installedProviders as ArrayList<AppWidgetProviderInfo>
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {

            widgetProviderInfoList.sortWith(Comparator { appWidgetProviderInfoLeft, appWidgetProviderInfoRight ->

                functionsClass.appName(appWidgetProviderInfoLeft.provider.packageName)
                        .compareTo(functionsClass.appName(appWidgetProviderInfoRight.provider.packageName))
            })
        }

        if (functionsClass.loadCustomIcons()) {
            loadCustomIcons.load()
            FunctionsClassDebug.PrintDebug("*** Total Custom Icon ::: " + loadCustomIcons.totalIconsNumber)
        }

        var oldAppName = ""
        var widgetIndex = 0

        widgetProviderInfoList.asFlow()
                .onEach {
                    FunctionsClassDebug.PrintDebug("*** Provider = " + it.provider + " | Config = " + it.configure + " ***")
                }
                .onCompletion {

                    loadWidgetsIndexInstalled()
                }
                .withIndex().collect { appWidgetProviderInfo ->
                    val packageName: String = appWidgetProviderInfo.value.provider.packageName
                    val className: String = appWidgetProviderInfo.value.provider.className
                    val componentNameConfiguration: ComponentName? = appWidgetProviderInfo.value.configure

                    if (componentNameConfiguration != null) {
                        if (packageManager.getActivityInfo(componentNameConfiguration, PackageManager.GET_META_DATA).exported) {
                            if (packageName.isNotEmpty() && className.isNotEmpty()) {

                                val newAppName = functionsClass.appName(packageName)
                                val newAppIcon = if (functionsClass.loadCustomIcons()) loadCustomIcons.getDrawableIconForPackage(packageName, functionsClass.shapedAppIcon(packageName)) else functionsClass.shapedAppIcon(packageName)

                                if (widgetIndex == 0) {
                                    installedWidgetsSections.add(WidgetSectionedGridRecyclerViewAdapter.Section(widgetIndex, newAppName, newAppIcon))
                                    indexListInstalled.add(newAppName.substring(0, 1).toUpperCase(Locale.getDefault()))
                                } else {
                                    if (oldAppName != newAppName) {
                                        installedWidgetsSections.add(WidgetSectionedGridRecyclerViewAdapter.Section(widgetIndex, newAppName, newAppIcon))
                                        indexListInstalled.add(newAppName.substring(0, 1).toUpperCase(Locale.getDefault()))
                                    }
                                }

                                oldAppName = functionsClass.appName(appWidgetProviderInfo.value.provider.packageName)

                                val widgetPreviewDrawable: Drawable? = appWidgetProviderInfo.value.loadPreviewImage(applicationContext, DisplayMetrics.DENSITY_HIGH)
                                val widgetLabel: String? = appWidgetProviderInfo.value.loadLabel(packageManager)

                                indexListInstalled.add(newAppName.substring(0, 1).toUpperCase(Locale.getDefault()))
                                installedWidgetsAdapterItems.add(AdapterItems(functionsClass.appName(appWidgetProviderInfo.value.provider.packageName),
                                        appWidgetProviderInfo.value.provider.packageName,
                                        appWidgetProviderInfo.value.provider.className,
                                        componentNameConfiguration.className,
                                        widgetLabel ?: newAppName,
                                        newAppIcon,
                                        widgetPreviewDrawable
                                                ?: appWidgetProviderInfo.value.loadIcon(applicationContext, DisplayMetrics.DENSITY_HIGH),
                                        appWidgetProviderInfo.value
                                ))

                            }
                        }
                    } else {

                        if (packageName.isNotEmpty() && className.isNotEmpty()) {
                            val newAppName = functionsClass.appName(packageName)
                            val newAppIcon = if (functionsClass.loadCustomIcons()) loadCustomIcons.getDrawableIconForPackage(packageName, functionsClass.shapedAppIcon(packageName)) else functionsClass.shapedAppIcon(packageName)

                            if (widgetIndex == 0) {
                                installedWidgetsSections.add(WidgetSectionedGridRecyclerViewAdapter.Section(widgetIndex, newAppName, newAppIcon))
                                indexListInstalled.add(newAppName.substring(0, 1).toUpperCase(Locale.getDefault()))
                            } else {
                                if (oldAppName != newAppName) {
                                    installedWidgetsSections.add(WidgetSectionedGridRecyclerViewAdapter.Section(widgetIndex, newAppName, newAppIcon))
                                    indexListInstalled.add(newAppName.substring(0, 1).toUpperCase(Locale.getDefault()))
                                }
                            }

                            oldAppName = functionsClass.appName(appWidgetProviderInfo.value.provider.packageName)
                            val widgetPreviewDrawable: Drawable? = appWidgetProviderInfo.value.loadPreviewImage(applicationContext, DisplayMetrics.DENSITY_HIGH)
                            val widgetLabel: String? = appWidgetProviderInfo.value.loadLabel(packageManager)

                            indexListInstalled.add(newAppName.substring(0, 1).toUpperCase(Locale.getDefault()))
                            installedWidgetsAdapterItems.add(AdapterItems(functionsClass.appName(appWidgetProviderInfo.value.provider.packageName),
                                    appWidgetProviderInfo.value.provider.packageName,
                                    appWidgetProviderInfo.value.provider.className,
                                    null,
                                    widgetLabel ?: newAppName,
                                    newAppIcon,
                                    widgetPreviewDrawable
                                            ?: appWidgetProviderInfo.value.loadIcon(applicationContext, DisplayMetrics.DENSITY_HIGH),
                                    appWidgetProviderInfo.value
                            ))
                        }
                    }

                    widgetIndex++
                }

        installedWidgetsRecyclerViewAdapter = InstalledWidgetsAdapter(this@WidgetConfigurations, applicationContext, installedWidgetsAdapterItems, appWidgetHost)

        installedWidgetsLoaded = true

        val viewPropertyAnimator: ViewPropertyAnimator = widgetConfigurationsViewsBinding.addWidget.animate()
                .rotation(135.0f)
                .withLayer()
                .setDuration(500L)
                .setInterpolator(OvershootInterpolator(13.0f))
        viewPropertyAnimator.start()

        widgetConfigurationsViewsBinding.installedNestedScrollView.setBackgroundColor(if (PublicVariable.themeLightDark) getColor(R.color.transparent_light) else getColor(R.color.dark_transparent))
        widgetConfigurationsViewsBinding.installedNestedScrollView.visibility = View.VISIBLE

        installedWidgetsRecyclerViewAdapter.notifyDataSetChanged()
        val sectionsData = arrayOfNulls<WidgetSectionedGridRecyclerViewAdapter.Section>(installedWidgetsSections.size)
        widgetSectionedGridRecyclerViewAdapter = WidgetSectionedGridRecyclerViewAdapter(
                applicationContext,
                R.layout.widgets_sections,
                widgetConfigurationsViewsBinding.installedWidgetList,
                installedWidgetsRecyclerViewAdapter
        )
        widgetSectionedGridRecyclerViewAdapter.setSections(installedWidgetsSections.toArray(sectionsData))
        widgetSectionedGridRecyclerViewAdapter.notifyDataSetChanged()
        widgetConfigurationsViewsBinding.installedWidgetList.adapter = widgetSectionedGridRecyclerViewAdapter

        val xPosition = (widgetConfigurationsViewsBinding.addWidget.x + widgetConfigurationsViewsBinding.addWidget.width / 2).roundToInt()
        val yPosition = (widgetConfigurationsViewsBinding.addWidget.y + widgetConfigurationsViewsBinding.addWidget.height / 2).roundToInt()
        val startRadius = 0
        val endRadius = hypot(functionsClass.displayX().toDouble(), functionsClass.displayY().toDouble()).toInt()
        val circularReveal = ViewAnimationUtils.createCircularReveal(widgetConfigurationsViewsBinding.installedNestedScrollView,
                xPosition, yPosition,
                startRadius.toFloat(), endRadius.toFloat())
        circularReveal.duration = 864
        circularReveal.start()
        circularReveal.addListener(object : Animator.AnimatorListener {

            override fun onAnimationStart(animator: Animator) {

            }

            override fun onAnimationEnd(animator: Animator) {
                widgetConfigurationsViewsBinding.installedNestedScrollView.visibility = View.VISIBLE

                window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
                window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
                if (functionsClass.appThemeTransparent()) {
                    val colorAnimation = ValueAnimator
                            .ofArgb(window.navigationBarColor, if (PublicVariable.themeLightDark) getColor(R.color.fifty_light_twice) else getColor(R.color.transparent_dark_high_twice))
                    colorAnimation.addUpdateListener { animator ->
                        window.statusBarColor = (animator.animatedValue) as Int
                        window.navigationBarColor = (animator.animatedValue) as Int
                    }
                    colorAnimation.start()
                } else {
                    if (PublicVariable.themeLightDark) {
                        widgetConfigurationsViewsBinding.installedNestedScrollView.setBackground(ColorDrawable(getColor(R.color.transparent_light)))
                        if (PublicVariable.themeLightDark) {
                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                                window.decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR
                            }
                        }
                        val colorAnimation = ValueAnimator
                                .ofArgb(window.navigationBarColor, functionsClass.mixColors(getColor(R.color.light), getWindow().navigationBarColor, 0.70f))
                        colorAnimation.addUpdateListener { animator ->
                            window.navigationBarColor = (animator.animatedValue) as Int
                            window.statusBarColor = (animator.animatedValue) as Int
                        }
                        colorAnimation.start()
                    } else if (!PublicVariable.themeLightDark) {
                        widgetConfigurationsViewsBinding.installedNestedScrollView.setBackground(ColorDrawable(getColor(R.color.dark_transparent)))
                        val colorAnimation = ValueAnimator
                                .ofArgb(getWindow().navigationBarColor, functionsClass.mixColors(getColor(R.color.dark), getWindow().navigationBarColor, 0.70f))
                        colorAnimation.addUpdateListener { animator ->
                            window.navigationBarColor = (animator.animatedValue) as Int
                            window.statusBarColor = (animator.animatedValue) as Int
                        }
                        colorAnimation.start()
                    }
                }
            }

            override fun onAnimationCancel(animator: Animator) {

            }

            override fun onAnimationRepeat(animator: Animator) {

            }
        })

        if (!getDatabasePath(PublicVariable.WIDGET_DATA_DATABASE_NAME).exists() || !configuredWidgetAvailable) {
            delay(200)

            val animation = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out)
            widgetConfigurationsViewsBinding.loadingSplash.visibility = View.INVISIBLE
            widgetConfigurationsViewsBinding.loadingSplash.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation) {
                    (findViewById<View>(R.id.switchFloating) as LinearLayout).visibility = View.VISIBLE
                }

                override fun onAnimationEnd(animation: Animation) {

                }

                override fun onAnimationRepeat(animation: Animation) {

                }
            })
        }

        widgetConfigurationsViewsBinding.loadingInstalledWidgets.visibility = View.INVISIBLE
    }

    fun loadWidgetsIndexConfigured() = CoroutineScope(SupervisorJob() + Dispatchers.IO).async {
        withContext(Dispatchers.Main) {
            widgetConfigurationsViewsBinding.indexView.removeAllViews()
        }

        val indexCount = indexListConfigured.size
        for (navItem in 0 until indexCount) {
            val indexText = indexListConfigured[navItem]
            if (mapIndexFirstItem[indexText] == null /*avoid duplication*/) {
                mapIndexFirstItem[indexText] = navItem
            }

            mapIndexLastItem[indexText] = navItem
        }

        withContext(Dispatchers.Main) {
            var textView: TextView? = null

            val indexListFinal: List<String> = ArrayList(mapIndexFirstItem.keys)
            for (index in indexListFinal) {
                textView = layoutInflater.inflate(R.layout.side_index_item, null) as TextView
                textView.text = index.toUpperCase(Locale.getDefault())
                textView.setTextColor(PublicVariable.colorLightDarkOpposite)

                widgetConfigurationsViewsBinding.indexView.addView(textView)
            }

            val finalTextView = textView

            delay(700)

            finalTextView?.let {
                var upperRange = (widgetConfigurationsViewsBinding.indexView.y - it.height).roundToInt()

                for (i in 0 until widgetConfigurationsViewsBinding.indexView.childCount) {
                    val indexText = (widgetConfigurationsViewsBinding.indexView.getChildAt(i) as TextView).text.toString()
                    val indexRange = (widgetConfigurationsViewsBinding.indexView.getChildAt(i).y + widgetConfigurationsViewsBinding.indexView.y + it.height).roundToInt()

                    for (jRange in upperRange..indexRange) {
                        mapRangeIndex[jRange] = indexText
                    }

                    upperRange = indexRange
                }
            }

            setupFastScrollingIndexingConfigured()
        }
    }

    fun loadWidgetsIndexInstalled() = CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
        withContext(Dispatchers.Main) {
            widgetConfigurationsViewsBinding.indexViewInstalled.removeAllViews()
        }

        val indexCount = indexListInstalled.size
        for (navItem in 0 until indexCount) {
            val indexText = indexListInstalled[navItem]
            if (mapIndexFirstItemInstalled[indexText] == null /*avoid duplication*/) {
                mapIndexFirstItemInstalled[indexText] = navItem
            }

            mapIndexLastItemInstalled[indexText] = navItem
        }

        withContext(Dispatchers.Main) {
            var textView: TextView? = null

            val indexListFinal: List<String> = ArrayList(mapIndexFirstItemInstalled.keys)
            for (index in indexListFinal) {
                textView = layoutInflater.inflate(R.layout.side_index_item, null) as TextView
                textView.text = index.toUpperCase(Locale.getDefault())
                textView.setTextColor(PublicVariable.colorLightDarkOpposite)

                widgetConfigurationsViewsBinding.indexViewInstalled.addView(textView)
            }

            val finalTextView = textView

            delay(700)

            finalTextView?.let {
                var upperRange = (widgetConfigurationsViewsBinding.indexViewInstalled.y - it.height).roundToInt()

                for (i in 0 until widgetConfigurationsViewsBinding.indexViewInstalled.childCount) {
                    val indexText = (widgetConfigurationsViewsBinding.indexViewInstalled.getChildAt(i) as TextView).text.toString()
                    val indexRange = (widgetConfigurationsViewsBinding.indexViewInstalled.getChildAt(i).y + widgetConfigurationsViewsBinding.indexViewInstalled.y + it.height).roundToInt()

                    for (jRange in upperRange..indexRange) {
                        mapRangeIndexInstalled[jRange] = indexText
                    }

                    upperRange = indexRange
                }
            }

            setupFastScrollingIndexingInstalled()
        }
    }

    private fun loadInstalledCustomIconPackages() = CoroutineScope(SupervisorJob() + Dispatchers.IO).async {
        val packageManager = applicationContext.packageManager
        //ACTION: com.novalauncher.THEME
        //CATEGORY: com.novalauncher.category.CUSTOM_ICON_PICKER
        val intentCustomIcons = Intent()
        intentCustomIcons.action = "com.novalauncher.THEME"
        intentCustomIcons.addCategory("com.novalauncher.category.CUSTOM_ICON_PICKER")
        val resolveInfos = packageManager.queryIntentActivities(intentCustomIcons, 0)

        PublicVariable.customIconsPackages.clear()
        for (resolveInfo in resolveInfos) {
            FunctionsClassDebug.PrintDebug("CustomIconPackages ::: " + resolveInfo.activityInfo.packageName)
            PublicVariable.customIconsPackages.add(resolveInfo.activityInfo.packageName)
        }
    }

    fun createWidget(context: Context?, widgetView: ViewGroup, appWidgetManager: AppWidgetManager, appWidgetHost: AppWidgetHost, appWidgetProviderInfo: AppWidgetProviderInfo, widgetId: Int) {
        widgetView.removeAllViews()

        val functionsClass = FunctionsClass(context)

        appWidgetHost.startListening()

        val hostView = appWidgetHost.createView(context, widgetId, appWidgetProviderInfo)
        hostView.setAppWidget(widgetId, appWidgetProviderInfo)

        val widgetWidth = 213
        val widgetHeight = 213

        hostView.minimumWidth = widgetWidth
        hostView.minimumHeight = widgetHeight

        val bundle = Bundle()
        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH, widgetWidth)
        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MIN_HEIGHT, widgetHeight)
        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH, functionsClass.displayX() / 2)
        bundle.putInt(AppWidgetManager.OPTION_APPWIDGET_MAX_HEIGHT, functionsClass.displayY() / 2)
        appWidgetManager.bindAppWidgetIdIfAllowed(widgetId, appWidgetProviderInfo.provider, bundle)

        widgetView.addView(hostView)
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupFastScrollingIndexingConfigured() {
        val popupIndexBackground = getDrawable(R.drawable.ic_launcher_balloon)?.mutate()
        popupIndexBackground?.setTint(PublicVariable.primaryColorOpposite)
        widgetConfigurationsViewsBinding.popupIndex.background = popupIndexBackground

        widgetConfigurationsViewsBinding.nestedIndexScrollView.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
        widgetConfigurationsViewsBinding.nestedIndexScrollView.visibility = View.VISIBLE

        val popupIndexOffsetY = PublicVariable.statusBarHeight + PublicVariable.actionBarHeight + (if (functionsClass.UsageStatsEnabled()) functionsClass.DpToInteger(7) else functionsClass.DpToInteger(7)).toFloat()

        widgetConfigurationsViewsBinding.nestedIndexScrollView.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (functionsClass.litePreferencesEnabled()) {

                    } else {
                        val indexText = mapRangeIndex[motionEvent.y.toInt()]
                        if (indexText != null) {
                            widgetConfigurationsViewsBinding.popupIndex.y = motionEvent.rawY - popupIndexOffsetY
                            widgetConfigurationsViewsBinding.popupIndex.text = indexText
                            widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
                            widgetConfigurationsViewsBinding.popupIndex.visibility = View.VISIBLE
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (functionsClass.litePreferencesEnabled()) {
                    } else {
                        val indexText = mapRangeIndex[motionEvent.y.toInt()]
                        if (indexText != null) {
                            if (!widgetConfigurationsViewsBinding.popupIndex.isShown) {
                                widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
                                widgetConfigurationsViewsBinding.popupIndex.visibility = View.VISIBLE
                            }
                            widgetConfigurationsViewsBinding.popupIndex.y = motionEvent.rawY - popupIndexOffsetY
                            widgetConfigurationsViewsBinding.popupIndex.text = indexText

                            widgetConfigurationsViewsBinding.configuredWidgetNestedScrollView.smoothScrollTo(
                                    0,
                                    widgetConfigurationsViewsBinding.configuredWidgetList.getChildAt(mapIndexFirstItem[mapRangeIndex[motionEvent.y.toInt()]]
                                            ?: 0).y.roundToInt()
                            )
                        } else {
                            if (widgetConfigurationsViewsBinding.popupIndex.isShown) {
                                widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out))
                                widgetConfigurationsViewsBinding.popupIndex.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (functionsClass.litePreferencesEnabled()) {
                        widgetConfigurationsViewsBinding.configuredWidgetNestedScrollView.smoothScrollTo(
                                0,
                                widgetConfigurationsViewsBinding.configuredWidgetList.getChildAt(mapIndexFirstItem[mapRangeIndex[motionEvent.y.toInt()]]
                                        ?: 0).y.roundToInt()
                        )

                    } else {
                        if (widgetConfigurationsViewsBinding.popupIndex.isShown) {
                            widgetConfigurationsViewsBinding.configuredWidgetNestedScrollView.smoothScrollTo(
                                    0,
                                    widgetConfigurationsViewsBinding.configuredWidgetList.getChildAt(mapIndexFirstItem[mapRangeIndex[motionEvent.y.toInt()]]
                                            ?: 0).y.roundToInt()
                            )

                            widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out))
                            widgetConfigurationsViewsBinding.popupIndex.visibility = View.INVISIBLE
                        }
                    }
                }
            }

            true
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    fun setupFastScrollingIndexingInstalled() {
        val popupIndexBackground = getDrawable(R.drawable.ic_launcher_balloon)?.mutate()
        popupIndexBackground?.setTint(PublicVariable.primaryColorOpposite)
        widgetConfigurationsViewsBinding.popupIndex.background = popupIndexBackground

        widgetConfigurationsViewsBinding.installedNestedIndexScrollView.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
        widgetConfigurationsViewsBinding.installedNestedIndexScrollView.visibility = View.VISIBLE

        val popupIndexOffsetY = PublicVariable.statusBarHeight + PublicVariable.actionBarHeight + (if (functionsClass.UsageStatsEnabled()) functionsClass.DpToInteger(7) else functionsClass.DpToInteger(7)).toFloat()

        widgetConfigurationsViewsBinding.installedNestedIndexScrollView.setOnTouchListener { view, motionEvent ->
            when (motionEvent.action) {
                MotionEvent.ACTION_DOWN -> {
                    if (functionsClass.litePreferencesEnabled()) {

                    } else {
                        val indexText = mapRangeIndexInstalled[motionEvent.y.toInt()]
                        if (indexText != null) {
                            widgetConfigurationsViewsBinding.popupIndex.y = motionEvent.rawY - popupIndexOffsetY
                            widgetConfigurationsViewsBinding.popupIndex.text = indexText
                            widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
                            widgetConfigurationsViewsBinding.popupIndex.visibility = View.VISIBLE
                        }
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (functionsClass.litePreferencesEnabled()) {

                    } else {
                        val indexText = mapRangeIndexInstalled[motionEvent.y.toInt()]
                        if (indexText != null) {
                            if (!widgetConfigurationsViewsBinding.popupIndex.isShown) {
                                widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
                                widgetConfigurationsViewsBinding.popupIndex.visibility = View.VISIBLE
                            }

                            widgetConfigurationsViewsBinding.popupIndex.y = motionEvent.rawY - popupIndexOffsetY
                            widgetConfigurationsViewsBinding.popupIndex.text = indexText

                            widgetConfigurationsViewsBinding.installedNestedScrollView.smoothScrollTo(
                                    0,
                                    widgetConfigurationsViewsBinding.installedWidgetList.getChildAt(mapIndexFirstItemInstalled[mapRangeIndexInstalled[motionEvent.y.toInt()]]
                                            ?: 0).y.roundToInt()
                            )
                        } else {
                            if (widgetConfigurationsViewsBinding.popupIndex.isShown) {
                                widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out))
                                widgetConfigurationsViewsBinding.popupIndex.visibility = View.INVISIBLE
                            }
                        }
                    }
                }
                MotionEvent.ACTION_UP -> {
                    if (functionsClass.litePreferencesEnabled()) {
                        try {
                            widgetConfigurationsViewsBinding.installedNestedScrollView.smoothScrollTo(
                                    0,
                                    widgetConfigurationsViewsBinding.installedWidgetList.getChildAt(mapIndexFirstItemInstalled[mapRangeIndexInstalled[motionEvent.y.toInt()]]
                                            ?: 0).y.roundToInt()
                            )
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else {
                        if (widgetConfigurationsViewsBinding.popupIndex.isShown) {
                            widgetConfigurationsViewsBinding.installedNestedScrollView.smoothScrollTo(
                                    0,
                                    widgetConfigurationsViewsBinding.installedWidgetList.getChildAt(mapIndexFirstItemInstalled[mapRangeIndexInstalled[motionEvent.y.toInt()]]
                                            ?: 0).y.roundToInt()
                            )

                            widgetConfigurationsViewsBinding.popupIndex.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out))
                            widgetConfigurationsViewsBinding.popupIndex.visibility = View.INVISIBLE
                        }
                    }
                }
            }

            true
        }
    }

    val scaleDownListener: Animator.AnimatorListener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            if (!installedWidgetsLoaded) {
                widgetConfigurationsViewsBinding.addWidget.animate().scaleXBy(0.23f).scaleYBy(0.23f).setDuration(223).setListener(scaleUpListener)
            }
        }

        override fun onAnimationCancel(animation: Animator) {

        }
    }

    val scaleUpListener: Animator.AnimatorListener = object : Animator.AnimatorListener {

        override fun onAnimationStart(animation: Animator) {

        }

        override fun onAnimationRepeat(animation: Animator) {

        }

        override fun onAnimationEnd(animation: Animator) {
            widgetConfigurationsViewsBinding.addWidget.animate().scaleXBy(-0.23f).scaleYBy(-0.23f).setDuration(323).setListener(scaleDownListener)
        }

        override fun onAnimationCancel(animation: Animator) {

        }
    }

    /*Search Engine*/
    private fun loadSearchEngineData() = CoroutineScope(SupervisorJob() + Dispatchers.Default).async {
        if (SearchEngineAdapter.allSearchResultItems.isEmpty()) {
            searchAdapterItems = ArrayList<AdapterItemsSearchEngine>()

            //Loading Applications
            val applicationInfoList = packageManager.queryIntentActivities(Intent().apply {
                this.action = Intent.ACTION_MAIN
                this.addCategory(Intent.CATEGORY_LAUNCHER)
            }, PackageManager.GET_RESOLVED_FILTER)
            val applicationInfoListSorted = applicationInfoList.sortedWith(ResolveInfo.DisplayNameComparator(packageManager))

            applicationInfoListSorted.asFlow()
                    .filter {

                        (packageManager.getLaunchIntentForPackage(it.activityInfo.packageName) != null)
                    }
                    .map {

                        it
                    }
                    .collect {
                        try {
                            val installedPackageName = it.activityInfo.packageName
                            val installedClassName = it.activityInfo.name
                            val installedAppName = functionsClass.activityLabel(it.activityInfo)

                            val installedAppIcon = if (functionsClass.loadCustomIcons()) {
                                loadCustomIcons.getDrawableIconForPackage(installedPackageName, functionsClass.shapedAppIcon(it.activityInfo))
                            } else {
                                functionsClass.shapedAppIcon(it.activityInfo)
                            }

                            searchAdapterItems.add(AdapterItemsSearchEngine(installedAppName, installedPackageName, installedClassName, installedAppIcon, SearchEngineAdapter.SearchResultType.SearchShortcuts))
                        } catch (e: Exception) {
                            e.printStackTrace()
                        } finally {

                        }
                    }

            //Loading Folders
            try {
                getFileStreamPath(".categoryInfo").readLines().forEach {
                    try {
                        searchAdapterItems.add(AdapterItemsSearchEngine(it, functionsClass.readFileLine(it), SearchEngineAdapter.SearchResultType.SearchFolders))
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }

            //Loading Widgets
            if (getDatabasePath(PublicVariable.WIDGET_DATA_DATABASE_NAME).exists()) {
                val appWidgetManager = AppWidgetManager.getInstance(this@WidgetConfigurations)
                val widgetDataInterface = Room.databaseBuilder(applicationContext, WidgetDataInterface::class.java, PublicVariable.WIDGET_DATA_DATABASE_NAME)
                        .fallbackToDestructiveMigration()
                        .addCallback(object : RoomDatabase.Callback() {
                            override fun onCreate(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                super.onCreate(supportSQLiteDatabase)
                            }

                            override fun onOpen(supportSQLiteDatabase: SupportSQLiteDatabase) {
                                super.onOpen(supportSQLiteDatabase)
                            }
                        })
                        .build()
                val widgetDataModels = widgetDataInterface.initDataAccessObject().getAllWidgetDataSuspend()

                if (widgetDataModels.isNotEmpty()) {
                    widgetDataModels.forEach { widgetDataModel ->
                        try {
                            val appWidgetId: Int = widgetDataModel.WidgetId
                            val packageName: String = widgetDataModel.PackageName
                            val className: String = widgetDataModel.ClassNameProvider
                            val configClassName: String? = widgetDataModel.ConfigClassName

                            FunctionsClassDebug.PrintDebug("*** $appWidgetId *** PackageName: $packageName - ClassName: $className - Configure: $configClassName ***")

                            if (functionsClass.appIsInstalled(packageName)) {

                                val appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId)
                                val newAppName = functionsClass.appName(packageName)
                                val appIcon = if (functionsClass.loadCustomIcons()) loadCustomIcons.getDrawableIconForPackage(packageName, functionsClass.shapedAppIcon(packageName)) else functionsClass.shapedAppIcon(packageName)

                                searchAdapterItems.add(AdapterItemsSearchEngine(
                                        newAppName,
                                        packageName,
                                        className,
                                        configClassName,
                                        widgetDataModel.WidgetLabel,
                                        appIcon,
                                        appWidgetProviderInfo,
                                        appWidgetId,
                                        SearchEngineAdapter.SearchResultType.SearchWidgets
                                ))
                            } else {
                                widgetDataInterface.initDataAccessObject().deleteByWidgetClassNameProviderWidget(packageName, className)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }

            val searchRecyclerViewAdapter = SearchEngineAdapter(applicationContext, searchAdapterItems)

            withContext(Dispatchers.Main) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

                if (searchAdapterItems.size > 0) {
                    setupSearchView(searchRecyclerViewAdapter)
                }
            }
        } else {
            searchAdapterItems = SearchEngineAdapter.allSearchResultItems

            val searchRecyclerViewAdapter = SearchEngineAdapter(applicationContext, searchAdapterItems)

            withContext(Dispatchers.Main) {
                window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_HIDDEN)

                if (searchAdapterItems.size > 0) {
                    setupSearchView(searchRecyclerViewAdapter)
                }
            }
        }
    }

    private fun setupSearchView(searchRecyclerViewAdapter: SearchEngineAdapter) {
        widgetConfigurationsViewsBinding.searchView.setAdapter(searchRecyclerViewAdapter)

        widgetConfigurationsViewsBinding.searchView.setDropDownBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        widgetConfigurationsViewsBinding.searchView.isVerticalScrollBarEnabled = false
        widgetConfigurationsViewsBinding.searchView.scrollBarSize = 0

        widgetConfigurationsViewsBinding.searchView.setTextColor(PublicVariable.colorLightDarkOpposite)
        widgetConfigurationsViewsBinding.searchView.compoundDrawableTintList = ColorStateList.valueOf(functionsClass.setColorAlpha(PublicVariable.colorLightDarkOpposite, 175f))

        val layerDrawableSearchIcon = getDrawable(R.drawable.search_icon) as RippleDrawable?
        val backgroundTemporarySearchIcon = layerDrawableSearchIcon?.findDrawableByLayerId(R.id.backgroundTemporary)
        val frontTemporarySearchIcon = layerDrawableSearchIcon?.findDrawableByLayerId(R.id.frontTemporary)
        val frontDrawableSearchIcon = layerDrawableSearchIcon?.findDrawableByLayerId(R.id.frontDrawable)
        backgroundTemporarySearchIcon?.setTint(PublicVariable.colorLightDarkOpposite)
        frontTemporarySearchIcon?.setTint(PublicVariable.colorLightDark)
        frontDrawableSearchIcon?.setTint(if (PublicVariable.themeLightDark) functionsClass.manipulateColor(PublicVariable.primaryColor, 0.90f) else functionsClass.manipulateColor(PublicVariable.primaryColor, 3.00f))

        layerDrawableSearchIcon?.setLayerInset(2,
                functionsClass.DpToInteger(13), functionsClass.DpToInteger(13), functionsClass.DpToInteger(13), functionsClass.DpToInteger(13))

        widgetConfigurationsViewsBinding.searchIcon.setImageDrawable(layerDrawableSearchIcon)
        widgetConfigurationsViewsBinding.searchIcon.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
        widgetConfigurationsViewsBinding.searchIcon.visibility = View.VISIBLE

        widgetConfigurationsViewsBinding.textInputSearchView.hintTextColor = ColorStateList.valueOf(PublicVariable.primaryColorOpposite)
        widgetConfigurationsViewsBinding.textInputSearchView.boxStrokeColor = PublicVariable.primaryColor

        var backgroundTemporaryInput = GradientDrawable()
        try {
            val layerDrawableBackgroundInput = getDrawable(R.drawable.background_search_input) as LayerDrawable?
            backgroundTemporaryInput = layerDrawableBackgroundInput!!.findDrawableByLayerId(R.id.backgroundTemporary) as GradientDrawable
            backgroundTemporaryInput.setTint(PublicVariable.colorLightDark)
            widgetConfigurationsViewsBinding.textInputSearchView.background = layerDrawableBackgroundInput
        } catch (e: Exception) {
            e.printStackTrace()
            widgetConfigurationsViewsBinding.textInputSearchView.background = null
        }

        val finalBackgroundTemporaryInput = backgroundTemporaryInput
        widgetConfigurationsViewsBinding.searchIcon.setOnClickListener {
            val bundleSearchEngineUsed = Bundle()
            bundleSearchEngineUsed.putParcelable("USER_USED_SEARCH_ENGINE", firebaseAuth.currentUser)
            bundleSearchEngineUsed.putInt("TYPE_USED_SEARCH_ENGINE", SearchEngineAdapter.SearchResultType.SearchFolders)

            val firebaseAnalytics = FirebaseAnalytics.getInstance(applicationContext)
            firebaseAnalytics.logEvent(SearchEngineAdapter.SEARCH_ENGINE_USED_LOG, bundleSearchEngineUsed)

            if (functionsClass.securityServicesSubscribed()) {
                if (functionsClass.readPreference(".Password", "Pin", "0") == "0" && functionsClass.securityServicesSubscribed()) {
                    startActivity(Intent(applicationContext, HandlePinPassword::class.java).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                            ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
                } else {
                    if (!SearchEngineAdapter.alreadyAuthenticatedSearchEngine) {
                        if (functionsClass.securityServicesSubscribed()) {
                            FunctionsClassSecurity.AuthOpenAppValues.authComponentName = getString(R.string.securityServices)
                            FunctionsClassSecurity.AuthOpenAppValues.authSecondComponentName = packageName
                            FunctionsClassSecurity.AuthOpenAppValues.authSearchEngine = true

                            functionsClassSecurity.openAuthInvocation()

                            val intentFilter = IntentFilter()
                            intentFilter.addAction("SEARCH_ENGINE_AUTHENTICATED")
                            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                                override fun onReceive(context: Context, intent: Intent) {
                                    if (intent.action == "SEARCH_ENGINE_AUTHENTICATED") {
                                        performSearchEngine(finalBackgroundTemporaryInput)
                                    }
                                }
                            }
                            try {
                                registerReceiver(broadcastReceiver, intentFilter)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                        }
                    } else {
                        performSearchEngine(finalBackgroundTemporaryInput)
                    }
                }
            } else {
                performSearchEngine(finalBackgroundTemporaryInput)
            }
        }
    }

    private fun performSearchEngine(finalBackgroundTemporaryInput: GradientDrawable) = CoroutineScope(Dispatchers.Main).launch {
        delay(90)

        if (functionsClass.searchEngineSubscribed()) {
            widgetConfigurationsViewsBinding.textInputSearchView.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
            widgetConfigurationsViewsBinding.textInputSearchView.visibility = View.VISIBLE

            widgetConfigurationsViewsBinding.searchIcon.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out))
            widgetConfigurationsViewsBinding.searchIcon.visibility = View.INVISIBLE

            val valueAnimatorCornerDown = ValueAnimator.ofInt(functionsClass.DpToInteger(51), functionsClass.DpToInteger(7))
            valueAnimatorCornerDown.duration = 777
            valueAnimatorCornerDown.addUpdateListener { animator ->
                val animatorValue = animator.animatedValue as Int

                widgetConfigurationsViewsBinding.textInputSearchView.setBoxCornerRadii(animatorValue.toFloat(), animatorValue.toFloat(), animatorValue.toFloat(), animatorValue.toFloat())
                finalBackgroundTemporaryInput.cornerRadius = animatorValue.toFloat()
            }
            valueAnimatorCornerDown.start()

            val valueAnimatorScalesUp = ValueAnimator.ofInt(functionsClass.DpToInteger(51), widgetConfigurationsViewsBinding.switchApps.width)
            valueAnimatorScalesUp.duration = 777
            valueAnimatorScalesUp.addUpdateListener { animator ->
                val animatorValue = animator.animatedValue as Int
                widgetConfigurationsViewsBinding.textInputSearchView.layoutParams.width = animatorValue
                widgetConfigurationsViewsBinding.textInputSearchView.requestLayout()
            }
            valueAnimatorScalesUp.addListener(object : Animator.AnimatorListener {
                override fun onAnimationStart(animation: Animator) {

                }

                override fun onAnimationEnd(animation: Animator) {
                    widgetConfigurationsViewsBinding.searchView.requestFocus()

                    val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                    inputMethodManager.showSoftInput(widgetConfigurationsViewsBinding.searchView, InputMethodManager.SHOW_IMPLICIT)

                    widgetConfigurationsViewsBinding.searchFloatIt.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_bounce_interpolator))
                    widgetConfigurationsViewsBinding.searchFloatIt.visibility = View.VISIBLE

                    widgetConfigurationsViewsBinding.searchClose.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_up_bounce_interpolator))
                    widgetConfigurationsViewsBinding.searchClose.visibility = View.VISIBLE
                }

                override fun onAnimationCancel(animation: Animator) {

                }

                override fun onAnimationRepeat(animation: Animator) {

                }
            })
            valueAnimatorScalesUp.start()

            widgetConfigurationsViewsBinding.searchFloatIt.setOnClickListener {
                if (!widgetConfigurationsViewsBinding.searchView.text.toString().isEmpty() && SearchEngineAdapter.allSearchResultItems.size > 0
                        && widgetConfigurationsViewsBinding.searchView.text.toString().length >= 2) {
                    SearchEngineAdapter.allSearchResultItems.forEach { searchResultItem ->
                        when (searchResultItem.searchResultType) {
                            SearchEngineAdapter.SearchResultType.SearchShortcuts -> {
                                functionsClassRunServices.runUnlimitedShortcutsService(searchResultItem.PackageName!!, searchResultItem.ClassName!!)
                            }
                            SearchEngineAdapter.SearchResultType.SearchFolders -> {
                                functionsClass.runUnlimitedFolderService(searchResultItem.folderName)
                            }
                            SearchEngineAdapter.SearchResultType.SearchWidgets -> {
                                functionsClass
                                        .runUnlimitedWidgetService(searchResultItem.appWidgetId!!,
                                                searchResultItem.widgetLabel)
                            }
                        }
                    }
                }
            }

            widgetConfigurationsViewsBinding.searchView.addTextChangedListener(object : TextWatcher {
                override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {

                }

                override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

                }

                override fun afterTextChanged(s: Editable) {

                }
            })

            widgetConfigurationsViewsBinding.searchView.setOnEditorActionListener(object : TextView.OnEditorActionListener {
                override fun onEditorAction(textView: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                    if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                        if (SearchEngineAdapter.allSearchResultItems.size == 1
                                && !widgetConfigurationsViewsBinding.searchView.text.toString().isEmpty()
                                && widgetConfigurationsViewsBinding.searchView.text.toString().length >= 2) {
                            when (SearchEngineAdapter.allSearchResultItems[0].searchResultType) {
                                SearchEngineAdapter.SearchResultType.SearchShortcuts -> {
                                    functionsClassRunServices.runUnlimitedShortcutsService(SearchEngineAdapter.allSearchResultItems[0].PackageName!!, SearchEngineAdapter.allSearchResultItems[0].ClassName!!)
                                }
                                SearchEngineAdapter.SearchResultType.SearchFolders -> {
                                    functionsClass.runUnlimitedFolderService(SearchEngineAdapter.allSearchResultItems[0].folderName)
                                }
                                SearchEngineAdapter.SearchResultType.SearchWidgets -> {
                                    functionsClass
                                            .runUnlimitedWidgetService(SearchEngineAdapter.allSearchResultItems[0].appWidgetId!!,
                                                    SearchEngineAdapter.allSearchResultItems[0].widgetLabel)
                                }
                            }

                            widgetConfigurationsViewsBinding.searchView.setText("")

                            val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                            inputMethodManager.hideSoftInputFromWindow(widgetConfigurationsViewsBinding.searchView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

                            val valueAnimatorCornerUp = ValueAnimator.ofInt(functionsClass.DpToInteger(7), functionsClass.DpToInteger(51))
                            valueAnimatorCornerUp.duration = 777
                            valueAnimatorCornerUp.addUpdateListener { animator ->
                                val animatorValue = animator.animatedValue as Int
                                widgetConfigurationsViewsBinding.textInputSearchView.setBoxCornerRadii(animatorValue.toFloat(), animatorValue.toFloat(), animatorValue.toFloat(), animatorValue.toFloat())
                                finalBackgroundTemporaryInput.cornerRadius = animatorValue.toFloat()
                            }
                            valueAnimatorCornerUp.start()

                            val valueAnimatorScales = ValueAnimator.ofInt(widgetConfigurationsViewsBinding.textInputSearchView.width, functionsClass.DpToInteger(51))
                            valueAnimatorScales.duration = 777
                            valueAnimatorScales.addUpdateListener { animator ->
                                val animatorValue = animator.animatedValue as Int
                                widgetConfigurationsViewsBinding.textInputSearchView.layoutParams.width = animatorValue
                                widgetConfigurationsViewsBinding.textInputSearchView.requestLayout()
                            }
                            valueAnimatorScales.addListener(object : Animator.AnimatorListener {
                                override fun onAnimationStart(animation: Animator) {

                                }

                                override fun onAnimationEnd(animation: Animator) {
                                    widgetConfigurationsViewsBinding.textInputSearchView.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out))
                                    widgetConfigurationsViewsBinding.textInputSearchView.visibility = View.INVISIBLE

                                    widgetConfigurationsViewsBinding.searchIcon.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
                                    widgetConfigurationsViewsBinding.searchIcon.visibility = View.VISIBLE

                                    widgetConfigurationsViewsBinding.searchFloatIt.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_down_zero))
                                    widgetConfigurationsViewsBinding.searchFloatIt.visibility = View.INVISIBLE

                                    widgetConfigurationsViewsBinding.searchClose.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_down_zero))
                                    widgetConfigurationsViewsBinding.searchClose.visibility = View.INVISIBLE
                                }

                                override fun onAnimationCancel(animation: Animator) {

                                }

                                override fun onAnimationRepeat(animation: Animator) {

                                }
                            })
                            valueAnimatorScales.start()
                        } else {
                            if (SearchEngineAdapter.allSearchResultItems.size > 0
                                    && widgetConfigurationsViewsBinding.searchView.text.toString().length >= 2) {
                                widgetConfigurationsViewsBinding.searchView.showDropDown()
                            }
                        }
                    }

                    return false
                }
            })

            widgetConfigurationsViewsBinding.searchClose.setOnClickListener {
                widgetConfigurationsViewsBinding.searchView.setText("")

                val inputMethodManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputMethodManager.hideSoftInputFromWindow(widgetConfigurationsViewsBinding.searchView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)

                val valueAnimatorCornerUp = ValueAnimator.ofInt(functionsClass.DpToInteger(7), functionsClass.DpToInteger(51))
                valueAnimatorCornerUp.duration = 777
                valueAnimatorCornerUp.addUpdateListener { animator ->
                    val animatorValue = animator.animatedValue as Int
                    widgetConfigurationsViewsBinding.textInputSearchView.setBoxCornerRadii(animatorValue.toFloat(), animatorValue.toFloat(), animatorValue.toFloat(), animatorValue.toFloat())
                    finalBackgroundTemporaryInput.cornerRadius = animatorValue.toFloat()
                }
                valueAnimatorCornerUp.start()

                val valueAnimatorScales = ValueAnimator.ofInt(widgetConfigurationsViewsBinding.textInputSearchView.width, functionsClass.DpToInteger(51))
                valueAnimatorScales.duration = 777
                valueAnimatorScales.addUpdateListener { animator ->
                    val animatorValue = animator.animatedValue as Int
                    widgetConfigurationsViewsBinding.textInputSearchView.layoutParams.width = animatorValue
                    widgetConfigurationsViewsBinding.textInputSearchView.requestLayout()
                }
                valueAnimatorScales.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationStart(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        widgetConfigurationsViewsBinding.textInputSearchView.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out))
                        widgetConfigurationsViewsBinding.textInputSearchView.visibility = View.INVISIBLE

                        widgetConfigurationsViewsBinding.searchIcon.startAnimation(AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_in))
                        widgetConfigurationsViewsBinding.searchIcon.visibility = View.VISIBLE

                        widgetConfigurationsViewsBinding.searchFloatIt.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_down_zero))
                        widgetConfigurationsViewsBinding.searchFloatIt.visibility = View.INVISIBLE

                        widgetConfigurationsViewsBinding.searchClose.startAnimation(AnimationUtils.loadAnimation(applicationContext, R.anim.scale_down_zero))
                        widgetConfigurationsViewsBinding.searchClose.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationRepeat(animation: Animator) {

                    }
                })
                valueAnimatorScales.start()
            }
        } else {
            InAppBilling.ItemIAB = BillingManager.iapSearchEngines

            startActivity(Intent(applicationContext, InAppBilling::class.java),
                    ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
        }
    }
    /*Search Engine*/
}