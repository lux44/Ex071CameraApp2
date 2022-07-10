package com.lux.ex071cameraapp2;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.widget.ImageView;
import android.widget.Toast;

import com.bumptech.glide.Glide;

import java.io.File;
import java.io.FileWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class MainActivity extends AppCompatActivity {

    ImageView iv;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //동적 퍼미션 요청 코드
        String[] permissions=new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (checkSelfPermission(permissions[0])== PackageManager.PERMISSION_DENIED){
            requestPermissions(permissions,100);
        }
        
        iv=findViewById(R.id.iv);
        findViewById(R.id.btn).setOnClickListener(view -> {
            Intent intent=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

            //저장될 파일의 uri를 설정하는 기능 메소드 호출 -- 저 아래에서 직접 만들것임.
            setImageUri();

            //카메라 앱에게 촬영한 이미지를 파일로 저장하도록 요청하기 위해
            //저장될 파일의 uri를 추가데이터를 인텐트에게 전달
            if (imgUri!=null) intent.putExtra(MediaStore.EXTRA_OUTPUT,imgUri);

            resultLauncher.launch(intent);
        });
    }

    //카메라 앱이 촬영한 사진을 저장할 파일의 경로 uri 참조변수
    Uri imgUri;

    //저장된 파일의 경로(uri)를 설정하는 기능 메소드 정의
    void setImageUri(){
        //외부저장소의 파일 경로부터 얻어오기 [동적 퍼미션 필요]
        File path= Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
        //경로가 정해졌으니 파일명을 정하기 - 중복되지 않은 이름으로 만들어지도록
        //특정한 날짜 포맷으로 문자열을 만들어주는 객체를 이용해보기
        SimpleDateFormat simpleDateFormat=new SimpleDateFormat("yyyyMMdd_hhmmss"); //20220323_103729
        String fileName="IMG"+simpleDateFormat.format(new Date())+".jpg";

        //경로 +  파일명.확장자 결합한 File 객체 생성
        File file=new File(path,fileName);

        //우선 여기까지 잘 되었는지 확인
        //new AlertDialog.Builder(this).setMessage(file.getAbsolutePath()).create().show();

        //File : 실제 파일의 절대 경로
        //Uri : 파일의 DB 경로(즉, 컨텐츠의 경로)

        //카메라 앱은 EXTRA_OUTPUT(사진이 저장될 경로)에 절대경로인 File이 아니라
        //콘텐츠 경로인 Uri를 요구함
        //그래서 File->Uri로 변환
        //N버전부터 이 변환작업시에 FielProvider라는 녀석이 필요함.
        if (Build.VERSION.SDK_INT<Build.VERSION_CODES.N){
            imgUri=Uri.fromFile(file);
        }else {
            //다른 앱에게 파일의 접근을 허용해주도록 하는 FileProvider 를 이용해야함.
            //두번째 파라미터 authority : FileProvider 객체의 식별 명칭
            imgUri= FileProvider.getUriForFile(this,"com.lux.ex071cameraapp2.FileProvider",file);

            //FileProvider 객체 만들기 - AndroidManifest.xml에 등록
        }
        //uri가 잘 구해졌는지 확인해보기 - " content://....."이면 잘 된 것임.
        new AlertDialog.Builder(this).setMessage(imgUri.toString()).create().show();


    }

    ActivityResultLauncher<Intent> resultLauncher=registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
        @Override
        public void onActivityResult(ActivityResult result) {
            if (result.getResultCode()==RESULT_OK){
                //개발자가 지정한 imgUri에 촬영된 사진이 저장되어 있을것이므로
                //결과 데이터 받을 필요 없음.
                Glide.with(MainActivity.this).load(imgUri).into(iv);
            }
        }
    });
    
    //동적 퍼미션 요청 결과 콜백메소드
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode==100 && grantResults[0]==PackageManager.PERMISSION_GRANTED){
            Toast.makeText(this, "사진 촬영 가능", Toast.LENGTH_SHORT).show();
        }else {
            Toast.makeText(this, "사진 촬영 불가", Toast.LENGTH_SHORT).show();
        }
    }
}