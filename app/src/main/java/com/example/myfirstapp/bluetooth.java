package com.example.myfirstapp;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.content.Intent;
import android.bluetooth.BluetoothSocket;
import android.bluetooth.BluetoothServerSocket;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.regex.Pattern;
import static com.example.myfirstapp.R.*;


public class bluetooth extends AppCompatActivity implements View.OnClickListener{
    List<String> data;
    List<String> addrs;
    private BluetoothSocket clientSocket;
    private BluetoothSocket Socket;
    private BluetoothServerSocket serverSocket;
    private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private OutputStream outStream = null;
    private InputStream inStream = null;
    private Handler handler = new Handler();

    String ReceiveData="";
    String ReceiveData1="";
    String ReceiveData2="";

    BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(layout.activity_bluetooth);
        Button button2;
        button2 = findViewById(id.btn_open);
        button2.setOnClickListener(this);
        Button button3;
        button3 = findViewById(id.btn_close);
        button3.setOnClickListener(this);
        Button button4;
        button4 = (Button)findViewById(id.btn_search);
        button4.setOnClickListener(this);
        TextView lblTitle=(TextView)findViewById(id.DataShow);
        lblTitle.setText("这是显示的内容");
        lblTitle.setText(null);
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
                bluetooth.this, android.R.layout.simple_list_item_1,data);
        final ListView listView = (ListView) findViewById(id.DeviceList);
        listView.setAdapter(adapter1);
        adapter1.notifyDataSetChanged();
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                adapter.cancelDiscovery();
                //Toast.makeText(bluetooth.this, "stop Discovery", Toast.LENGTH_SHORT).show();
                Toast.makeText(bluetooth.this,"Connect to: " + addrs.get(i), Toast.LENGTH_SHORT).show();
                BluetoothDevice btDev = adapter.getRemoteDevice(addrs.get(i));
                try {
                    //这里是调用的方法，此方法使用反射,后面解释
                    createBond(btDev.getClass(), btDev);
                } catch (Exception e) {
                    e.printStackTrace();
                }
                try {
                    Socket = btDev.createRfcommSocketToServiceRecord(MY_UUID);
                    Socket.connect();
                    Log.e("error", "ON RESUME: BT connection established, data transfer link open.");
                } catch (IOException e) {
                    try {
                        Socket.close();
                    } catch (IOException e2) {
                        Log .e("error","ON RESUME: Unable to close socket during connection failure", e2);
                    }

                }
                try {
                    outStream = Socket.getOutputStream();
                } catch (IOException e) {
                    Log.e("error", "ON RESUME: Output stream creation failed.", e);
                }
                Toast.makeText(bluetooth.this,"Connect OK", Toast.LENGTH_SHORT).show();
                //handler.postDelayed(task,2);//延迟调用
                data.clear();
                ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
                        bluetooth.this, android.R.layout.simple_list_item_1,data);
                final ListView listView = (ListView) findViewById(id.DeviceList);
                listView.setAdapter(adapter1);
                adapter1.notifyDataSetChanged();
                handler.post(task);
                //handler.post(task);//立即调用
            }
        });
    }
    @SuppressLint("SetTextI18n")
    private void processBuffer(byte[] buff, int size)
    {
        int length=0;
        TextView lblTitle=(TextView)findViewById(id.DataShow);
        lblTitle.clearComposingText();

        for(int i=0;i<size;i++)
        {
            if(buff[i]>'\0')
            {
                length++;
            }
            else
            {
                break;
            }
        }
        byte[] newbuff=new byte[length];  //newbuff字节数组，用于存放真正接收到的数据
        for(int j=0;j<length;j++)
        {
            newbuff[j]=buff[j];
        }
       // ReceiveData = ReceiveData + new String(newbuff);
        ReceiveData =  new String(newbuff);
        ReceiveData = ReceiveData.substring((int)ReceiveData.indexOf("HR:")+3,(int)ReceiveData.indexOf("HR:")+6);
        ReceiveData = ReceiveData.replaceAll("\n","\0");
        ReceiveData = ReceiveData.replaceAll("\r","\0");
        ReceiveData1 = new String(newbuff);
        ReceiveData1 = ReceiveData1.substring((int)ReceiveData1.indexOf("SP:")+3,(int)ReceiveData1.indexOf("SP:")+12);
        ReceiveData1 = ReceiveData1.replaceAll("\n","\0");
        ReceiveData1 = ReceiveData1.replaceAll("\r","\0");
        ReceiveData2 = new String(newbuff);
        ReceiveData2 = ReceiveData2.substring((int)ReceiveData2.indexOf("AL:")+3,(int)ReceiveData2.indexOf("AL:")+8);
        ReceiveData2 = ReceiveData2.replaceAll("\n","\0");
        ReceiveData2 = ReceiveData2.replaceAll("\r","\0");
        lblTitle.setText("心跳是:" + ReceiveData + "\r\n血氧是:" + ReceiveData1 + "\r\n酒精是:" + ReceiveData2);
        //Log.e("Data",ReceiveData);
        Message msg=Message.obtain();
        msg.what=1;
        //lblTitle.clearComposingText();
        //Toast.makeText(bluetooth.this,ReceiveData, Toast.LENGTH_SHORT).show();
    }

    //@Override
    private final BroadcastReceiver mReceiver;
    {
        mReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                String action = intent.getAction();
                // 发现设备
                if (BluetoothDevice.ACTION_FOUND.equals(action)) {
                    // 从Intent中获取设备对象
                    BluetoothDevice device = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);
                    // 将设备名称和地址放入array adapter，以便在ListView中显示
                    data.add(device.getName());
                    addrs.add(device.getAddress());
                    Toast.makeText(bluetooth.this,device.getName(), Toast.LENGTH_SHORT).show();
                    ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
                            bluetooth.this, android.R.layout.simple_list_item_1, data);
                    ListView listView = (ListView) findViewById(id.DeviceList);
                    listView.setAdapter(adapter1);
                    adapter1.notifyDataSetChanged();
                }
            }
        };
        data = new ArrayList<String>();
        addrs = new ArrayList<String>();
    }

    @Override
    public void onClick(View v) {
        ArrayAdapter<String> adapter1 = new ArrayAdapter<String>(
                bluetooth.this, android.R.layout.simple_list_item_1, data);
        ListView listView = (ListView) findViewById(id.DeviceList);
        listView.setAdapter(adapter1);
        switch (v.getId()) {
            case id.btn_open:
                Toast.makeText(bluetooth.this, "open buletooth", Toast.LENGTH_SHORT).show();
                adapter.enable(); //打开蓝牙
                data.clear();
                adapter1.notifyDataSetChanged();
                break;
            case id.btn_close:
                Toast.makeText(bluetooth.this, "close buletooth", Toast.LENGTH_SHORT).show();
                data.clear();
                adapter.disable(); //关闭蓝牙
                adapter1.notifyDataSetChanged();
                break;
            case id.btn_search:
                Toast.makeText(bluetooth.this, "search buletooth", Toast.LENGTH_SHORT).show();
                IntentFilter filter = new IntentFilter(BluetoothDevice.ACTION_FOUND);
                registerReceiver(mReceiver, filter); // Don't forget to unregister during onDestroy
                data.clear();
                addrs.clear();
                adapter.startDiscovery();
                break;
            default:
                break;
        }
    }
    public boolean createBond(Class btClass, BluetoothDevice btDevice) throws Exception {
        Method createBondMethod = btClass.getMethod("createBond");//获取蓝牙的连接方法
        Boolean returnValue = (Boolean) createBondMethod.invoke(btDevice);
        return returnValue.booleanValue();//返回连接状态
    }
    private Runnable task =new Runnable() {
        public void run() {
            handler.postDelayed(this,1500);//设置延迟时间，此处是1秒
            //需要执行的代码
            {
                //定义一个存储空间buff
                byte[] buff = new byte[1024];
                try {
                    inStream = Socket.getInputStream();
                    //System.out.println("waitting for instream");
                    inStream.read(buff); //读取数据存储在buff数组中
                    processBuffer(buff, 1024);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    };
    public static boolean isInteger(String str) {
        Pattern pattern = Pattern.compile("^[-\\+]?[\\d]*$");
        return pattern.matcher(str).matches();
    };
}
