package com.example.administrator.bluetooth;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Set;
import java.util.UUID;

import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * <p>
 * Created by 火龙裸 on 2017/9/1.
 */

public class Printer extends AppCompatActivity implements View.OnClickListener
{
    private static final String TAG = "Printer";
    @BindView(R.id.btn_printer)
    Button btn_printer;

    private boolean isConnectPrint; //布尔值是为了区分，判断是否配对了“内置打印机”这个设备
    private String deviceAddress;

    private Set<BluetoothDevice> pairedDevices;

    private BluetoothAdapter bluetoothAdapter;

    private static BluetoothSocket bluetoothSocket = null;
    private OutputStream outputStream = null;
    private static final UUID uuid = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
    private int REQUEST_ENABLE_BT = 100;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_printer);
        ButterKnife.bind(this);

        bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

        btn_printer.setOnClickListener(this);
    }


    @Override
    public void onClick(View view)
    {
        switch (view.getId())
        {
            case R.id.btn_printer:
                //判断蓝牙是否打开，没有打开则提示用户打开
                if (!bluetoothAdapter.isEnabled())
                {
                    Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                    startActivityForResult(enableBtIntent, REQUEST_ENABLE_BT);
                }
                else
                {
                    //去扫描
                    toScann();
                }
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK)
        {
            if (requestCode == REQUEST_ENABLE_BT)
            {
                //去扫描
                toScann();
            }
        }

    }

    private void toScann()
    {
        //获取“已配对”设备的集合
        pairedDevices = bluetoothAdapter.getBondedDevices();


        if (pairedDevices.size() > 0)
        {
            Log.e(TAG, "扫描结果: 设备数量========================" + pairedDevices.size());

            for (BluetoothDevice device : pairedDevices)
            {
                if (TextUtils.equals("内置打印机", device.getName()))
                {
                    deviceAddress = device.getAddress();
                    isConnectPrint = true;
                    break;
                }
                else
                {
                    isConnectPrint = false;
                }
            }

            if (isConnectPrint)//布尔值是为了区分，判断是否配对了“内置打印机”这个设备
            {
                toPrint(deviceAddress);
            }
            else
            {
                Toast.makeText(this, "请前往设置页将蓝牙配对为“内置打印机”", Toast.LENGTH_SHORT).show();
            }
        }
        else
        {
            Log.e(TAG, "xxxxxxxxxxx: 没有可用设备========================");
        }
    }

    private void toPrint(String deviceAddress)
    {
        if (isConnectPrint)
        {
            Log.e(TAG, "onConnectSuccess: >>>>>>>>开始打印");

            try
            {
                bluetoothSocket = bluetoothAdapter.getRemoteDevice(deviceAddress).createRfcommSocketToServiceRecord(uuid);
                bluetoothSocket.connect();
                outputStream = bluetoothSocket.getOutputStream();

                PrintUtils.setOutputStream(outputStream);
                PrintUtils.selectCommand(PrintUtils.RESET);
                PrintUtils.selectCommand(PrintUtils.LINE_SPACING_DEFAULT);
                PrintUtils.selectCommand(PrintUtils.ALIGN_CENTER);
                PrintUtils.printText("美食餐厅\n\n");
                PrintUtils.selectCommand(PrintUtils.DOUBLE_HEIGHT_WIDTH);
                PrintUtils.printText("桌号：1号桌\n\n");
                PrintUtils.selectCommand(PrintUtils.NORMAL);
                PrintUtils.selectCommand(PrintUtils.ALIGN_LEFT);
                PrintUtils.printText(PrintUtils.printTwoData("订单编号", "201507161515\n"));
                PrintUtils.printText(PrintUtils.printTwoData("点菜时间", "2016-02-16 10:46\n"));
                PrintUtils.printText(PrintUtils.printTwoData("上菜时间", "2016-02-16 11:46\n"));
                PrintUtils.printText(PrintUtils.printTwoData("人数：2人", "收银员：张三\n"));

                PrintUtils.printText("--------------------------------\n");
                PrintUtils.selectCommand(PrintUtils.BOLD);
                PrintUtils.printText(PrintUtils.printThreeData("项目", "数量", "金额\n"));
                PrintUtils.printText("--------------------------------\n");
                PrintUtils.selectCommand(PrintUtils.BOLD_CANCEL);
                PrintUtils.printText(PrintUtils.printThreeData("面", "1", "0.00\n"));
                PrintUtils.printText(PrintUtils.printThreeData("米饭", "1", "6.00\n"));
                PrintUtils.printText(PrintUtils.printThreeData("铁板烧", "1", "26.00\n"));
                PrintUtils.printText(PrintUtils.printThreeData("一个测试", "1", "226.00\n"));
                PrintUtils.printText(PrintUtils.printThreeData("牛肉面啊啊", "1", "2226.00\n"));
                PrintUtils.printText(PrintUtils.printThreeData("牛肉面啊啊啊牛肉面啊啊啊", "888", "98886.00\n"));

                PrintUtils.printText("--------------------------------\n");
                PrintUtils.printText(PrintUtils.printTwoData("合计", "53.50\n"));
                PrintUtils.printText(PrintUtils.printTwoData("抹零", "3.50\n"));
                PrintUtils.printText("--------------------------------\n");
                PrintUtils.printText(PrintUtils.printTwoData("应收", "50.00\n"));
                PrintUtils.printText("--------------------------------\n");

                PrintUtils.selectCommand(PrintUtils.ALIGN_LEFT);
                PrintUtils.printText("备注：不要辣、不要香菜");
                PrintUtils.printText("\n\n\n\n\n");
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }
    }

    /**
     * 断开蓝牙设备连接
     */
    public void disconnect()
    {
        System.out.println("断开蓝牙设备连接");
        Log.e(TAG, "disconnect: 断开蓝牙设备连接>>>>>>>>>>>>>>>>>>>>");
        try
        {
            bluetoothSocket.close();
            outputStream.close();
        }
        catch (IOException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        disconnect();
    }
}
