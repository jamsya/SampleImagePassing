package nova.sampleImageProcessing;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;

public class MainActivity extends AppCompatActivity {

    Uri imageUri;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);




        Button btnSelect = (Button)findViewById(R.id.buttonSaveImage);
        btnSelect.setOnClickListener(new ImageButton.OnClickListener(){
            public void onClick(View v){
                saveImage();

            }
        });
    }

    public void buttonGoClicked(View v){
        Intent intent = new Intent(getApplicationContext(), ImageActivity.class);

        // 파일로부터 uri를 불러온다.

        imageUri = getUri();

        Log.v("urilog", imageUri.toString());
//주석 테스트
        //Toast.makeText(getApplicationContext(), imageUri.toString(), Toast.LENGTH_SHORT).show();



        intent.putExtra("imageUri", imageUri.toString());
        startActivity(intent);
    }

    public void saveImage(){

        FileOutputStream fOutStream = null;


        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.god2);
        try{
            fOutStream=new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/tempImage.jpg");
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutStream);
        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }
    }

    public Uri getUri(){
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/tempImage.jpg"));
    }


}



