package com.istl.pianoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class PianoModel extends AppCompatActivity  implements View.OnClickListener {

    private Button btnDo, btnRe, btnMi, btnFa, btnSol, btnLa, btnSi, btn1, btn2;

    private static final String TAG = "PianoModel";

    Handler bluetoothIn;
    final int handlerState = 0;
    private BluetoothAdapter mBtAdapter = null;
    private BluetoothSocket mBtSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private static String addres = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_piano_model);
        btnDo = (Button) findViewById(R.id.btnDo);
        btnRe = (Button) findViewById(R.id.btnRe);
        btnMi = (Button) findViewById(R.id.btnMi);
        btnFa = (Button) findViewById(R.id.btnFa);
        btnSol = (Button) findViewById(R.id.btnSol);
        btnLa = (Button) findViewById(R.id.btnLa);
        btnSi = (Button) findViewById(R.id.btnSi);
        btn1 = (Button) findViewById(R.id.btn1);
        btn2 = (Button) findViewById(R.id.btn2);

        mBtAdapter = BluetoothAdapter.getDefaultAdapter();
        checkBTState();

        btnDo.setOnClickListener(this);
        btnRe.setOnClickListener(this);
        btnMi.setOnClickListener(this);
        btnFa.setOnClickListener(this);
        btnSol.setOnClickListener(this);
        btnLa.setOnClickListener(this);
        btnSi.setOnClickListener(this);
        btn1.setOnClickListener(this);
        btn2.setOnClickListener(this);


    }

    @Override
    public void onClick(View v) {
        if (v == btnDo) {
            mConnectedThread.write(1);
        }
        if (v == btnRe) {
            mConnectedThread.write(2);
        }
        if (v == btnMi) {
            mConnectedThread.write(3);
        }
        if (v == btnFa) {
            mConnectedThread.write(4);
        }
        if (v == btnSol) {
            //mConnectedThread.write(5);
        }
        if (v == btnLa) {
            //mConnectedThread.write(6);
        }
        if (v == btnSi) {
            //mConnectedThread.write(7);
        }
    }

    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException { //puerta de conexion al blooutu
        return device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }


    public void onResume() {
        super.onResume();

        Intent intent = getIntent();

        addres = intent.getStringExtra(ListDevicesActivity.EXTRA_DEVICE_ADDRESS);

        BluetoothDevice device = mBtAdapter.getRemoteDevice(addres);
        try {
            mBtSocket = createBluetoothSocket(device);

        } catch (IOException e) {
            Toast.makeText(getBaseContext(), "La creacción del Socket fallo", Toast.LENGTH_LONG).show();
        }
        // Establece la conexión con el socket Bluetooth.
        try {
            mBtSocket.connect();
        } catch (IOException e) {
            try {
                mBtSocket.close();
            } catch (IOException e2) {
            }
        }
        mConnectedThread = new ConnectedThread(mBtSocket);
        mConnectedThread.start();
        mConnectedThread.write(0);
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            mBtSocket.close();
        } catch (IOException e2) {

        }
    }


    private void checkBTState() {
        // Check device has Bluetooth and that it is turned on
        mBtAdapter = BluetoothAdapter.getDefaultAdapter(); // CHECK THIS OUT THAT IT WORKS!!!
        if (mBtAdapter == null) {
            Toast.makeText(getBaseContext(), "No es posible activar el Bluetooth", Toast.LENGTH_LONG).show();
        } else {
            if (mBtAdapter.isEnabled()) {
                Log.d(TAG, "Bluetooth Activado");
            } else {
                //Prompt user to turn on Bluetooth
                Intent enableBtIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
                startActivityForResult(enableBtIntent, 1);

            }
        }
    }

    private class ConnectedThread extends Thread {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        public ConnectedThread(BluetoothSocket socket) {

            InputStream tmpIn = null;
            OutputStream tmpOut = null;
            try {
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) {

            }
            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        public void run() {
            byte[] buffer = new byte[256];
            int bytes;

            // Se mantiene en modo escucha para determinar el ingreso de datos
            while (true) {
                try {

                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);
                    // Envia los datos obtenidos hacia el evento via handler
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                } catch (IOException e) {
                    break;
                }
            }
        }

        //Envio de trama
        public void write(int input) {
            try {
                mmOutStream.write(input);
            } catch (IOException e) {
                //si no es posible enviar datos se cierra la conexión
                Toast.makeText(getBaseContext(), "La Conexión fallo", Toast.LENGTH_LONG).show();
                finish();
            }
        }
    }
}
