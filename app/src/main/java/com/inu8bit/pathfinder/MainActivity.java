package com.inu8bit.pathfinder;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.os.StrictMode;
import android.speech.tts.TextToSpeech;
import android.speech.tts.TextToSpeech.OnInitListener;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.Socket;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.UUID;


// TODO: Make Activity interface (for example, TTS Service must stop onDestroy and so on)
// TODO: All method should be called by interface-form
// TODO: RFID Activity

public class MainActivity extends AppCompatActivity {

    private BluetoothService bluetoothService;
    private boolean isfirst = true;
    private ImageView imageView;

    //private Navigation navi;

    GestureDetector detector;

    private static final int SWIPE_MIN_DISTANCE = 120;
    private static final int SWIPE_MAX_OFF_PATH = 250;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    String fullName = "";

    private final String DEVICE_ADDRESS = "00:21:13:00:E1:2D";//재현
    //private final String DEVICE_ADDRESS="00:21:13:00:F5:F4";


    private final UUID PORT_UUID = UUID.fromString("00001101-0000-1000-8000-00805f9b34fb");//Serial Port Service ID
    private BluetoothDevice device;
    private BluetoothSocket socket;
    private OutputStream outputStream;
    private InputStream inputStream;

    /////////////////서버 연결에 필요한 변수들////////////////
    Socket serverSocket;  //소켓생성
    OutputStream os;
    InputStream is;
    Protocol protocol;
    byte[] buf;
    Thread worker; // 소켓 연결 쓰레드
    ///////////////////////////////////////////////////////////

    boolean deviceConnected = false;
    byte buffer[];
    boolean stopThread;

    private TTSManager ttsManager = null;

