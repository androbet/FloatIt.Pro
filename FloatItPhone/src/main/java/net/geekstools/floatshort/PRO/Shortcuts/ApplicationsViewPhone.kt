/*
 * Copyright © 2022 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 6/18/22, 9:18 AM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Shortcuts

import android.animation.Animator
import android.app.ActivityOptions
import android.app.Dialog
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.graphics.drawable.LayerDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.AccelerateInterpolator
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageView
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityOptionsCompat
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.OrientationHelper
import androidx.recyclerview.widget.RecyclerView
import com.android.billingclient.api.BillingClient
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.firebase.analytics.ktx.analytics
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.ktx.auth
import com.google.firebase.inappmessaging.FirebaseInAppMessagingClickListener
import com.google.firebase.inappmessaging.model.Action
import com.google.firebase.inappmessaging.model.InAppMessage
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import kotlinx.coroutines.*
import kotlinx.coroutines.flow.*
import net.geekstools.floatshort.PRO.BuildConfig
import net.geekstools.floatshort.PRO.Folders.FoldersConfigurations
import net.geekstools.floatshort.PRO.Preferences.PreferencesActivity
import net.geekstools.floatshort.PRO.R
import net.geekstools.floatshort.PRO.SearchEngine.Data.Filter.SearchResultType
import net.geekstools.floatshort.PRO.SearchEngine.UI.SearchEngine
import net.geekstools.floatshort.PRO.SecurityServices.AuthenticationProcess.Extensions.UserInterfaceExtraData
import net.geekstools.floatshort.PRO.SecurityServices.AuthenticationProcess.Fingerprint.AuthenticationFingerprint
import net.geekstools.floatshort.PRO.SecurityServices.AuthenticationProcess.Utils.AuthenticationCallback
import net.geekstools.floatshort.PRO.SecurityServices.AuthenticationProcess.Utils.SecurityInterfaceHolder
import net.geekstools.floatshort.PRO.Shortcuts.ShortcutsAdapter.ApplicationsViewItemsAdapter
import net.geekstools.floatshort.PRO.Shortcuts.ShortcutsAdapter.HybridSectionedGridRecyclerViewAdapter
import net.geekstools.floatshort.PRO.Utils.AdapterDataItem.RecycleViewSmoothLayoutGrid
import net.geekstools.floatshort.PRO.Utils.AdapterItemsData.AdapterItemsApplications
import net.geekstools.floatshort.PRO.Utils.Functions.Debug.Companion.PrintDebug
import net.geekstools.floatshort.PRO.Utils.Functions.Dialogues
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable
import net.geekstools.floatshort.PRO.Utils.InAppStore.DigitalAssets.InitializeInAppBilling
import net.geekstools.floatshort.PRO.Utils.InAppStore.DigitalAssets.Items.InAppBillingData
import net.geekstools.floatshort.PRO.Utils.InAppStore.DigitalAssets.Utils.PurchasesCheckpoint
import net.geekstools.floatshort.PRO.Utils.InAppUpdate.InAppUpdateProcess
import net.geekstools.floatshort.PRO.Utils.RemoteProcess.CloudMessageHandler
import net.geekstools.floatshort.PRO.Utils.RemoteProcess.LicenseValidator
import net.geekstools.floatshort.PRO.Utils.RemoteTask.Create.RecoveryFolders
import net.geekstools.floatshort.PRO.Utils.RemoteTask.Create.RecoveryShortcuts
import net.geekstools.floatshort.PRO.Utils.RemoteTask.Create.RecoveryWidgets
import net.geekstools.floatshort.PRO.Utils.UI.CustomIconManager.LoadCustomIcons
import net.geekstools.floatshort.PRO.Utils.UI.Gesture.GestureConstants
import net.geekstools.floatshort.PRO.Utils.UI.Gesture.GestureListenerConstants
import net.geekstools.floatshort.PRO.Utils.UI.Gesture.GestureListenerInterface
import net.geekstools.floatshort.PRO.Utils.UI.Gesture.SwipeGestureListener
import net.geekstools.floatshort.PRO.Utils.UI.PopupDialogue.WaitingDialogue
import net.geekstools.floatshort.PRO.Utils.UI.PopupDialogue.WaitingDialogueLiveData
import net.geekstools.floatshort.PRO.Utils.UI.PopupIndexedFastScroller.Factory.IndexedFastScrollerFactory
import net.geekstools.floatshort.PRO.Utils.UI.PopupIndexedFastScroller.IndexedFastScroller
import net.geekstools.floatshort.PRO.Widgets.WidgetConfigurations
import net.geekstools.floatshort.PRO.databinding.HybridApplicationViewBinding
import java.util.*
import kotlin.math.hypot

class ApplicationsViewPhone : AppCompatActivity(),
        View.OnClickListener, View.OnLongClickListener,
        GestureListenerInterface,
        FirebaseInAppMessagingClickListener {

    private val applicationsViewPhoneDependencyInjection: ApplicationsViewPhoneDependencyInjection by lazy {
        ApplicationsViewPhoneDependencyInjection(applicationContext)
    }

    private val dialogues: Dialogues by lazy {
        Dialogues(this@ApplicationsViewPhone, applicationsViewPhoneDependencyInjection.functionsClassLegacy)
    }

    private val listOfNewCharOfItemsForIndex: ArrayList<String> = ArrayList<String>()

    private val applicationsAdapterItems: ArrayList<AdapterItemsApplications> = ArrayList<AdapterItemsApplications>()

    private lateinit var recyclerViewAdapter: RecyclerView.Adapter<ApplicationsViewItemsAdapter.ViewHolder>
    private lateinit var recyclerViewLayoutManager: GridLayoutManager

    private val indexSections: ArrayList<HybridSectionedGridRecyclerViewAdapter.Section> = ArrayList<HybridSectionedGridRecyclerViewAdapter.Section>()

    private var indexSectionsPosition: Int = 0

    private lateinit var frequentlyUsedAppsList: Array<String>
    private lateinit var frequentlyUsedAppsCounter: IntArray
    var loadFrequentlyApps = false

    private val swipeGestureListener: SwipeGestureListener by lazy {
        SwipeGestureListener(applicationContext, this@ApplicationsViewPhone)
    }

    private val firebaseRemoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()


    private lateinit var firebaseAuth: FirebaseAuth

    private lateinit var waitingDialogue: Dialog

    private lateinit var waitingDialogueLiveData: WaitingDialogueLiveData

    private val loadCustomIcons: LoadCustomIcons by lazy {
        LoadCustomIcons(applicationContext, applicationsViewPhoneDependencyInjection.functionsClassLegacy.customIconPackageName())
    }

    private var billingClient: BillingClient? = null

    private lateinit var hybridApplicationViewBinding: HybridApplicationViewBinding

    private object Google {
        const val SignInRequest: Int = 666
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hybridApplicationViewBinding = HybridApplicationViewBinding.inflate(layoutInflater)
        setContentView(hybridApplicationViewBinding.root)

        applicationsViewPhoneDependencyInjection.functionsClassLegacy.loadSavedColor()
        applicationsViewPhoneDependencyInjection.functionsClassLegacy.checkLightDarkTheme()

        applicationsViewPhoneDependencyInjection.applicationThemeController.setThemeColorFloating(this, hybridApplicationViewBinding.MainView, applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent())
        dialogues.changeLog()

        recyclerViewLayoutManager = RecycleViewSmoothLayoutGrid(applicationContext, applicationsViewPhoneDependencyInjection.functionsClassLegacy.columnCount(105), OrientationHelper.VERTICAL, false)
        hybridApplicationViewBinding.applicationsListView.layoutManager = recyclerViewLayoutManager

        /*All Loading Process*/
        initiateLoadingProcessAll()
        /*All Loading Process*/

        val drawPreferenceAction: LayerDrawable = getDrawable(R.drawable.draw_pref_action) as LayerDrawable
        val backPreferenceAction: Drawable = drawPreferenceAction.findDrawableByLayerId(R.id.backgroundTemporary)
        backPreferenceAction.setTint(PublicVariable.primaryColorOpposite)
        hybridApplicationViewBinding.actionButton.setImageDrawable(drawPreferenceAction)

        hybridApplicationViewBinding.switchWidgets.setTextColor(getColor(R.color.light))
        hybridApplicationViewBinding.switchCategories.setTextColor(getColor(R.color.light))
        if (PublicVariable.themeLightDark /*light*/ && applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent() /*transparent*/) {
            hybridApplicationViewBinding.switchWidgets.setTextColor(getColor(R.color.dark))
            hybridApplicationViewBinding.switchCategories.setTextColor(getColor(R.color.dark))
        }

        hybridApplicationViewBinding.switchCategories.setBackgroundColor(if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent()) applicationsViewPhoneDependencyInjection.functionsClassLegacy.setColorAlpha(PublicVariable.primaryColor, 51f) else PublicVariable.primaryColor)

        hybridApplicationViewBinding.switchCategories.rippleColor = ColorStateList.valueOf(if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent()) applicationsViewPhoneDependencyInjection.functionsClassLegacy.setColorAlpha(PublicVariable.primaryColorOpposite, 51f) else PublicVariable.primaryColorOpposite)

        hybridApplicationViewBinding.switchWidgets.setBackgroundColor(if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent()) applicationsViewPhoneDependencyInjection.functionsClassLegacy.setColorAlpha(PublicVariable.primaryColor, 51f) else PublicVariable.primaryColor)

        hybridApplicationViewBinding.switchWidgets.rippleColor = ColorStateList.valueOf(if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent()) applicationsViewPhoneDependencyInjection.functionsClassLegacy.setColorAlpha(PublicVariable.primaryColorOpposite, 51f) else PublicVariable.primaryColorOpposite)

        hybridApplicationViewBinding.recoveryAction.setBackgroundColor(PublicVariable.primaryColorOpposite)
        hybridApplicationViewBinding.recoveryAction.rippleColor = ColorStateList.valueOf(PublicVariable.primaryColor)

        val drawRecoverFloatingCategories = getDrawable(R.drawable.draw_recovery)?.mutate() as LayerDrawable?
        val backRecoverFloatingCategories = drawRecoverFloatingCategories?.findDrawableByLayerId(R.id.backgroundTemporary)?.mutate()
        backRecoverFloatingCategories?.setTint(if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent()) applicationsViewPhoneDependencyInjection.functionsClassLegacy.setColorAlpha(PublicVariable.primaryColor, 51f) else PublicVariable.primaryColor)

        val drawRecoverFloatingWidgets = getDrawable(R.drawable.draw_recovery_widgets)?.mutate() as LayerDrawable?
        val backRecoverFloatingWidgets = drawRecoverFloatingWidgets?.findDrawableByLayerId(R.id.backgroundTemporary)?.mutate()
        backRecoverFloatingWidgets?.setTint(if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent()) applicationsViewPhoneDependencyInjection.functionsClassLegacy.setColorAlpha(PublicVariable.primaryColor, 51f) else PublicVariable.primaryColor)

        hybridApplicationViewBinding.recoverFloatingCategories.setImageDrawable(drawRecoverFloatingCategories)
        hybridApplicationViewBinding.recoverFloatingWidgets.setImageDrawable(drawRecoverFloatingWidgets)

        hybridApplicationViewBinding.actionButton.setOnClickListener {
            applicationsViewPhoneDependencyInjection.functionsClassLegacy.doVibrate(33)

            if (!PublicVariable.actionCenter) {

                val finalRadius = hypot(applicationsViewPhoneDependencyInjection.functionsClassLegacy.displayX().toDouble(), applicationsViewPhoneDependencyInjection.functionsClassLegacy.displayY().toDouble()).toInt()
                val circularReveal = ViewAnimationUtils.createCircularReveal(hybridApplicationViewBinding.recoveryAction, hybridApplicationViewBinding.actionButton.x.toInt(), hybridApplicationViewBinding.actionButton.y.toInt(), finalRadius.toFloat(), applicationsViewPhoneDependencyInjection.functionsClassLegacy.DpToInteger(13).toFloat())
                circularReveal.duration = 777
                circularReveal.interpolator = AccelerateInterpolator()
                circularReveal.start()
                circularReveal.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        hybridApplicationViewBinding.recoveryAction.visibility = View.INVISIBLE
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationStart(animation: Animator) {

                    }
                })

                applicationsViewPhoneDependencyInjection.functionsClassLegacy.openActionMenuOption(this@ApplicationsViewPhone, hybridApplicationViewBinding.fullActionViews, hybridApplicationViewBinding.actionButton, hybridApplicationViewBinding.fullActionViews.isShown)
            } else {
                hybridApplicationViewBinding.recoveryAction.visibility = View.VISIBLE

                val finalRadius = hypot(applicationsViewPhoneDependencyInjection.functionsClassLegacy.displayX().toDouble(), applicationsViewPhoneDependencyInjection.functionsClassLegacy.displayY().toDouble()).toInt()
                val circularReveal = ViewAnimationUtils.createCircularReveal(hybridApplicationViewBinding.recoveryAction, hybridApplicationViewBinding.actionButton.x.toInt(), hybridApplicationViewBinding.actionButton.y.toInt(), applicationsViewPhoneDependencyInjection.functionsClassLegacy.DpToInteger(13).toFloat(), finalRadius.toFloat())
                circularReveal.duration = 1300
                circularReveal.interpolator = AccelerateInterpolator()
                circularReveal.start()
                circularReveal.addListener(object : Animator.AnimatorListener {
                    override fun onAnimationRepeat(animation: Animator) {

                    }

                    override fun onAnimationEnd(animation: Animator) {
                        hybridApplicationViewBinding.recoveryAction.visibility = View.VISIBLE
                    }

                    override fun onAnimationCancel(animation: Animator) {

                    }

                    override fun onAnimationStart(animation: Animator) {

                    }
                })
            }
        }

        hybridApplicationViewBinding.switchCategories.setOnClickListener {
            try {
                applicationsViewPhoneDependencyInjection.functionsClassLegacy.navigateToClass(this@ApplicationsViewPhone, FoldersConfigurations::class.java,
                        ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_right, R.anim.slide_to_left))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
        hybridApplicationViewBinding.switchWidgets.setOnClickListener {
            if (applicationsViewPhoneDependencyInjection.networkCheckpoint.networkConnection() && firebaseAuth.currentUser != null) {

                if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.floatingWidgetsPurchased()) {

                    applicationsViewPhoneDependencyInjection.functionsClassLegacy.navigateToClass(this@ApplicationsViewPhone, WidgetConfigurations::class.java,
                            ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_left, R.anim.slide_to_right))

                } else {

                    startActivity(Intent(applicationContext, InitializeInAppBilling::class.java).apply {
                        putExtra(InitializeInAppBilling.Entry.PurchaseType, InitializeInAppBilling.Entry.OneTimePurchase)
                        putExtra(InitializeInAppBilling.Entry.ItemToPurchase, InAppBillingData.SKU.InAppItemFloatingWidgets)
                        addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    }, ActivityOptions.makeCustomAnimation(applicationContext, R.anim.down_up, android.R.anim.fade_out).toBundle())

                }
            } else {
                if (applicationsViewPhoneDependencyInjection.networkCheckpoint.networkConnection()) {
                    Toast.makeText(applicationContext, getString(R.string.internetError), Toast.LENGTH_LONG).show()
                }

                if (firebaseAuth.currentUser == null) {
                    Toast.makeText(applicationContext, getString(R.string.authError), Toast.LENGTH_LONG).show()
                }
            }
        }

        hybridApplicationViewBinding.recoveryAction.setOnClickListener {
            Intent(applicationContext, RecoveryShortcuts::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(it)
            }
        }
        hybridApplicationViewBinding.recoverFloatingCategories.setOnClickListener {
            Intent(applicationContext, RecoveryFolders::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(it)
            }

            val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
            hybridApplicationViewBinding.recoverFloatingCategories.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    hybridApplicationViewBinding.recoverFloatingCategories.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {

                }

            })
        }
        hybridApplicationViewBinding.recoverFloatingWidgets.setOnClickListener {
            Intent(applicationContext, RecoveryWidgets::class.java).let {
                it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                startService(it)
            }

            val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
            hybridApplicationViewBinding.recoverFloatingWidgets.startAnimation(animation)
            animation.setAnimationListener(object : Animation.AnimationListener {
                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    hybridApplicationViewBinding.recoverFloatingWidgets.visibility = View.INVISIBLE
                }

                override fun onAnimationStart(animation: Animation?) {

                }

            })
        }

        hybridApplicationViewBinding.actionButton.setOnLongClickListener {
            Handler(Looper.getMainLooper()).postDelayed({
                val activityOptionsCompat = ActivityOptionsCompat.makeSceneTransitionAnimation(this@ApplicationsViewPhone, hybridApplicationViewBinding.actionButton, "transition")
                Intent().let {
                    it.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                    it.setClass(this@ApplicationsViewPhone, PreferencesActivity::class.java)
                    startActivity(it, activityOptionsCompat.toBundle())
                }
            }, 113)

            true
        }

        hybridApplicationViewBinding.switchCategories.setOnLongClickListener {
            if (!hybridApplicationViewBinding.recoverFloatingCategories.isShown) {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_show)
                hybridApplicationViewBinding.recoverFloatingCategories.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        hybridApplicationViewBinding.recoverFloatingCategories.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            } else {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
                hybridApplicationViewBinding.recoverFloatingCategories.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }
                    override fun onAnimationEnd(animation: Animation) {
                        hybridApplicationViewBinding.recoverFloatingCategories.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            }

            true
        }
        hybridApplicationViewBinding.switchWidgets.setOnLongClickListener {
            if (!hybridApplicationViewBinding.recoverFloatingWidgets.isShown) {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_show)
                hybridApplicationViewBinding.recoverFloatingWidgets.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        hybridApplicationViewBinding.recoverFloatingWidgets.visibility = View.VISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            } else {
                val animation = AnimationUtils.loadAnimation(applicationContext, R.anim.recovery_actions_hide)
                hybridApplicationViewBinding.recoverFloatingWidgets.startAnimation(animation)
                animation.setAnimationListener(object : Animation.AnimationListener {
                    override fun onAnimationStart(animation: Animation) {

                    }

                    override fun onAnimationEnd(animation: Animation) {
                        hybridApplicationViewBinding.recoverFloatingWidgets.visibility = View.INVISIBLE
                    }

                    override fun onAnimationRepeat(animation: Animation) {

                    }
                })
            }

            true
        }

        //In-App Billing
        billingClient = PurchasesCheckpoint(this@ApplicationsViewPhone).trigger()

        firebaseAuth = FirebaseAuth.getInstance()

        if (BuildConfig.VERSION_NAME.contains("[BETA]") && !applicationsViewPhoneDependencyInjection.functionsClassLegacy.readPreference(".UserInformation", "SubscribeToBeta", false)) {
            FirebaseMessaging.getInstance().subscribeToTopic("BETA").addOnSuccessListener {
                applicationsViewPhoneDependencyInjection.functionsClassLegacy.savePreference(".UserInformation", "SubscribeToBeta", true)
            }.addOnFailureListener {

            }
        }




    }

    override fun onStart() {
        super.onStart()

        if (!getFileStreamPath(".License").exists()
                && applicationsViewPhoneDependencyInjection.networkCheckpoint.networkConnection()
                && !BuildConfig.DEBUG) {

            startService(Intent(applicationContext, LicenseValidator::class.java))

            val intentFilter = IntentFilter()
            intentFilter.addAction(getString(R.string.license))
            val broadcastReceiver: BroadcastReceiver = object : BroadcastReceiver() {
                override fun onReceive(context: Context, intent: Intent) {
                    if (intent.action == getString(R.string.license)) {
                        applicationsViewPhoneDependencyInjection.functionsClassLegacy.dialogueLicense(this@ApplicationsViewPhone)

                        Handler(Looper.getMainLooper()).postDelayed({
                            stopService(Intent(applicationContext, LicenseValidator::class.java))
                        }, 1000)

                        unregisterReceiver(this)
                    }
                }
            }
            registerReceiver(broadcastReceiver, intentFilter)
        }

        if (applicationsViewPhoneDependencyInjection.networkCheckpoint.networkConnection()
                && applicationsViewPhoneDependencyInjection.functionsClassLegacy.readPreference(".UserInformation", "userEmail", null) == null
                && firebaseAuth.currentUser == null) {

            val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                    .requestIdToken(getString(R.string.webClientId))
                    .requestEmail()
                    .build()

            val googleSignInClient = GoogleSignIn.getClient(this, googleSignInOptions)
            googleSignInClient.signInIntent.run {
                startActivityForResult(this, Google.SignInRequest)
            }

            waitingDialogueLiveData = ViewModelProvider(this@ApplicationsViewPhone).get(WaitingDialogueLiveData::class.java)
            waitingDialogueLiveData.run {
                this.dialogueTitle.value = getString(R.string.signinTitle)
                this.dialogueMessage.value = getString(R.string.signinMessage)

                waitingDialogue = WaitingDialogue().initShow(this@ApplicationsViewPhone)
            }
        }

        val drawableShare: LayerDrawable? = getDrawable(R.drawable.draw_share) as LayerDrawable
        val backgroundShare = drawableShare!!.findDrawableByLayerId(R.id.backgroundTemporary)
        backgroundShare.setTint(PublicVariable.primaryColor)

        hybridApplicationViewBinding.shareIt.setImageDrawable(drawableShare)
        hybridApplicationViewBinding.shareIt.setOnClickListener {

            Firebase.analytics.logEvent("ShareIt", Bundle().apply {
                putString("Email Address", Firebase.auth.currentUser?.email)
            })

            applicationsViewPhoneDependencyInjection.functionsClassLegacy.doVibrate(50)

            val shareText = getString(R.string.shareTitle) +
                    "\n" + getString(R.string.shareSummary) +
                    "\n" + getString(R.string.play_store_link) + packageName
            val sharingIntent = Intent(Intent.ACTION_SEND).apply {
                this.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                this.putExtra(Intent.EXTRA_TEXT, shareText)
                this.type = "text/plain"
            }
            startActivity(sharingIntent)

        }
    }

    override fun onResume() {
        super.onResume()
        PublicVariable.inMemory = true

        firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default)
        firebaseRemoteConfig.fetch(0)
                .addOnSuccessListener {

                    firebaseRemoteConfig.activate().addOnSuccessListener {
                        if (firebaseRemoteConfig.getLong(applicationsViewPhoneDependencyInjection.functionsClassLegacy.versionCodeRemoteConfigKey()) > applicationsViewPhoneDependencyInjection.functionsClassLegacy.applicationVersionCode(packageName)) {

                            val layerDrawableNewUpdate = getDrawable(R.drawable.ic_update) as LayerDrawable?
                            val gradientDrawableNewUpdate = layerDrawableNewUpdate?.findDrawableByLayerId(R.id.ic_launcher_back_layer) as BitmapDrawable?
                            gradientDrawableNewUpdate?.setTint(PublicVariable.primaryColor)

                            hybridApplicationViewBinding.newUpdate.setImageDrawable(layerDrawableNewUpdate)
                            hybridApplicationViewBinding.newUpdate.visibility = View.VISIBLE

                            hybridApplicationViewBinding.newUpdate.setOnClickListener {
                                applicationsViewPhoneDependencyInjection.functionsClassLegacy.upcomingChangeLog(
                                        this@ApplicationsViewPhone,
                                        firebaseRemoteConfig.getString(applicationsViewPhoneDependencyInjection.functionsClassLegacy.upcomingChangeLogRemoteConfigKey()), firebaseRemoteConfig.getLong(applicationsViewPhoneDependencyInjection.functionsClassLegacy.versionCodeRemoteConfigKey()).toString())
                            }

                            applicationsViewPhoneDependencyInjection.functionsClassLegacy.notificationCreator(
                                    getString(R.string.updateAvailable),
                                    firebaseRemoteConfig.getString(applicationsViewPhoneDependencyInjection.functionsClassLegacy.upcomingChangeLogSummaryConfigKey()),
                                    firebaseRemoteConfig.getLong(applicationsViewPhoneDependencyInjection.functionsClassLegacy.versionCodeRemoteConfigKey()).toInt()
                            )

                            val inAppUpdateTriggeredTime =
                                    (Calendar.getInstance()[Calendar.YEAR].toString() + Calendar.getInstance()[Calendar.MONTH].toString() + Calendar.getInstance()[Calendar.DATE].toString())
                                            .toInt()

                            if (firebaseAuth.currentUser != null
                                    && applicationsViewPhoneDependencyInjection.functionsClassLegacy.readPreference("InAppUpdate", "TriggeredDate", 0) < inAppUpdateTriggeredTime) {

                                startActivity(Intent(applicationContext, InAppUpdateProcess::class.java)
                                        .putExtra("UPDATE_CHANGE_LOG", firebaseRemoteConfig.getString(applicationsViewPhoneDependencyInjection.functionsClassLegacy.upcomingChangeLogRemoteConfigKey()))
                                        .putExtra("UPDATE_VERSION", firebaseRemoteConfig.getLong(applicationsViewPhoneDependencyInjection.functionsClassLegacy.versionCodeRemoteConfigKey()).toString())
                                        .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK),
                                        ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())
                            }
                        }
                    }
                }
    }

    override fun onPause() {
        super.onPause()

        val firebaseUser = FirebaseAuth.getInstance().currentUser
        firebaseUser?.reload()?.addOnCompleteListener {
            firebaseAuth.addAuthStateListener { firebaseAuth ->
                val user = firebaseAuth.currentUser
                if (user == null) {
                    applicationsViewPhoneDependencyInjection.functionsClassLegacy.savePreference(".UserInformation", "userEmail", null)

                    val googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                            .requestIdToken(getString(R.string.webClientId))
                            .requestEmail()
                            .build()

                    val googleSignInClient = GoogleSignIn.getClient(this@ApplicationsViewPhone, googleSignInOptions)
                    try {
                        googleSignInClient.signOut()
                        googleSignInClient.revokeAccess()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                } else {

                }
            }
        }

        applicationsViewPhoneDependencyInjection.popupApplicationShortcuts.addPopupApplicationShortcuts()

        applicationsViewPhoneDependencyInjection.functionsClassLegacy.savePreference("LoadView", "LoadViewPosition", recyclerViewLayoutManager.findFirstVisibleItemPosition())
        if (PublicVariable.actionCenter) {
            applicationsViewPhoneDependencyInjection.functionsClassLegacy.closeActionMenuOption(this@ApplicationsViewPhone, hybridApplicationViewBinding.fullActionViews, hybridApplicationViewBinding.actionButton)
        }

        applicationsViewPhoneDependencyInjection.functionsClassLegacy.savePreference("OpenMode", "openClassName", this.javaClass.simpleName)

        billingClient?.endConnection()

    }

    override fun onDestroy() {
        super.onDestroy()

        PublicVariable.inMemory = false

    }

    override fun onBackPressed() {
        val homeScreen = Intent(Intent.ACTION_MAIN).apply {
            this.addCategory(Intent.CATEGORY_HOME)
            this.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeScreen, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, android.R.anim.fade_out).toBundle())

        applicationsViewPhoneDependencyInjection.functionsClassLegacy.CheckSystemRAM(this@ApplicationsViewPhone)
    }

    override fun onClick(view: View?) {
        if (view is ImageView) {
            val position = view.id

            applicationsViewPhoneDependencyInjection.floatingServices
                    .runUnlimitedShortcutsServiceFrequently(frequentlyUsedAppsList[position])
        }
    }

    override fun onLongClick(view: View?): Boolean {
        if (view is ImageView) {
            val position = view.id
            if (applicationsViewPhoneDependencyInjection.securityFunctions.isAppLocked(frequentlyUsedAppsList[position])) {

                SecurityInterfaceHolder.authenticationCallback = object : AuthenticationCallback {

                    override fun authenticatedFloatIt(extraInformation: Bundle?) {
                        super.authenticatedFloatIt(extraInformation)
                        Log.d(this@ApplicationsViewPhone.javaClass.simpleName, "AuthenticatedFloatingShortcuts")

                        applicationsViewPhoneDependencyInjection.functionsClassLegacy
                                .appsLaunchPad(frequentlyUsedAppsList[position])
                    }

                    override fun failedAuthenticated() {
                        super.failedAuthenticated()
                        Log.d(this@ApplicationsViewPhone.javaClass.simpleName, "FailedAuthenticated")

                    }

                    override fun invokedPinPassword() {
                        super.invokedPinPassword()
                        Log.d(this@ApplicationsViewPhone.javaClass.simpleName, "InvokedPinPassword")

                    }
                }

                startActivity(Intent(applicationContext, AuthenticationFingerprint::class.java).apply {
                    putExtra(UserInterfaceExtraData.OtherTitle, applicationsViewPhoneDependencyInjection.functionsClassLegacy.applicationName(frequentlyUsedAppsList[position]))
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                }, ActivityOptions.makeCustomAnimation(applicationContext, android.R.anim.fade_in, 0).toBundle())

            } else {

                applicationsViewPhoneDependencyInjection.functionsClassLegacy
                        .appsLaunchPad(frequentlyUsedAppsList[position])
            }
        }
        return true
    }

    override fun onSwipeGesture(gestureConstants: GestureConstants, downMotionEvent: MotionEvent, moveMotionEvent: MotionEvent, initVelocityX: Float, initVelocityY: Float) {
        super.onSwipeGesture(gestureConstants, downMotionEvent, moveMotionEvent, initVelocityX, initVelocityY)

        when (gestureConstants) {
            is GestureConstants.SwipeHorizontal -> {
                when (gestureConstants.horizontalDirection) {
                    GestureListenerConstants.SWIPE_RIGHT -> {
                        if (applicationsViewPhoneDependencyInjection.networkCheckpoint.networkConnection() && firebaseAuth.currentUser != null) {

                            if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.floatingWidgetsPurchased()) {

                                applicationsViewPhoneDependencyInjection.functionsClassLegacy.navigateToClass(this@ApplicationsViewPhone, WidgetConfigurations::class.java,
                                        ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_left, R.anim.slide_to_right))

                            } else {

                                startActivity(Intent(applicationContext, InitializeInAppBilling::class.java).apply {
                                    putExtra(InitializeInAppBilling.Entry.PurchaseType, InitializeInAppBilling.Entry.OneTimePurchase)
                                    putExtra(InitializeInAppBilling.Entry.ItemToPurchase, InAppBillingData.SKU.InAppItemFloatingWidgets)
                                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                }, ActivityOptions.makeCustomAnimation(applicationContext, R.anim.down_up, android.R.anim.fade_out).toBundle())
                            }
                        } else {
                            if (applicationsViewPhoneDependencyInjection.networkCheckpoint.networkConnection()) {
                                Toast.makeText(applicationContext, getString(R.string.internetError), Toast.LENGTH_LONG).show()
                            }

                            if (firebaseAuth.currentUser == null) {
                                Toast.makeText(applicationContext, getString(R.string.authError), Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                    GestureListenerConstants.SWIPE_LEFT -> {
                        applicationsViewPhoneDependencyInjection.functionsClassLegacy.navigateToClass(this@ApplicationsViewPhone, FoldersConfigurations::class.java,
                                ActivityOptions.makeCustomAnimation(applicationContext, R.anim.slide_from_right, R.anim.slide_to_left))
                    }
                }
            }
            else -> {}
        }
    }

    override fun dispatchTouchEvent(motionEvent: MotionEvent?): Boolean {
        motionEvent?.let {
            swipeGestureListener.onTouchEvent(it)
        }

        return if (motionEvent != null) {
            super.dispatchTouchEvent(motionEvent)
        } else {
            false
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        when (requestCode) {
            Google.SignInRequest -> {

                GoogleSignIn.getSignedInAccountFromIntent(data).addOnCompleteListener { googleSignInAccountTask ->

                    val authCredential = GoogleAuthProvider.getCredential(googleSignInAccountTask.result.idToken, null)
                    firebaseAuth.signInWithCredential(authCredential)
                        .addOnSuccessListener {
                            val firebaseUser = firebaseAuth.currentUser
                            if (firebaseUser != null) {
                                PrintDebug("Firebase Activities Done Successfully")

                                applicationsViewPhoneDependencyInjection.functionsClassLegacy.savePreference(".UserInformation", "userEmail", firebaseUser.email)

                                applicationsViewPhoneDependencyInjection.functionsClassLegacy.Toast(getString(R.string.signinFinished), Gravity.TOP)

                                applicationsViewPhoneDependencyInjection.securityFunctions.downloadLockedAppsData()

                                waitingDialogue.dismiss()
                            }
                        }.addOnFailureListener { exception ->

                            waitingDialogueLiveData.run {
                                this.dialogueTitle.value = getString(R.string.error)
                                this.dialogueMessage.value = exception.message
                            }
                        }

                }

            }
        }
    }

    override fun messageClicked(inAppMessage: InAppMessage, action: Action) {

        CloudMessageHandler()
                .extractData(inAppMessage, action)
    }

    private fun initiateLoadingProcessAll() {
        if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.appThemeTransparent()) {
            hybridApplicationViewBinding.loadingSplash.setBackgroundColor(Color.TRANSPARENT)
        } else {
            hybridApplicationViewBinding.loadingSplash.setBackgroundColor(window.navigationBarColor)
        }

        if (PublicVariable.themeLightDark) {
            hybridApplicationViewBinding.loadingProgress.indeterminateDrawable.colorFilter = PorterDuffColorFilter(PublicVariable.darkMutedColor, PorterDuff.Mode.MULTIPLY)
        } else if (!PublicVariable.themeLightDark) {
            hybridApplicationViewBinding.loadingProgress.indeterminateDrawable.colorFilter = PorterDuffColorFilter(PublicVariable.vibrantColor, PorterDuff.Mode.MULTIPLY)
        }

        val layerDrawableLoadLogo: LayerDrawable = getDrawable(R.drawable.ic_launcher_layer) as LayerDrawable
        val gradientDrawableLoadLogo: BitmapDrawable = layerDrawableLoadLogo!!.findDrawableByLayerId(R.id.ic_launcher_back_layer) as BitmapDrawable
        gradientDrawableLoadLogo.setTint(PublicVariable.primaryColor)
        hybridApplicationViewBinding.loadingLogo.setImageDrawable(layerDrawableLoadLogo)

        loadApplicationsData()
    }

    private fun loadApplicationsData() = CoroutineScope(SupervisorJob() + Dispatchers.Default).launch {

        if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.customIconsEnable()) {
            loadCustomIcons.load()
        }

        val applicationInfoList = packageManager.queryIntentActivities(Intent().apply {
            action = Intent.ACTION_MAIN
            addCategory(Intent.CATEGORY_LAUNCHER)
        }, PackageManager.MATCH_ALL)
        val applicationInfoListSorted = applicationInfoList.sortedWith(ResolveInfo.DisplayNameComparator(packageManager))

        var newChar: String = "A"
        var oldChar: String = "Z"

        listOfNewCharOfItemsForIndex.clear()

        applicationInfoListSorted.asFlow()
//                .flowOn(Dispatchers.IO)
                .filter {

                    (packageManager.getLaunchIntentForPackage(it.activityInfo.packageName) != null)
                }
                .onCompletion {

                    if (intent.getStringArrayExtra("frequentApps") != null) {
                        frequentlyUsedAppsList = intent.getStringArrayExtra("frequentApps")!!
                        val freqLength = intent.getIntExtra("frequentAppsNumbers", -1)

                        PublicVariable.frequentlyUsedApps = frequentlyUsedAppsList
                        PublicVariable.freqLength = freqLength

                        if (freqLength > 1) {
                            loadFrequentlyApps = true
                        }
                    }
                }
                .withIndex().collect {

                    val installedPackageName = it.value.activityInfo.packageName
                    val installedClassName = it.value.activityInfo.name
                    val installedAppName: String? = applicationsViewPhoneDependencyInjection.functionsClassLegacy.activityLabel(it.value.activityInfo)

                    newChar = try {

                        installedAppName?.substring(0, 1)?.toUpperCase(Locale.getDefault())?:"Z"

                    } catch (e: StringIndexOutOfBoundsException) {
                        e.printStackTrace()

                        "Z"
                    }

                    if (it.index == 0) {
                        indexSections.add(HybridSectionedGridRecyclerViewAdapter.Section(indexSectionsPosition, newChar))
                    } else {

                        if (oldChar != newChar) {
                            indexSections.add(HybridSectionedGridRecyclerViewAdapter.Section(indexSectionsPosition, newChar))

                            listOfNewCharOfItemsForIndex.add(newChar)
                        }
                    }

                    val installedAppIcon = if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.customIconsEnable()) {
                        loadCustomIcons.getDrawableIconForPackage(installedPackageName, applicationsViewPhoneDependencyInjection.functionsClassLegacy.shapedAppIcon(it.value.activityInfo))
                    } else {
                        applicationsViewPhoneDependencyInjection.functionsClassLegacy.shapedAppIcon(it.value.activityInfo)
                    }

                    applicationsAdapterItems.add(AdapterItemsApplications(installedAppName?:"Unknown",
                            installedPackageName!!, installedClassName!!,
                            installedAppIcon!!,
                            applicationsViewPhoneDependencyInjection.functionsClassLegacy.extractDominantColor(installedAppIcon),
                            SearchResultType.SearchShortcuts))

                    listOfNewCharOfItemsForIndex.add(newChar)

                    indexSectionsPosition += 1

                    oldChar = try {

                        installedAppName?.substring(0, 1)?.toUpperCase(Locale.getDefault())?:"Z"

                    } catch (e: StringIndexOutOfBoundsException) {
                        e.printStackTrace()

                        "Z"
                    }
                }

        withContext(Dispatchers.Main) {
            recyclerViewAdapter = ApplicationsViewItemsAdapter(applicationContext, applicationsAdapterItems)

            val splashAnimation = AnimationUtils.loadAnimation(applicationContext, android.R.anim.fade_out)
            hybridApplicationViewBinding.loadingSplash.startAnimation(splashAnimation)
            splashAnimation.setAnimationListener(object : Animation.AnimationListener {

                override fun onAnimationStart(animation: Animation?) {

                    if (loadFrequentlyApps) {
                        CoroutineScope(Dispatchers.Main).launch {

                            val loadFrequentlyUsedApplications = loadFrequentlyUsedApplications()
                            loadFrequentlyUsedApplications.flowOn(Dispatchers.Main).collect {


                            }

                        }
                    }

                    /*Search Engine*/
                    SearchEngine(activity = this@ApplicationsViewPhone, context = applicationContext,
                            searchEngineViewBinding = hybridApplicationViewBinding.searchEngineViewInclude,
                            functionsClassLegacy = applicationsViewPhoneDependencyInjection.functionsClassLegacy,
                            fileIO = applicationsViewPhoneDependencyInjection.fileIO,
                            floatingServices = applicationsViewPhoneDependencyInjection.floatingServices,
                            customIcons = loadCustomIcons,
                            firebaseAuth = firebaseAuth).apply {

                        this.initializeSearchEngineData()
                    }
                    /*Search Engine*/

                    hybridApplicationViewBinding.switchFloating.visibility = View.VISIBLE
                }

                override fun onAnimationRepeat(animation: Animation?) {

                }

                override fun onAnimationEnd(animation: Animation?) {
                    hybridApplicationViewBinding.loadingSplash.visibility = View.INVISIBLE
                }
            })

            if (loadFrequentlyApps) {
                val layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                )
                layoutParams.addRule(RelativeLayout.ABOVE, R.id.freqList)
                hybridApplicationViewBinding.nestedScrollView.layoutParams = layoutParams

                hybridApplicationViewBinding.scrollRelativeLayout.setPadding(0, hybridApplicationViewBinding.scrollRelativeLayout.paddingTop, 0, 0)
            } else {
                hybridApplicationViewBinding.MainView.removeView(hybridApplicationViewBinding.freqList)

                val layoutParams = RelativeLayout.LayoutParams(
                        RelativeLayout.LayoutParams.MATCH_PARENT,
                        RelativeLayout.LayoutParams.MATCH_PARENT
                )
                hybridApplicationViewBinding.nestedScrollView.layoutParams = layoutParams
            }

            recyclerViewAdapter.notifyDataSetChanged()
            val sectionsData = arrayOfNulls<HybridSectionedGridRecyclerViewAdapter.Section>(indexSections.size)
            val hybridSectionedGridRecyclerViewAdapter = HybridSectionedGridRecyclerViewAdapter(
                    applicationContext,
                    R.layout.hybrid_sections,
                    R.id.section_text,
                    hybridApplicationViewBinding.applicationsListView,
                    recyclerViewAdapter
            )

            hybridSectionedGridRecyclerViewAdapter.setSections(indexSections.toArray(sectionsData))
            hybridApplicationViewBinding.applicationsListView.adapter = hybridSectionedGridRecyclerViewAdapter

            recyclerViewLayoutManager.scrollToPosition(0)
            hybridApplicationViewBinding.nestedScrollView.scrollTo(0, 0)
        }

        /*Indexed Popup Fast Scroller*/
        val indexedFastScroller: IndexedFastScroller = IndexedFastScroller(
                context = applicationContext,
                layoutInflater = layoutInflater,
                rootView = hybridApplicationViewBinding.MainView,
                nestedScrollView = hybridApplicationViewBinding.nestedScrollView,
                recyclerView = hybridApplicationViewBinding.applicationsListView,
                fastScrollerIndexViewBinding = hybridApplicationViewBinding.fastScrollerIndexInclude,
                indexedFastScrollerFactory = IndexedFastScrollerFactory(
                        popupEnable = !applicationsViewPhoneDependencyInjection.functionsClassLegacy.litePreferencesEnabled(),
                        popupTextColor = PublicVariable.colorLightDarkOpposite,
                        indexItemTextColor = PublicVariable.colorLightDarkOpposite)
        )
        indexedFastScroller.initializeIndexView().await()
                .loadIndexData(listOfNewCharOfItemsForIndex = listOfNewCharOfItemsForIndex).await()
        /*Indexed Popup Fast Scroller*/

        loadInstalledCustomIconPackages().await()
    }

    private fun loadFrequentlyUsedApplications(): Flow<String> = flow() {

        val layoutParamsAbove = hybridApplicationViewBinding.searchEngineViewInclude.root.layoutParams as RelativeLayout.LayoutParams
        layoutParamsAbove.addRule(RelativeLayout.ABOVE, R.id.freqList)

        hybridApplicationViewBinding.searchEngineViewInclude.root.layoutParams = layoutParamsAbove
        hybridApplicationViewBinding.searchEngineViewInclude.root.bringToFront()

        hybridApplicationViewBinding.freqItem.removeAllViews()

        hybridApplicationViewBinding.freqList.visibility = View.VISIBLE

        frequentlyUsedAppsCounter = IntArray(25)
        frequentlyUsedAppsList = intent.getStringArrayExtra("frequentApps")!!
        val freqLength = intent.getIntExtra("frequentAppsNumbers", -1)
        if (getFileStreamPath("Frequently").exists()) {
            applicationsViewPhoneDependencyInjection.fileIO.removeLine(".categoryInfo", "Frequently")
            deleteFile("Frequently")
        }
        applicationsViewPhoneDependencyInjection.fileIO.saveFileAppendLine(".categoryInfo", "Frequently")

        for (i in 0 until freqLength) {

            val freqLayout = layoutInflater.inflate(R.layout.freq_item, null) as RelativeLayout

            val shapesImage = applicationsViewPhoneDependencyInjection.functionsClassLegacy.initShapesImage(freqLayout, R.id.freqItems)
            shapesImage.id = i
            shapesImage.setOnClickListener(this@ApplicationsViewPhone)
            shapesImage.setOnLongClickListener(this@ApplicationsViewPhone)
            shapesImage.setImageDrawable(if (applicationsViewPhoneDependencyInjection.functionsClassLegacy.customIconsEnable()) {
                loadCustomIcons.getDrawableIconForPackage(frequentlyUsedAppsList[i], applicationsViewPhoneDependencyInjection.functionsClassLegacy.shapedAppIcon(frequentlyUsedAppsList[i]))
            } else {
                applicationsViewPhoneDependencyInjection.functionsClassLegacy.shapedAppIcon(frequentlyUsedAppsList[i])
            })

            hybridApplicationViewBinding.freqItem.addView(freqLayout)

            applicationsViewPhoneDependencyInjection.fileIO.saveFileAppendLine("Frequently", frequentlyUsedAppsList[i])
            applicationsViewPhoneDependencyInjection.fileIO.saveFile(frequentlyUsedAppsList[i] + "Frequently", frequentlyUsedAppsList[i])

            this.emit(frequentlyUsedAppsList[i])

        }

        applicationsViewPhoneDependencyInjection.popupApplicationShortcuts.addPopupApplicationShortcuts()

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
            PrintDebug("CustomIconPackages ::: " + resolveInfo.activityInfo.packageName)
            PublicVariable.customIconsPackages.add(resolveInfo.activityInfo.packageName)
        }
    }
}