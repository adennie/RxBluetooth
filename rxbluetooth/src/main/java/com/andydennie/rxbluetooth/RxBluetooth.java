package com.andydennie.rxbluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Build;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;

import com.plackemacher.rxreceiver.RxReceiver;

import java.lang.reflect.Method;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func0;
import rx.functions.Func1;

public final class RxBluetooth {

    private static final BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

    /**
     * Enables Bluetooth (asynchronously) and returns an Observable that emits updates to the Bluetooth state.  The
     * emitted values will be one of {@link BluetoothAdapter#STATE_OFF}, {@link BluetoothAdapter#STATE_ON},
     * or {@link BluetoothAdapter#STATE_TURNING_ON}.  The stream will terminate when the state changes to either
     * STATE_ON or STATE_OFF.
     *
     * The returned Observable will emit an error if Bluetooth is already enabled or cannot be enabled.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<Integer> enableBluetooth(final Context context) {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                if (bluetoothAdapter.isEnabled()) {
                    return Observable.error(new IllegalStateException("bluetooth is already enabled"));
                }

                if (!bluetoothAdapter.enable()) {
                    return Observable.error(new IllegalStateException("cannot enable bluetooth (maybe airplane mode " +
                            "is on?"));
                } else {
                    return observeBluetoothState(context)
                            .takeUntil(new Func1<Integer, Boolean>() {
                                @Override
                                public Boolean call(Integer state) {
                                    return state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF;
                                }
                            });
                }
            }
        });
    }

    /**
     * Disables Bluetooth (asynchronously) and returns an Observable that emits updates to the Bluetooth state.  The
     * emitted values will be one of {@link BluetoothAdapter#STATE_OFF}, {@link BluetoothAdapter#STATE_ON}, or
     * {@link BluetoothAdapter#STATE_TURNING_OFF}.  The stream will terminate when the state changes to either
     * STATE_ON or STATE_OFF.
     *
     * The returned Observable will emit an error if Bluetooth is already disabled or cannot be disabled.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<Integer> disableBluetooth(final Context context) {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                if (!bluetoothAdapter.isEnabled()) {
                    return Observable.error(new IllegalStateException("bluetooth is already disabled"));
                }

                if (!bluetoothAdapter.disable()) {
                    return Observable.error(new IllegalStateException("cannot disable bluetooth"));
                } else {
                    return observeBluetoothState(context)
                            .takeUntil(new Func1<Integer, Boolean>() {
                                @Override
                                public Boolean call(Integer state) {
                                    return state == BluetoothAdapter.STATE_ON || state == BluetoothAdapter.STATE_OFF;
                                }
                            });
                }
            }
        });
    }

    /**
     * Returns an Observable that emits changes to the Bluetooth state.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<Integer> observeBluetoothState(@NonNull Context context) {
        return RxReceiver.fromBroadcast(context, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED))
                .map(new Func1<Intent, Integer>() {
                    @Override
                    public Integer call(Intent intent) {
                        return intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, 0);
                    }
                });
    }

    /**
     * Returns an Observable that emits Bluetooth state changes to BluetoothAdapter.STATE_ON and STATE_OFF,
     * (only), filtering out redundant changes (i.e. STATE_ON->STATE_ON or STATE_OFF->STATE_OFF)
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<Integer> observeBluetoothStateOnOff(@NonNull Context context) {
        return observeBluetoothState(context)
                .filter(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer state) {
                        return state == BluetoothAdapter.STATE_OFF || state == BluetoothAdapter.STATE_ON;
                    }
                })
                .distinctUntilChanged();
    }

    /**
     * Returns an Observable that emits a BluetoothDevice whenever one is connected.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<BluetoothDevice> observeBluetoothConnections(@NonNull Context context) {
        return RxReceiver.fromBroadcast(context, new IntentFilter(BluetoothDevice.ACTION_ACL_CONNECTED))
                .map(new Func1<Intent, BluetoothDevice>() {
                    @Override
                    public BluetoothDevice call(Intent intent) {
                        return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    }
                });
    }

    /**
     * Returns an Observable that emits a BluetoothDevice whenever one is disconnected.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<BluetoothDevice> observeBluetoothDisconnections(@NonNull Context context) {
        return RxReceiver.fromBroadcast(context, new IntentFilter(BluetoothDevice.ACTION_ACL_DISCONNECTED))
                .map(new Func1<Intent, BluetoothDevice>() {
                    @Override
                    public BluetoothDevice call(Intent intent) {
                        return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    }
                });
    }

    /**
     * Starts the Bluetooth device discovery process and returns an Observable that emits
     * BluetoothDevices until discovery ends, and then completes.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<BluetoothDevice> discoverDevices(@NonNull final Context context) {

        return Observable.create(new Observable.OnSubscribe<BluetoothDevice>() {
            @Override
            public void call(final Subscriber<? super BluetoothDevice> subscriber) {
                observeDiscoveredDevices(context)
                        .takeUntil(observeDeviceDiscoveryFinish(context))
                        .subscribe(subscriber);
                bluetoothAdapter.startDiscovery();
            }
        });
    }

    /**
     * Returns an Observable that emits the first BluetoothAdapter.ACTION_DISCOVERY_STARTED that is broadcast after
     * subscription, followed by the ensuing BluetoothAdapter.ACTION_DISCOVERY_FINISHED, and then completes.  Note
     * that is this observable should be subscribed prior to initiating discovery, otherwise the
     * ACTION_DISCOVERY_STARTED broadcast may be missed, causing this Observable to wait in vain for it.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<String> observeDeviceDiscovery(@NonNull final Context context) {
        return observeDeviceDiscoveryStatus(context)
                .skipWhile(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    }
                })
                .takeUntil(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    }
                });
    }

    /**
     * Returns an Observable that emits the first BluetoothAdapter.ACTION_DISCOVERY_FINISHED that is broadcast
     * following its subscrition, and then completes.  Note that is this observable should be subscribed prior to
     * initiating discovery, otherwise the ACTION_DISCOVERY_FINISHED broadcast may be missed, causing this Observable
     * to wait in vain for it.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<String> observeDeviceDiscoveryFinish(@NonNull final Context context) {
        return observeDeviceDiscoveryStatus(context)
                .takeFirst(new Func1<String, Boolean>() {
                    @Override
                    public Boolean call(String s) {
                        return s.equals(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
                    }
                });
    }

    /**
     * Returns an Observable that emits BluetoothAdapter.ACTION_DISCOVERY_STARTED and
     * BluetoothAdapter.ACTION_DISCOVERY_FINISHED whenever discovery starts and ends. Note that this observable
     * emits these actions indefinitely -- the stream does not complete after an
     * ACTION_DISCOVERY_FINISHED.
     *
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<String> observeDeviceDiscoveryStatus(@NonNull final Context context) {
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);
        filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
        return RxReceiver.fromBroadcast(context, filter)
                .map(new Func1<Intent, String>() {
                    @Override
                    public String call(Intent intent) {
                        return intent.getAction();
                    }
                });
    }

    /**
     * Returns an Observable that emits discovered BluetoothDevices, indefinitely.
     * @param context an Android Context
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<BluetoothDevice> observeDiscoveredDevices(@NonNull Context context) {
        return RxReceiver.fromBroadcast(context, new IntentFilter(BluetoothDevice.ACTION_FOUND))
                .map(new Func1<Intent, BluetoothDevice>() {
                    @Override
                    public BluetoothDevice call(Intent intent) {
                        return intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    }
                })
                .distinct(new Func1<BluetoothDevice, String>() {
                    @Override
                    public String call(BluetoothDevice device) {
                        return device.getAddress();
                    }
                });
    }

    /**
     * Initiates and monitors bonding (pairing) of the specified BluetoothDevice and returns an Observable that emits
     * BluetoothDevice.BOND_BONDING, followed by either BluetoothDevice.BOND_BONDED or BluetoothDevice.BOND_NONE, and
     * then completes.  If the specified BluetoothDevice is already bonded or in the process of bonding, the
     * Observer's onError method is invoked.
     *
     * @param context an Android Context
     * @param device  the BluetoothDevice
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<Integer> bondDevice(@NonNull final Context context, @NonNull final BluetoothDevice device) {
        return Observable.create(new Observable.OnSubscribe<Integer>() {
            @Override
            public void call(Subscriber<? super Integer> subscriber) {
                if (device.getBondState() == BluetoothDevice.BOND_BONDED) {
                    subscriber.onError(new IllegalStateException("device is already bonded"));
                }

                if (device.getBondState() == BluetoothDevice.BOND_BONDING) {
                    subscriber.onError(new IllegalStateException("device is already in the process of bonding"));
                }

                observeDeviceBonding(context, device).subscribe(subscriber);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    device.createBond();
                } else {
                    try {
                        Method method = device.getClass().getMethod("createBond", (Class[]) null);
                        method.invoke(device, (Object[]) null);
                    } catch (Exception e) {
                        subscriber.onError(e);
                    }
                }
            }
        });
    }

    /**
     * Returns an Observable that emits the first BluetoothDevice.BOND_BONDING that is broadcast after
     * subscription, followed by the ensuing BluetoothDevice.BOND_BONDED or BluetoothDevice.BOND_NONE, and then
     * completes. Note that is this Observable should be subscribed prior to initiating the bonding process,
     * otherwise the BOND_BONDING broadcast may be missed, causing this Observable to wait in vain for it.
     *
     * @param context      an Android Context
     * @param deviceToBond the BluetoothDevice whose bonding is to be monitored
     * @return the Observable
     */
    @CheckResult
    @NonNull
    public static Observable<Integer> observeDeviceBonding(@NonNull final Context context,
                                                           @NonNull final BluetoothDevice deviceToBond) {
        return RxReceiver.fromBroadcast(context, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED))
                .filter(new Func1<Intent, Boolean>() {
                    @Override
                    public Boolean call(Intent intent) {
                        BluetoothDevice bondingDevice = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                        return bondingDevice.equals(deviceToBond);
                    }
                })
                .map(new Func1<Intent, Integer>() {
                    @Override
                    public Integer call(Intent intent) {
                        return intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, -1);
                    }
                })
                .skipWhile(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer state) {
                        return state != BluetoothDevice.BOND_BONDING;
                    }
                })
                .takeUntil(new Func1<Integer, Boolean>() {
                    @Override
                    public Boolean call(Integer state) {
                        return state == BluetoothDevice.BOND_BONDED || state == BluetoothDevice.BOND_NONE;
                    }
                });
    }
}
