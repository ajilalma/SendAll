package com.aj.sendall.application;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.aj.sendall.db.contentprovidutil.ContentProviderUtil;
import com.aj.sendall.db.sharedprefs.SharedPrefConstants;
import com.aj.sendall.db.sharedprefs.SharedPrefUtil;
import com.aj.sendall.db.util.DBUtil;
import com.aj.sendall.network.broadcastreceiver.AbstractGroupCreationListener;
import com.aj.sendall.network.runnable.GroupCreator;
import com.aj.sendall.notification.util.NotificationUtil;

import java.io.Serializable;

import javax.inject.Inject;
import javax.inject.Singleton;

/**
 * Created by ajilal on 25/5/17.
 */

@Singleton
public class AppManager implements Serializable{
    public Context context;
    public DBUtil dbUtil;
    private boolean initialised = false;
    public WifiManager wifiManager;
    public WifiP2pManager wifiP2pManager;
    public WifiP2pManager.Channel channel;
    public Handler handler;
    public SharedPrefUtil sharedPrefUtil;
    private int wifiP2pState;
    public NotificationUtil notificationUtil;
    public ContentProviderUtil contentProviderUtil;

    @Inject
    public AppManager(Context context,
                      DBUtil dbUtil,
                      Handler groupHandler,
                      SharedPrefUtil sharedPrefUtil,
                      NotificationUtil notificationUtil,
                      ContentProviderUtil contentProviderUtil){
        this.context = context;
        this.dbUtil = dbUtil;
        this.handler = groupHandler;
        this.sharedPrefUtil = sharedPrefUtil;
        this.notificationUtil = notificationUtil;
        this.contentProviderUtil = contentProviderUtil;
        init(context);
    }