    private BluetoothAdapter.LeScanCallback mLeScanCallback = new BluetoothAdapter.LeScanCallback() {
        // TODO: LeScanCallback and LeStanStart were deprecated as of when Lollipop launched. Find replacement.
        @Override
        public void onLeScan(final BluetoothDevice device, int rssi, byte[] scanRecord) {
            Log.d("BLUETOOTH", "BLE device found: " + device.getName() + "; MAC " + device.getAddress());
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON); //display on
        setContentView(R.layout.activity_main);

        if (BTinit()) {
            if (BTconnect()) {
                //setUiEnabled(true);
                deviceConnected = true;
                beginListenForData();
                //textView.append("\nConnection Opened!\n");
                Toast.makeText(getApplicationContext(), "Connection Opened!", Toast.LENGTH_SHORT).show();
            }

        }
        setContentView(R.layout.activity_main);

        //////////////////////서버 연결/////////////////////////
        StrictMode.enableDefaults(); //이게 있어야 밑에 쓰레드에서 오류나는걸 무시해줌
        worker = new Thread() {    //worker 를 Thread 로 생성
            public void run() { //스레드 실행구문
                try {
                    //소켓을 생성하고 입출력 스트립을 소켓에 연결한다.
                    Log.e("socket", "connecting...");
                    serverSocket = new Socket("118.47.174.73", 3000); //소켓생성
                    Log.e("socket", "connected");
                    os = serverSocket.getOutputStream();
                    is = serverSocket.getInputStream();
                    protocol = new Protocol();
                    buf = protocol.getPacket();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };
        worker.start();  //onResume()에서 실행.
        ////////////////////////////////////////////////////////////

        ttsManager = new TTSManager();
        ttsManager.init(this);

        if (isfirst) {
            isfirst = false;
            Handler handler = new Handler();
            handler.postDelayed(new Runnable() {
                public void run() {
                    ttsManager.initQueue("안녕하세요. Pathfinder 도우미입니다.");
                    ttsManager.addQueue("메뉴는 다음과 같습니다.");
                    ttsManager.addQueue("오른쪽, 경로 검색");
                    ttsManager.addQueue("위쪽, 주변 정보");
                    ttsManager.addQueue("아래쪽, 주변 정류장 검색");
                }
            }, 1000);
        }

        imageView = findViewById(R.id.imageView);
        imageView.setOnTouchListener(new SwipeListener(getApplicationContext()) {
            @Override
            public void onLeft() {
//                Intent RFIDIntent = new Intent(getApplicationContext(), RFIDActivity.class);
                //              startActivity(RFIDIntent);
            }

            public void onRight() {
                ttsManager.stop();
                Intent RouteIntent = new Intent(getApplicationContext(), RouteActivity.class);
                startActivity(RouteIntent);
            }

            public void onTop() {
                ttsManager.stop();
                Intent InfoIntent = new Intent(getApplicationContext(), InfoActivity.class);
                startActivity(InfoIntent);
            }

            public void onBottom() {
                ttsManager.stop();
                Intent BusIntent = new Intent(getApplicationContext(), BusActivity.class);
                startActivity(BusIntent);
            }
        });


    }

    public boolean BTinit() {
        boolean found = false;
        BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        if (bluetoothAdapter == null) {
            Toast.makeText(getApplicationContext(), "Device doesnt Support Bluetooth", Toast.LENGTH_SHORT).show();
        }
        if (!bluetoothAdapter.isEnabled()) {
            Intent enableAdapter = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(enableAdapter, 2);
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        Set<BluetoothDevice> bondedDevices = bluetoothAdapter.getBondedDevices();
        if (bondedDevices.isEmpty()) {
            Toast.makeText(getApplicationContext(), "Please Pair the Device first", Toast.LENGTH_SHORT).show();
        } else {
            for (BluetoothDevice iterator : bondedDevices) {
                if (iterator.getAddress().equals(DEVICE_ADDRESS)) {
                    device = iterator;
                    found = true;
                    break;
                }
            }
        }
        return found;
    }

    public boolean BTconnect() {
        boolean connected = true;
        try {
            socket = device.createRfcommSocketToServiceRecord(PORT_UUID);
            socket.connect();
        } catch (IOException e) {
            e.printStackTrace();
            connected = false;
        }
        if (connected) {
            try {
                outputStream = socket.getOutputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                inputStream = socket.getInputStream();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return connected;
    }

    void beginListenForData() {
        final Handler handler = new Handler();
        stopThread = false;
        buffer = new byte[1024];
        Thread thread = new Thread(new Runnable() {
            public void run() {
                while (!Thread.currentThread().isInterrupted() && !stopThread) {
                    try {
                        int byteCount = inputStream.available();
                        if (byteCount > 0) {
                            final byte[] rawBytes = new byte[byteCount];
                            inputStream.read(rawBytes, 0, byteCount); // 0과 byteCount 추가함
                            String string = new String(rawBytes, "UTF-8");
                            fullName = fullName + string;
                            Log.d("string", fullName);
                            if (fullName.contains("\r\n")) {
                                fullName.replaceAll("\r\n", "");

                                //////////////////// NFC정보 검색 ////////////////////
                                protocol = new Protocol(Protocol.PT_RES_NFC);
                                buf = protocol.getPacket();
                                int packetType = buf[0];
                                protocol.setPacket(packetType, buf);

                                System.out.println("서버에 NFC 등록 요청");

                                // 서버로 패킷 전송 (NFC 정보)
                                protocol.setNFCID(fullName);
                                System.out.println("NFC 정보 전송");
                                try {
                                    os = serverSocket.getOutputStream();
                                    os.write(protocol.getPacket());
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }
                                // 패킷 초기화
                                protocol = new Protocol(Protocol.PT_NFC_RESULT);
                                buf = protocol.getPacket();
                                packetType = buf[0];
                                protocol.setPacket(packetType, buf);

                                try {
                                    is = serverSocket.getInputStream();
                                    is.read(buf);
                                } catch (IOException e) {
                                    e.printStackTrace();
                                }

                                packetType = buf[0];
                                protocol.setPacket(packetType, buf);

                                System.out.println("서버가 NFC 결과 전송");
                                String result = protocol.getNFCResult();
                                if (result.equals("1")) {
                                    System.out.println("NFC 등록 성공");
                                    ttsManager.initQueue(protocol.getNFCName() + "입니다.");
                                } else if (result.equals("2")) {
                                    System.out.println("NFC 등록 실패");
                                }
                                fullName = "";
                            }
                        }
                    } catch (IOException ex) {
                        stopThread = true;
                    }
                }
            }
        });

        thread.start();
    }

    @Override
    protected void onPause() {
        ttsManager.stop();
        ttsManager.shutDown();
        super.onPause();
    }

    @Override
    protected void onDestroy() {
        ttsManager.stop();
        ttsManager.shutDown();
        super.onDestroy();
    }
}
