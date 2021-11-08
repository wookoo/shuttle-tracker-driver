package me.synology.wookoo.shuttle_tracker_driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.FragmentManager;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.naver.maps.geometry.LatLng;
import com.naver.maps.map.CameraUpdate;
import com.naver.maps.map.MapFragment;
import com.naver.maps.map.NaverMap;
import com.naver.maps.map.OnMapReadyCallback;
import com.naver.maps.map.overlay.Marker;
import com.naver.maps.map.overlay.OverlayImage;

import org.java_websocket.client.WebSocketClient;
import org.java_websocket.handshake.ServerHandshake;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import app.akexorcist.bluetotohspp.library.BluetoothSPP;
import app.akexorcist.bluetotohspp.library.BluetoothState;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class DriveActivity extends AppCompatActivity implements LocationListener{
    private BluetoothSPP bt;
    private LocationManager locationManager;
    private String TAG = "GPS POSITION";
    //String Address= "98:DA:60:01:7A:EF"; //E주소
    String Address= "98:DA:60:01:4F:25"; //B 에 대한 주소

    private double lat,lon;
    String SERVER_URL = "http://220.69.209.111:8000/";

    boolean start = false;
    boolean process = false;

    Marker marker = null;
    NaverMap naverMap = null;

    private URI uri;
    private WebSocketClient mSocketClient;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);

        FragmentManager fm = getSupportFragmentManager();
        MapFragment mapFragment = (MapFragment)fm.findFragmentById(R.id.map_fragment);
        if(mapFragment == null){
            mapFragment = MapFragment.newInstance();
            fm.beginTransaction().add(R.id.map_fragment, mapFragment).commit();
        }

        try {
            uri = new URI("ws://220.69.209.111:8000/ws/provider/");
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }



        mapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(@NonNull NaverMap naverMap) {
                DriveActivity.this.naverMap = naverMap;
                Log.d("marker IS NULL",""+ (naverMap==null));
                if(DriveActivity.this.naverMap != null){
                    marker  = new Marker();
                    marker.setPosition(new LatLng(37.5670135, 126.9783740));
                    //marker.setIcon(OverlayImage.fromResource(R.drawable.ic_bus));
                    marker.setMap(naverMap);
                }

            }
        }); //항상 콜백으로 돌려야됨됨


        //인텐트에서 sender 받아오기
        lat= 0;
        lon = 0;


        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


        //버튼 누르면 call 하게끔 수정

        Button mButton = findViewById(R.id.drive_start_btn);


        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //Toast.makeText(DriveActivity.this,"버튼 눌림" + start,Toast.LENGTH_SHORT).show();
                if(process){
                    return;
                }
                process = true;
                if(start){
                    //블루투스 페어링 확인하는부분
                    //남았는지 확인
                    AlertDialog.Builder builder = new AlertDialog.Builder(DriveActivity.this);
                    builder.setTitle("운행 종료");
                    builder.setMessage("정말로 운행을 종료할까요?");
                    builder.setPositiveButton("예",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    Log.d("운행 종료 전송","했다");
                                    bt.send("0",false);
                                }
                            });
                    builder.setNegativeButton("아니오",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            });
                    builder.setCancelable(false);
                    builder.show();



                    return;
                }
                //여기에 dialog
                AlertDialog.Builder builder = new AlertDialog.Builder(DriveActivity.this);
                builder.setTitle("운행 시작");
                builder.setMessage("운행을 시작할까요?");
                builder.setCancelable(false);
                builder.setPositiveButton("예",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                Toast.makeText(getApplicationContext()
                                        , "차량과 연결중입니다.",Toast.LENGTH_SHORT).show();
                                bt.connect(Address);
                            }
                        });
                builder.setNegativeButton("아니오",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                builder.show();


                //여기서 GPS 전송 스레드 시작
            }
        });


        bt = new BluetoothSPP(this);
        if (!bt.isBluetoothAvailable()) { //블루투스 사용 불가
            Toast.makeText(getApplicationContext()
                    , "Bluetooth is not available"
                    , Toast.LENGTH_SHORT).show();
            finish();
        }
        bt.setBluetoothConnectionListener(new BluetoothSPP.BluetoothConnectionListener() {
            @Override
            public void onDeviceConnected(String name, String address) {

                Toast.makeText(getApplicationContext()
                        , "차량과 연결되었습니다.",Toast.LENGTH_SHORT).show();
                start = true;
                mSocketClient = new WebSocketClient(uri) {
                    @Override
                    public void onOpen(ServerHandshake handshakedata) {

                    }

                    @Override
                    public void onMessage(String message) {
                        Log.d("On Message",message);
                    }

                    @Override
                    public void onClose(int code, String reason, boolean remote) {

                    }

                    @Override
                    public void onError(Exception ex) {

                    }
                };
                mSocketClient.connect();

                mButton.setText("운행종료");

                if (ActivityCompat.checkSelfPermission(DriveActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, DriveActivity.this);
                process = false;
            }

            @Override
            public void onDeviceDisconnected() {
                process = false;
                try{
                    mSocketClient.close();
                }
                catch (Exception e){

                }

                if(start){
                    AlertDialog.Builder builder = new AlertDialog.Builder(DriveActivity.this);
                    builder.setTitle("운행오류 발생");
                    builder.setMessage("차량과 연결이 해제 되어 강제 운행 중지됩니다.");
                    builder.setCancelable(false);
                    builder.setPositiveButton("확인",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    locationManager.removeUpdates(DriveActivity.this);
                                    mButton.setText("운행시작");
                                    start = false;
                                    process = false;



                                }
                            });
                    builder.show();



                }


            }

            @Override
            public void onDeviceConnectionFailed() {

                AlertDialog.Builder builder = new AlertDialog.Builder(DriveActivity.this);
                builder.setTitle("운행불가");
                builder.setMessage("차량과 연결이 실패되어 운행 할 수 없습니다.");
                builder.setCancelable(false);
                builder.setPositiveButton("확인",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                process = false;
                            }
                        });
                builder.show();

            }
        });
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                //process = false;
                message = message.trim();
                Log.d("bluetooth",message);


                //정해야 한다
                //남은 사람 수 및 카드키 번호
                //전송정리 . command/tag/method 로 짤라버리기

                //ride/tag/on
                //ride/tag/off
                //remain/사람,사람,사람/슷자



                String[] datas = message.split("/");
                String command = datas[0];
                String tag = datas[1];
                //String info ="hel";
                String info = datas[2];

                if(command.equals("ride")){
                    if(info.equals("on")){
                        info = "탑승";
                    }else{
                        info = "하차";
                    }
                    long now = System.currentTimeMillis();
                    Date date = new Date(now);
                    SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                    String getTime = dateFormat.format(date);


                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(SERVER_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    RetrofitAPI r = retrofit.create(RetrofitAPI.class);
                    JSONObject input = new JSONObject();

                    try {
                        input.put("lat",lat);
                        input.put("lon",lon);
                        input.put("tag",tag);
                        input.put("info",info);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    mSocketClient.send(input.toString());
                    Log.d("SEND",input.toString());
                    Call<rideUploadData> c = r.sendRide(input);
                    c.enqueue(new Callback<rideUploadData>() {
                        @Override
                        public void onResponse(Call<rideUploadData> call, Response<rideUploadData> response) {
                            rideUploadData r = response.body();

                            Log.d("rr",r.getStatus()+"" + r.getName());
                            if(!r.getStatus()){
                                Toast.makeText(DriveActivity.this,"등록되지 않은 사용자입니다.",Toast.LENGTH_SHORT).show();
                                bt.send("1",false);
                                return;
                            }
                            Toast.makeText(DriveActivity.this,r.getName() + "어린이가 " + r.getInfo() + " 했습니다.",Toast.LENGTH_SHORT).show();

                        }

                        @Override
                        public void onFailure(Call<rideUploadData> call, Throwable t) {
                            Log.d("error",t.getMessage());
                        }
                    });

                }
                else if(command.equals("remain")){
                    int remainPeople = Integer.parseInt(info);
                    if(remainPeople == 0){
                        //
                        locationManager.removeUpdates(DriveActivity.this);
                        mButton.setText("운행시작");
                        Toast.makeText(DriveActivity.this,"운행이 종료되었습니다",Toast.LENGTH_SHORT).show();
                        start = false;


                        bt.disconnect();
                        process = false;
                        //locationManager.removeUpdates(DriveActivity.this);
                        return;
                    }
                    Retrofit retrofit = new Retrofit.Builder()
                            .baseUrl(SERVER_URL)
                            .addConverterFactory(GsonConverterFactory.create())
                            .build();
                    RetrofitAPI r = retrofit.create(RetrofitAPI.class);
                    JSONObject input = new JSONObject();

                    try {
                        input.put("tags",tag);
                        input.put("remain",info);

                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Call<remainData> c = r.check(input);
                    c.enqueue(new Callback<remainData>() {
                        @Override
                        public void onResponse(Call<remainData> call, Response<remainData> response) {
                            remainData r = response.body();
                            //Toast.makeText(DriveActivity.this,"총 " + r.getRemain() + "명이 탑승중입니다 : " + r.getTags(),Toast.LENGTH_SHORT).show();
                            AlertDialog.Builder builder = new AlertDialog.Builder(DriveActivity.this);
                            builder.setTitle("운행 종료 불가");
                            builder.setMessage("총 " + r.getRemain() + "명 탑승중입니다.\n탑승자 : "+r.getTags());
                            builder.setCancelable(false);
                            builder.setPositiveButton("확인",
                                    new DialogInterface.OnClickListener() {
                                        public void onClick(DialogInterface dialog, int which) {
                                            process = false;
                                        }
                                    });
                            builder.show();
                        }

                        @Override
                        public void onFailure(Call<remainData> call, Throwable t) {

                        }
                    });


                }
                Log.d("수신 메시지",message);








            }
        });
        if (!bt.isBluetoothEnabled()) { //
            Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
            startActivityForResult(intent, BluetoothState.REQUEST_ENABLE_BT);
        } else {
            if (!bt.isServiceAvailable()) {
                bt.setupService();
                bt.startService(BluetoothState.DEVICE_OTHER); //DEVICE_ANDROID는 안드로이드 기기 끼리
            }

        }


        //bt.connect(Address);





    }

    @Override
    public void onProviderEnabled(String provider) {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, this);

    }


    @Override
    public void onLocationChanged(Location location) {



        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            lat = location.getLatitude();
            lat = Math.round(lat*10000)/10000.0;
            lon = location.getLongitude();
            lon = Math.round(lon*10000)/10000.0;
            if(marker != null){
                LatLng nowPos = new LatLng(lat,lon);
                marker.setPosition(nowPos);
                naverMap.moveCamera(CameraUpdate.scrollTo(nowPos));
            }
            JSONObject input = new JSONObject();
            try {
                input.put("lat",lat);
                input.put("lon",lon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            try{
                mSocketClient.send(input.toString());
            }catch (Exception e){
                Log.d("ERRROR",e.toString());
            }


            //여기서 전송을 하는게 맞는듯
            /*

            Log.d(TAG + " GPS : ", Double.toString(latitude )+ '/' + Double.toString(longitude));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl(SERVER_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
            RetrofitAPI r = retrofit.create(RetrofitAPI.class);
            JSONObject input = new JSONObject();
            try {
                input.put("lat",lat);
                input.put("lon",lon);
            } catch (JSONException e) {
                e.printStackTrace();
            }
            Call<busUploadData> call = r.sendGPS(input);
            call.enqueue(new Callback<busUploadData>() {
                @Override
                public void onResponse(Call<busUploadData> call, Response<busUploadData> response) {
                    busUploadData r = response.body();
                    //Log.d("SUCCESS",r.getStatus()+"");
                }

                @Override
                public void onFailure(Call<busUploadData> call, Throwable t) {
                    Log.d("ERROR RT",t.getMessage()+"");
                }
            });

             */


        }



    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

    }

    @Override
    public void onProviderDisabled(String provider) {

    }

    @Override
    protected void onStart() {
        super.onStart();
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            //권한이 없을 경우 최초 권한 요청 또는 사용자에 의한 재요청 확인
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_FINE_LOCATION) &&
                    ActivityCompat.shouldShowRequestPermissionRationale(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)) {
                // 권한 재요청
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            } else {
                ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION, android.Manifest.permission.ACCESS_COARSE_LOCATION}, 100);
                return;
            }
        }

    }

    @Override
    protected void onPause() {
        super.onPause();
        locationManager.removeUpdates(this);
        Log.d("call on Puase","start");
    }

    @Override
    protected void onResume() {
        super.onResume();

    }
}