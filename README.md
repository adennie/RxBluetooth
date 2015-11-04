RxBluetooth
===========

RxJava Observables for Android Bluetooth

Quick overview:

`enableBluetooth` - enables Bluetooth and emits Bluetooth state changes until STATE_ON or STATE_OFF is reached, then completes

`disableBluetooth` - enables Bluetooth and emits Bluetooth state changes until STATE_ON or STATE_OFF is reached, then completes

`observeBluetoothState` - emits Bluetooth state changes, indefinitely

`observeBluetoothConnections` - emits BluetoothDevices as they are connected, indefinitely

`observeBluetoothDisconnections` - emits BluetoothDevices as they are disconnected, indefinitely

`discoverDevices` - initiates discovery and emits discovered BluetoothDevices until discovery finishes, then completes

`observeDeviceDiscovery` - emits BluetoothAdapter.ACTION_DISCOVERY_STARTED and ACTION_DISCOVERY_FINISHED, then completes

`observeDeviceDiscoveryFinish` - emits BluetoothAdapter.ACTION_DISCOVERY_FINISHED, then completes

`observeDeviceDiscoveryStatus` - emits BluetoothAdapter.ACTION_DISCOVERY_STARTED and ACTION_DISCOVERY_FINISHED, indefinitely

`observeDiscoveredDevices` - emits discovered BluetoothDevices, indefinitely

`bondDevice` - initiates device bonding and emits bonding status updates until BluetoothDevice.BOND_BONDED or BOND_NONE, then completes
 
`observeDeviceBonding` - emits bonding status updates until BluetoothDevice.BOND_BONDED or BOND_NONE, then completes 


