package me.synology.wookoo.shuttle_tracker_driver;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
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

import org.json.JSONException;
import org.json.JSONObject;

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
    String Address= "98:DA:60:01:47:D2";
    private double lat,lon;

    boolean start = false;




    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_drive);


        //인텐트에서 sender 받아오기
        lat= 0;
        lon = 0;


        locationManager = (LocationManager)getSystemService(Context.LOCATION_SERVICE);


        //버튼 누르면 call 하게끔 수정

        Button mButton = findViewById(R.id.drive_start_btn);

        mButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                start = !start;
                Toast.makeText(DriveActivity.this,"버튼 눌림" + start,Toast.LENGTH_SHORT).show();
                if(!start){
                    //블루투스 페어링 확인하는부분
                  //  bt.disconnect();
                    locationManager.removeUpdates(DriveActivity.this);
                    mButton.setText("운행시작");

                    return;
                }
                //bt.connect(Address);
                mButton.setText("운행종료");

                if (ActivityCompat.checkSelfPermission(DriveActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    return;
                }
                locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 0, DriveActivity.this);
                //여기서 GPS 전송 스레드 시작
            }
        });
        /*

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
            }

            @Override
            public void onDeviceDisconnected() {

            }

            @Override
            public void onDeviceConnectionFailed() {

            }
        });
        bt.setOnDataReceivedListener(new BluetoothSPP.OnDataReceivedListener() {
            @Override
            public void onDataReceived(byte[] data, String message) {
                message = message.trim();
                String[] datas = message.split("/");
                String tag = datas[0];
                String method = datas[1];
                Log.d("수신 메시지",message);

                if(method.equals("on")){
                    method = "탑승";
                }else{
                    method = "하차";
                }
                long now = System.currentTimeMillis();
                Date date = new Date(now);
                SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
                String getTime = dateFormat.format(date);


                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl("http://192.168.1.2:8000")
                        .addConverterFactory(GsonConverterFactory.create())
                        .build();
                RetrofitAPI r = retrofit.create(RetrofitAPI.class);
                JSONObject input = new JSONObject();

                try {
                    input.put("lat",lat);
                    input.put("lon",lon);
                    input.put("tag",tag);
                    input.put("method",method);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                Call<rideUploadData> c = r.sendRide(input);
                c.enqueue(new Callback<rideUploadData>() {
                    @Override
                    public void onResponse(Call<rideUploadData> call, Response<rideUploadData> response) {
                        rideUploadData r = response.body();
                        Log.d("rr",r.getStatus()+"" + r.getName());
                        Toast.makeText(DriveActivity.this,r.getName() + "어린이가 " + r.getMethod() + " 했습니다.",Toast.LENGTH_SHORT).show();

                    }

                    @Override
                    public void onFailure(Call<rideUploadData> call, Throwable t) {
                        Log.d("error",t.getMessage());
                    }
                });






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

        }*/





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

        double latitude = 0.0;
        double longitude = 0.0;

        if(location.getProvider().equals(LocationManager.GPS_PROVIDER)) {
            latitude = location.getLatitude();
            lat = Math.round(latitude*10000)/10000.0;
            longitude = location.getLongitude();
            lon = Math.round(longitude*10000)/10000.0;
            //여기서 전송을 하는게 맞는듯

            Log.d(TAG + " GPS : ", Double.toString(latitude )+ '/' + Double.toString(longitude));

            Retrofit retrofit = new Retrofit.Builder()
                    .baseUrl("http://192.168.1.71:8000")
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