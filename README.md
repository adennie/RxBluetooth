RxBluetooth
===========

RxJava Observables for Android Bluetooth

Quick overview:

method|description
---|---
`enableBluetooth` | if Bluetooth is in a state in which it can be enabled, enables it and emits `BluetoothAdapter.STATE_TURNING_ON` followed by either `STATE_ON` or `STATE_OFF`, then completes.  If Bluetooth is not in a state where it can be enabled, invokes the Subscriber's `onError`.
`disableBluetooth` | if Bluetooth is in a state in which it can be disabled, disables it  and emits `Bluetooth.STATE_TURNING_OFF` followed by either `STATE_ON` or `STATE_OFF`, then completes.  If Bluetooth is not in a state where it can be disabled, invokes the Subscriber's `onError`.
`observeBluetoothState` | emits Bluetooth state changes, indefinitely
`observeBluetoothStateOnOff` | emits Bluetooth state changes to `BluetoothAdapter.STATE_ON` or `STATE_OFF` (only), filtering out redundant changes (i.e. `STATE_ON`->`STATE_ON` or `STATE_OFF`->`STATE_OFF`), indefinitely
`observeBluetoothConnections` | emits BluetoothDevices as they are connected, indefinitely
`observeBluetoothDisconnections` | emits BluetoothDevices as they are disconnected, indefinitely
`discoverDevices` | initiates discovery and emits discovered BluetoothDevices until discovery finishes, then completes
`observeDeviceDiscovery` | emits Bluetooth discovery status updates until `BluetoothAdapter.ACTION_DISCOVERY_FINISHED`, then completes
`observeDeviceDiscoveryFinish` | emits the next `BluetoothAdapter.ACTION_DISCOVERY_FINISHED`, then completes
`observeDeviceDiscoveryStatus` | emits `BluetoothAdapter.ACTION_DISCOVERY_STARTED` and `ACTION_DISCOVERY_FINISHED`, indefinitely
`observeDiscoveredDevices` | emits discovered BluetoothDevices, indefinitely
`bondDevice` | initiates device bonding and emits `BluetoothDevice.BOND_BONDING` followed by `BOND_BONDED` or `BOND_NONE`, then completes
`observeDeviceBonding` | emits bonding status updates until `BluetoothDevice.BOND_BONDED` or `BOND_NONE`, then completes 


