/*
 * Copyright © 2020 By Geeks Empire.
 *
 * Created by Elias Fazel
 * Last modified 4/16/20 11:59 PM
 *
 * Licensed Under MIT License.
 * https://opensource.org/licenses/MIT
 */

package net.geekstools.floatshort.PRO.Utils.IAP;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.android.billingclient.api.BillingClient;
import com.android.billingclient.api.BillingResult;
import com.android.billingclient.api.SkuDetails;
import com.android.billingclient.api.SkuDetailsResponseListener;
import com.bumptech.glide.Glide;
import com.bumptech.glide.RequestManager;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.button.MaterialButton;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import net.geekstools.floatshort.PRO.R;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClass;
import net.geekstools.floatshort.PRO.Utils.Functions.FunctionsClassDebug;
import net.geekstools.floatshort.PRO.Utils.Functions.PublicVariable;
import net.geekstools.floatshort.PRO.Utils.IAP.billing.BillingManager;
import net.geekstools.floatshort.PRO.Utils.IAP.billing.BillingProvider;
import net.geekstools.floatshort.PRO.Utils.IAP.skulist.SkusAdapter;
import net.geekstools.floatshort.PRO.Utils.IAP.skulist.row.SkuRowData;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

@Deprecated
public class AcquireFragment extends DialogFragment implements View.OnClickListener {

    Activity activity;
    Context context;

    FunctionsClass functionsClass;

    RecyclerView recyclerView;
    ProgressBar progressBar;

    HorizontalScrollView itemIABDemo;
    LinearLayout itemIABDemoList;
    TextView itemIABDemoDescription;

    SkusAdapter skusAdapter;

    MaterialButton materialButtonShare;

    BillingProvider billingProvider;

    TreeMap<Integer, Drawable> mapIndexDrawable = new TreeMap<Integer, Drawable>();
    TreeMap<Integer, Uri> mapIndexURI = new TreeMap<Integer, Uri>();

    RequestManager requestManager;

    int screenshotsNumber = 6, glideLoadCounter = 0;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.activity = getActivity();
        this.context = getContext();

        functionsClass = new FunctionsClass(context);
        requestManager = Glide.with(context);

