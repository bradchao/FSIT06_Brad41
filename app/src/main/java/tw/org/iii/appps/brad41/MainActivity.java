package tw.org.iii.appps.brad41;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ListView;
import android.widget.SimpleAdapter;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Set;

public class MainActivity extends AppCompatActivity {
    private BluetoothAdapter bluetoothAdapter;
    private LinkedList<HashMap<String,String>> devices = new LinkedList<>();
    private ListView listDevices;
    private SimpleAdapter adapter;
    private String[] from = {"name", "mac"};
    private int[] to = {R.id.item_name, R.id.item_mac};

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            try {
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    String deviceName = device.getName();
                    String deviceHardwareAddress = device.getAddress(); // MAC address
                    Log.v("brad", deviceName);

                    HashMap<String,String> deviceData = new HashMap<>();
                    deviceData.put("name", deviceName);
                    deviceData.put("mac", deviceHardwareAddress);

                    boolean isRepeat = false;
                    for (HashMap<String,String> dd : devices){
                        if (dd.get("mac").equals(deviceHardwareAddress)){
                            Log.v("brad", "device dup");
                            isRepeat = true;
                            break;
                        }
                    }

                    if (!isRepeat) {
                        Log.v("brad", "new device add");
                        devices.add(deviceData);
                        adapter.notifyDataSetChanged();
                    }

                }
            }catch (Exception e){

            }
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    123);
        }else{
            init();
        }

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        init();
    }

    private void  init(){
        listDevices = findViewById(R.id.listDevices);
        adapter = new SimpleAdapter(this,devices, R.layout.item_device,from,to);
        listDevices.setAdapter(adapter);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableBtIntent, 123);
        }else{
            regReceiver();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 123 && resultCode == RESULT_OK){
            regReceiver();
        }
    }

    private void regReceiver(){
        IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
        registerReceiver(broadcastReceiver, filter);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(broadcastReceiver);
    }

    public void test1(View view) {
        Set<BluetoothDevice> pairedDevices =
                bluetoothAdapter.getBondedDevices();

        if (pairedDevices.size() > 0) {
            // There are paired devices. Get the name and address of each paired device.
            for (BluetoothDevice device : pairedDevices) {
                String deviceName = device.getName();
                String deviceHardwareAddress = device.getAddress(); // MAC address

                Log.v("brad", deviceName + ":" + deviceHardwareAddress);

            }
        }
    }

    // Start Scanning
    public void test2(View view){
        if (!bluetoothAdapter.isDiscovering()) {
            devices.clear();
            bluetoothAdapter.startDiscovery();
        }
    }

    // discoverable
    public void test4(View view){
        Intent discoverableIntent =
                new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
        discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
        startActivity(discoverableIntent);
    }


    // Stop Scanning
    public void test3(View view){
        if (bluetoothAdapter.isDiscovering()){
            Log.v("brad", "cancel scanning...");
            bluetoothAdapter.cancelDiscovery();
        }
    }

}