    private void init(Context context) {
        if(!initialised) {
            wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            wifiP2pManager = (WifiP2pManager) context.getSystemService(Context.WIFI_P2P_SERVICE);
            channel = wifiP2pManager.initialize(context, context.getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {

                }
            });
            initialised = true;
        }
    }

    public boolean isWifiEnabled(){
        return  wifiP2pState == WifiP2pManager.WIFI_P2P_STATE_ENABLED;
    }

    public void enableWifi(boolean enable){
        wifiManager.setWifiEnabled(enable);
    }

    /*Method to set current wifi status from wifi broadcast receiver*/
    public void setWifiP2pState(int currentWifiStatus){
        this.wifiP2pState = currentWifiStatus;
    }


    public boolean createGroupAndAdvertise(AbstractGroupCreationListener broadcastReceiver, int newAppStatus){
        //first of all, change the app status
        int appStatus = sharedPrefUtil.getCurrentAppStatus();
        if(appStatus == SharedPrefConstants.CURR_STATUS_IDLE) {
            sharedPrefUtil.setCurrentAppStatus(newAppStatus);
            sharedPrefUtil.commit();
            enableWifi(true);

            //Start the receiver to receive the group data
            IntentFilter intentFilter = new IntentFilter();
            intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);
            context.registerReceiver(broadcastReceiver, intentFilter);

            //Delay for the BroadcastReceiver to start
            handler.postDelayed(new GroupCreator(this, notificationUtil, sharedPrefUtil), 500);
            return true;
        } else {
            Toast.makeText(context, "Please wait for the current operation to finish", Toast.LENGTH_SHORT).show();
            return false;
        }
    }

    public void startP2pServiceDiscovery(final WifiP2pManager.DnsSdTxtRecordListener textListener, final WifiP2pManager.DnsSdServiceResponseListener serviceResponseListener){
        enableWifi(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        clearedLocalServices();
                    }

                    @Override
                    public void onFailure(int reason) {
                        clearedLocalServices();
                    }

                    private void clearedLocalServices(){
                        wifiP2pManager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                clearedServiceRequests();
                            }

                            @Override
                            public void onFailure(int reason) {
                                clearedServiceRequests();
                            }

                            private void clearedServiceRequests(){
                                wifiP2pManager.setDnsSdResponseListeners(channel, serviceResponseListener, textListener);

                                final WifiP2pDnsSdServiceRequest serviceRequest = WifiP2pDnsSdServiceRequest.newInstance();
                                wifiP2pManager.addServiceRequest(channel, serviceRequest, new WifiP2pManager.ActionListener() {
                                    int failureCount = 0;
                                    @Override
                                    public void onSuccess() {
                                        Log.d(AppManager.class.getSimpleName(), "Added service request");
                                        if(isWifiEnabled()) {
                                            notificationUtil.showToggleReceivingNotification();
                                        }
                                        serviceRequestAdded();
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        Log.d(AppManager.class.getSimpleName(), "Adding Service request failed");
                                        failureCount++;
                                        if(isWifiEnabled() && WifiP2pManager.BUSY == reason && failureCount < 5) {
                                            //retry after 2 seconds
                                            final WifiP2pManager.ActionListener thisListener = this;
                                            handler.postDelayed(new Runnable() {
                                                @Override
                                                public void run() {
                                                    wifiP2pManager.addServiceRequest(channel, serviceRequest, thisListener);
                                                }
                                            }, 2000);
                                        } else {
                                            Log.d(AppManager.class.getSimpleName(), "Removed service request");
                                            Toast.makeText(context, "Scanning failed", Toast.LENGTH_SHORT).show();
                                            sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                            sharedPrefUtil.commit();
                                            if(isWifiEnabled()) {
                                                notificationUtil.showToggleReceivingNotification();
                                            }
                                            serviceRequestAdded();
                                        }
                                    }

                                    private void serviceRequestAdded(){
                                        wifiP2pManager.discoverServices(channel, new WifiP2pManager.ActionListener() {
                                            int failureCount = 0;

                                            @Override
                                            public void onSuccess() {
                                                Log.d(AppManager.class.getSimpleName(), "Started service discovery");
                                                if(isWifiEnabled()) {
                                                    notificationUtil.showToggleReceivingNotification();
                                                }
                                            }

                                            @Override
                                            public void onFailure(int reason) {
                                                Log.d(AppManager.class.getSimpleName(), "Failed starting service discovery");
                                                failureCount++;
                                                if(isWifiEnabled() && WifiP2pManager.BUSY == reason && failureCount < 5) {
                                                    //retry after 2 seconds
                                                    final WifiP2pManager.ActionListener thisListener = this;
                                                    handler.postDelayed(new Runnable() {
                                                        @Override
                                                        public void run() {
                                                            wifiP2pManager.discoverServices(channel, thisListener);
                                                        }
                                                    }, 2000);
                                                } else {
                                                    Log.d(AppManager.class.getSimpleName(), "Aborted service discovery");
                                                    Toast.makeText(context, "Scanning failed", Toast.LENGTH_SHORT).show();
                                                    sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                                    sharedPrefUtil.commit();
                                                    if(isWifiEnabled()) {
                                                        notificationUtil.showToggleReceivingNotification();
                                                    }
                                                }
                                            }
                                        });
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }, 1000);

    }

    public void stopP2pServiceDiscovery(){
        wifiP2pManager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
            int failureCount = 0;
            @Override
            public void onSuccess() {
                Log.d(AppManager.class.getSimpleName(), "Stopped service discovery");
                sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                sharedPrefUtil.commit();
                if(isWifiEnabled()) {
                    notificationUtil.showToggleReceivingNotification();
                }
            }

            @Override
            public void onFailure(int reason) {
                Log.d(AppManager.class.getSimpleName(), "Failed stopping service discovery");
                failureCount++;
                if(isWifiEnabled() && WifiP2pManager.BUSY == reason && failureCount < 2) {
                    //retry after 0.5 seconds
                    final WifiP2pManager.ActionListener thisListener = this;
                    handler.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            wifiP2pManager.clearServiceRequests(channel, thisListener);
                        }
                    }, 500);
                } else {
                    Log.d(AppManager.class.getSimpleName(), "Stopped service discovery");
                    sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                    sharedPrefUtil.commit();
                    if(isWifiEnabled()) {
                        notificationUtil.showToggleReceivingNotification();
                    }
                }
            }
        });
    }

    public void stopAllWifiOps(){
        final boolean wasWifiEnabled = isWifiEnabled();
        enableWifi(true);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                wifiP2pManager.clearLocalServices(channel, new WifiP2pManager.ActionListener() {
                    @Override
                    public void onSuccess() {
                        removedLocalServices();
                    }

                    @Override
                    public void onFailure(int reason) {
                        removedLocalServices();
                    }

                    private void removedLocalServices(){
                        wifiP2pManager.clearServiceRequests(channel, new WifiP2pManager.ActionListener() {
                            @Override
                            public void onSuccess() {
                                clearedServiceRequests();
                            }

                            @Override
                            public void onFailure(int reason) {
                                clearedServiceRequests();
                            }

                            private void clearedServiceRequests(){
                                wifiP2pManager.removeGroup(channel, new WifiP2pManager.ActionListener() {
                                    @Override
                                    public void onSuccess() {
                                        groupRemoved();
                                    }

                                    @Override
                                    public void onFailure(int reason) {
                                        groupRemoved();
                                    }

                                    private void groupRemoved(){
                                        if(!wasWifiEnabled){
                                            enableWifi(false);
                                        }
                                        sharedPrefUtil.setCurrentAppStatus(SharedPrefConstants.CURR_STATUS_IDLE);
                                    }
                                });
                            }
                        });
                    }
                });
            }
        }, 1000);
    }
}