        if (PublicVariable.themeLightDark) {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AppThemeLight);
        } else {
            setStyle(DialogFragment.STYLE_NORMAL, R.style.AppThemeDark);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View root = inflater.inflate(R.layout.iap_fragment, container, false);

        recyclerView = (RecyclerView) root.findViewById(R.id.list);

        progressBar = (ProgressBar) root.findViewById(R.id.progress_circular);

        itemIABDemo = (HorizontalScrollView) root.findViewById(R.id.itemIABDemo);
        itemIABDemoList = (LinearLayout) root.findViewById(R.id.itemIABDemoList);
        itemIABDemoDescription = (TextView) root.findViewById(R.id.itemIABDemoDescription);

        materialButtonShare = (MaterialButton) root.findViewById(R.id.shareNow);

        root.findViewById(R.id.backgroundFull).setBackgroundColor(PublicVariable.themeLightDark ? context.getColor(R.color.light) : context.getColor(R.color.dark));

        onManagerReady((BillingProvider) activity);

        if (InAppBilling.ItemIAB != null) {
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction("LOAD_SCREENSHOTS");
            BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
                @Override
                public void onReceive(Context context, Intent intent) {

                    if (intent.getAction().equals("LOAD_SCREENSHOTS")) {

                        for (int i = 1; i <= screenshotsNumber; i++) {
                            FunctionsClassDebug.Companion.PrintDebug(">>> " + mapIndexURI.get(i) + " <<<");

                            RelativeLayout demoLayout = (RelativeLayout) activity.getLayoutInflater().inflate(R.layout.iap_demo_layout, null);
                            ImageView demoItem = (ImageView) demoLayout.findViewById(R.id.DemoItem);

                            demoItem.setImageDrawable(mapIndexDrawable.get(i));
                            demoItem.setOnClickListener(AcquireFragment.this);
                            demoItem.setTag(mapIndexURI.get(i));
                            itemIABDemoList.addView(demoLayout);
                        }
                    }
                }
            };
            try {
                context.registerReceiver(broadcastReceiver, intentFilter);
            } catch (Exception e) {
                e.printStackTrace();
            }

            if (!functionsClass.floatingWidgetsPurchased()
                    || !functionsClass.securityServicesSubscribed()
                    || !functionsClass.searchEngineSubscribed()) {

                itemIABDemoDescription.setTextColor(PublicVariable.themeLightDark ? context.getColor(R.color.dark) : context.getColor(R.color.light));
                if (InAppBilling.ItemIAB.equals(BillingManager.iapFloatingWidgets)) {
                    itemIABDemoDescription.setText(Html.fromHtml(getString(R.string.floatingWidgetsDemoDescriptions)));
                }
                if (InAppBilling.ItemIAB.equals(BillingManager.iapSearchEngines)) {
                    itemIABDemoDescription.setText(Html.fromHtml(getString(R.string.searchEngineDemoDescriptions)));
                }
                if (InAppBilling.ItemIAB.equals(BillingManager.iapSecurityServices)) {
                    itemIABDemoDescription.setText(Html.fromHtml(getString(R.string.securityServicesDemoDescriptions)));
                }

                FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                firebaseRemoteConfig.setDefaultsAsync(R.xml.remote_config_default);
                firebaseRemoteConfig.setConfigSettingsAsync(new FirebaseRemoteConfigSettings.Builder().setMinimumFetchIntervalInSeconds(1).build());
                firebaseRemoteConfig.fetchAndActivate().addOnSuccessListener(new OnSuccessListener<Boolean>() {
                    @Override
                    public void onSuccess(Boolean aBoolean) {

                        if (InAppBilling.ItemIAB.equals(BillingManager.iapFloatingWidgets)) {
                            itemIABDemoDescription.setText(Html.fromHtml(firebaseRemoteConfig.getString("floating_widgets_description")));
                            screenshotsNumber = (int) firebaseRemoteConfig.getLong("floating_widgets_demo_screenshots");
                        }

                        if (InAppBilling.ItemIAB.equals(BillingManager.iapSearchEngines)) {
                            itemIABDemoDescription.setText(Html.fromHtml(firebaseRemoteConfig.getString("search_engine_description")));
                            screenshotsNumber = (int) firebaseRemoteConfig.getLong("search_engine_demo_screenshots");
                        }

                        if (InAppBilling.ItemIAB.equals(BillingManager.iapSecurityServices)) {
                            itemIABDemoDescription.setText(Html.fromHtml(firebaseRemoteConfig.getString("security_services_description")));
                            screenshotsNumber = (int) firebaseRemoteConfig.getLong("security_services_demo_screenshots");
                        }

                        String ItemIAB = "FloatingWidgets";
                        if (InAppBilling.ItemIAB.equals(BillingManager.iapFloatingWidgets)) {
                            ItemIAB = "FloatingWidgets";
                        }

                        if (InAppBilling.ItemIAB.equals(BillingManager.iapSearchEngines)) {
                            ItemIAB = "SearchEngine";
                        }

                        if (InAppBilling.ItemIAB.equals(BillingManager.iapSecurityServices)) {
                            ItemIAB = "SecurityServices";
                        }

                        for (int i = 1; i <= screenshotsNumber; i++) {
                            String sceenshotFileName = ItemIAB + "Demo" + i + ".png";
                            FirebaseStorage firebaseStorage = FirebaseStorage.getInstance();
                            StorageReference firebaseStorageReference = firebaseStorage.getReference();
                            StorageReference storageReference = firebaseStorageReference
                                    //gs://floating-shortcuts-pro.appspot.com/Assets/Images/Screenshots/[ItemIAB]/IAP.Demo/[ItemIAB] + Demo1.png
                                    .child("Assets/Images/Screenshots/" + ItemIAB + "/IAP.Demo/" + sceenshotFileName);
                            storageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri screenshotURI) {

                                    requestManager
                                            .load(screenshotURI)
                                            .diskCacheStrategy(DiskCacheStrategy.DATA)
                                            .addListener(new RequestListener<Drawable>() {
                                                @Override
                                                public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                                                    return false;
                                                }

                                                @Override
                                                public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                                                    glideLoadCounter++;

                                                    String beforeToken = screenshotURI.toString().split("\\?alt=media&token=")[0];
                                                    int drawableIndex = Integer.parseInt(String.valueOf(beforeToken.charAt(beforeToken.length() - 5)));

                                                    mapIndexDrawable.put(drawableIndex, resource);
                                                    mapIndexURI.put(drawableIndex, screenshotURI);

                                                    if (screenshotsNumber == glideLoadCounter) {
                                                        activity.runOnUiThread(new Runnable() {
                                                            @Override
                                                            public void run() {
                                                                new Handler().postDelayed(new Runnable() {
                                                                    @Override
                                                                    public void run() {
                                                                        context.sendBroadcast(new Intent("LOAD_SCREENSHOTS"));
                                                                    }
                                                                }, 113);
                                                            }
                                                        });
                                                    }

                                                    return false;
                                                }
                                            })
                                            .submit();
                                }
                            });
                        }
                    }
                });
            }
        }

        materialButtonShare.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String shareText =
                        getString(R.string.shareTitle) +
                                "\n" + getString(R.string.shareSummary) +
                                "\n" + getString(R.string.play_store_link) + getContext().getPackageName();

                Intent sharingIntent = new Intent(Intent.ACTION_SEND);
                sharingIntent.putExtra(Intent.EXTRA_TEXT, shareText);
                sharingIntent.setType("text/plain");
                sharingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(sharingIntent);
            }
        });

        getDialog().setOnKeyListener(new DialogInterface.OnKeyListener() {
            @Override
            public boolean onKey(DialogInterface dialog, int keyCode, android.view.KeyEvent event) {
                if ((keyCode == android.view.KeyEvent.KEYCODE_BACK)) {
                    requestManager.pauseAllRequests();
                    activity.finish();
                }
                return true;
            }
        });

        return root;
    }

    @Override
    public void onResume() {
        super.onResume();
        try {
            requestManager.resumeRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            requestManager.pauseAllRequests();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onClick(View view) {
        if (view instanceof ImageView) {
            String screenshotURI = view.getTag().toString();
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setData(Uri.parse(screenshotURI));
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    public void refreshUI() {
        if (skusAdapter != null) {
            skusAdapter.notifyDataSetChanged();
        }
    }

    public void onManagerReady(BillingProvider billingProvider) {
        this.billingProvider = billingProvider;
        if (recyclerView != null) {
            skusAdapter = new SkusAdapter(this.billingProvider, activity);
            if (recyclerView.getAdapter() == null) {
                recyclerView.setAdapter(skusAdapter);
                recyclerView.setLayoutManager(new LinearLayoutManager(context, RecyclerView.VERTICAL, false));
            }
            handleManagerAndUiReady();
        }
    }

    private void handleManagerAndUiReady() {
        itemIABDemoList.setVisibility(View.VISIBLE);
        itemIABDemo.setVisibility(View.VISIBLE);
        itemIABDemoDescription.setVisibility(View.VISIBLE);

        List<String> inAppSkus = billingProvider.getBillingManager().getSkus(BillingClient.SkuType.INAPP);
        billingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.INAPP,
                inAppSkus,
                new SkuDetailsResponseListener() {
                    @Override
                    public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsListInApp) {

                        if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK && skuDetailsListInApp != null) {
                            List<SkuRowData> skuRowDataList = new ArrayList<>();

                            for (SkuDetails skuDetails : skuDetailsListInApp) {
                                FunctionsClassDebug.Companion.PrintDebug("*** SKU List Product ::: " + skuDetails + " ***");

                                if (skuDetails.getSku().equals(BillingManager.iapFloatingWidgets) && functionsClass.floatingWidgetsPurchased()) {

                                    continue;
                                }

                                skuRowDataList.add(new SkuRowData(
                                        skuDetails,
                                        skuDetails.getSku(),
                                        skuDetails.getTitle(),
                                        skuDetails.getPrice(),
                                        skuDetails.getDescription(),
                                        skuDetails.getType())
                                );
                            }
                            if (skuRowDataList.size() == 0) {
                                displayError();
                            } else {
                                skusAdapter.updateData(skuRowDataList);

                                progressBar.setVisibility(View.INVISIBLE);
                            }

                            List<String> subsSkus = billingProvider.getBillingManager().getSkus(BillingClient.SkuType.SUBS);
                            billingProvider.getBillingManager().querySkuDetailsAsync(BillingClient.SkuType.SUBS,
                                    subsSkus,
                                    new SkuDetailsResponseListener() {

                                        @Override
                                        public void onSkuDetailsResponse(BillingResult billingResult, List<SkuDetails> skuDetailsListSubs) {
                                            if (billingResult.getResponseCode() == BillingClient.BillingResponseCode.OK) {
                                                for (SkuDetails skuDetails : skuDetailsListSubs) {
                                                    FunctionsClassDebug.Companion.PrintDebug("*** SKU List Subscriptions ::: " + skuDetails + " ***");

                                                    if (skuDetails.getSku().equals(BillingManager.iapSecurityServices) && functionsClass.securityServicesSubscribed()) {

                                                        continue;
                                                    }

                                                    if (skuDetails.getSku().equals(BillingManager.iapSearchEngines) && functionsClass.searchEngineSubscribed()) {

                                                        continue;
                                                    }

                                                    skuRowDataList.add(new SkuRowData(
                                                            skuDetails,
                                                            skuDetails.getSku(),
                                                            skuDetails.getTitle(),
                                                            skuDetails.getIntroductoryPrice(),
                                                            skuDetails.getDescription(),
                                                            skuDetails.getType())
                                                    );
                                                }

                                                if (skuRowDataList.size() == 0) {
                                                    displayError();
                                                } else {
                                                    skusAdapter.updateData(skuRowDataList);
                                                }
                                            }
                                        }
                                    });
                        }
                    }
                });
    }

    private void displayError() {
        Toast.makeText(context, getString(R.string.error), Toast.LENGTH_LONG).show();
    }
}

