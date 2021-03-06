package nova.sampleImageProcessing;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import static nova.sampleImageProcessing.BasicInfo.CROP_FROM_IMAGE;
import static nova.sampleImageProcessing.BasicInfo.PICK_FROM_ALBUM;
import static nova.sampleImageProcessing.BasicInfo.PICK_FROM_CAMERA;






//////////////이미지 프로세싱 연습을 위한 코드
///1.  이미지를 갤러리 / 사진을 찍고 잘라서 가져온다.
///2. 다른 액티비티로 uri를 보내 사진을 가져올 수 있게 해준다.






public class MainActivity extends AppCompatActivity {

    Uri imageUri;

    private Uri mImageCaptureUri;
    private Uri cropImageUri;
    private ImageView imageViewForAdd;
    private String absolutePath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        clearMyPrefs();

        setContentView(R.layout.activity_main);




        imageViewForAdd = (ImageView)this.findViewById(R.id.imageViewAdd);



        Button buttonSave = (Button)findViewById(R.id.buttonSaveImage);
        buttonSave.setOnClickListener(new ImageButton.OnClickListener(){
            public void onClick(View v){
                saveImage();

                Intent intent = new Intent(getApplicationContext(), ImageActivity.class);

                // crop Uri를 실어서 보내준다. 아무것도 싣지 않았다면 리소스를 실어 보내준다.

                //Toast.makeText(getApplicationContext(), imageUri.toString(), Toast.LENGTH_SHORT).show();

                    intent.putExtra("imageUri", cropImageUri.toString());


                startActivity(intent);


            }
        });
    }


    public void onClickAddImageClicked(View v){
        DialogInterface.OnClickListener cameraListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakePhotoAction();
            }
        };
        DialogInterface.OnClickListener albumListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                doTakeAlbumAction();
            }
        };

        DialogInterface.OnClickListener cancelListener = new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        };

        new AlertDialog.Builder(this)
                .setTitle("업로드할 이미지 선택")
                .setPositiveButton("사진촬영", cameraListener)
                .setNeutralButton("앨범선택", albumListener)
                .setNegativeButton("취소", cancelListener)
                .show();
    }


    public void doTakePhotoAction() // 카메라 촬영 후 이미지 가져오기
    {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

        // 임시로 사용할 파일의 경로를 생성
        String url = "tmp_" + String.valueOf(System.currentTimeMillis()) + ".jpg";
        mImageCaptureUri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(), url));

        intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, mImageCaptureUri);
        startActivityForResult(intent, PICK_FROM_CAMERA);
    }

    public void doTakeAlbumAction() // 앨범에서 이미지 가져오기
    {
        // 앨범 호출
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType(android.provider.MediaStore.Images.Media.CONTENT_TYPE);
        startActivityForResult(intent, PICK_FROM_ALBUM);
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode != RESULT_OK)
            return;

        switch (requestCode) {
            case PICK_FROM_ALBUM: {
                // 이후의 처리가 카메라와 같으므로 일단  break없이 진행합니다.
                // 실제 코드에서는 좀더 합리적인 방법을 선택하시기 바랍니다.
                mImageCaptureUri = data.getData();
                Log.d("SmartWheel", mImageCaptureUri.getPath().toString());
            }

            case PICK_FROM_CAMERA: {


                // 이미지를 가져온 이후의 리사이즈할 이미지 크기를 결정합니다.
                // 이후에 이미지 크롭 어플리케이션을 호출하게 됩니다.
                Intent intent = new Intent("com.android.camera.action.CROP");
                intent.setDataAndType(mImageCaptureUri, "image/*");

                // CROP할 이미지를 200*200 크기로 저장
                intent.putExtra("outputX", 200); // CROP한 이미지의 x축 크기
                intent.putExtra("outputY", 200); // CROP한 이미지의 y축 크기
                intent.putExtra("aspectX", 1); // CROP 박스의 X축 비율
                intent.putExtra("aspectY", 1); // CROP 박스의 Y축 비율
                intent.putExtra("scale", true);
                intent.putExtra("return-data", true);
                startActivityForResult(intent, CROP_FROM_IMAGE); // CROP_FROM_CAMERA case문 이동
                break;
            }

            case CROP_FROM_IMAGE: {
                // 크롭이 된 이후의 이미지를 넘겨 받습니다.
                // 이미지뷰에 이미지를 보여준다거나 부가적인 작업 이후에
                // 임시 파일을 삭제합니다.
                if (resultCode != RESULT_OK) {
                    return;
                }

                final Bundle extras = data.getExtras();

                // CROP된 이미지를 저장하기 위한 FILE 경로
                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath() +
                        "/TempCrop/" + System.currentTimeMillis() + ".jpg";

                if (extras != null) {
                    Bitmap photo = extras.getParcelable("data"); // CROP된 BITMAP
                    imageViewForAdd.setImageBitmap(photo); // 레이아웃의 이미지칸에 CROP된 BITMAP을 보여줌

                    storeCropImage(photo, filePath); // CROP된 이미지를 외부저장소, 앨범에 저장한다.
                    absolutePath = filePath;
                    break;
                }


            }
        }
    }

    private void storeCropImage(Bitmap bitmap, String filePath) {
        // tempCrop 폴더를 생성하여 이미지를 저장하는 방식이다.
        String dirPath = Environment.getExternalStorageDirectory().getAbsolutePath()+"/tempCrop";
        File temp_crop = new File(dirPath);

        if(!temp_crop.exists()) // tempCrop 디렉터리에 폴더가 없다면 (새로 이미지를 저장할 경우에 속한다.)
            temp_crop.mkdir();

        File copyFile = new File(filePath);
        BufferedOutputStream out = null;

        try {

            copyFile.createNewFile();
            out = new BufferedOutputStream(new FileOutputStream(copyFile));


            //////////////////////uri from file을 이용, 자른 이미지의 uri를 얻어옴
            cropImageUri = Uri.fromFile(copyFile);
            saveState();

            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, out);


            // sendBroadcast를 통해 Crop된 사진을 앨범에 보이도록 갱신한다.
            sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE,
                    Uri.fromFile(copyFile)));

            out.flush();
            out.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    public void saveImage(){

        FileOutputStream fOutStream = null;

        String resDrawableUri = "android.resource://"+getApplicationContext().getPackageName()+"/drawable/basicimage";


        Bitmap bitmap = null;
        try {
            if(cropImageUri == null){

                cropImageUri =  Uri.parse(resDrawableUri) ;
            }

            Log.v("logForCropUri", "cropUri = "+cropImageUri.toString());

            bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), cropImageUri);
        } catch (IOException e) {
            e.printStackTrace();
        }


        try{

            if(cropImageUri.equals(Uri.parse(resDrawableUri) ) )
            {
                fOutStream=new FileOutputStream(resDrawableUri);
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutStream);


            }
            else {
                fOutStream=new FileOutputStream(Environment.getExternalStorageDirectory().getPath()+"/tempImage.jpg");
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fOutStream);
            }

        }
        catch(FileNotFoundException e)
        {
            e.printStackTrace();
        }


        // 임시 파일 삭제

        if(mImageCaptureUri!=null){
            File f = new File(mImageCaptureUri.getPath());
            if(f.exists())
            {
                f.delete();
            }
        }

    }

    public Uri getUri(){
        return Uri.fromFile(new File(Environment.getExternalStorageDirectory().getPath() + "/tempImage.jpg"));
    }

    @Override
    protected void onPause() {
        super.onPause();

        saveState();
    }

    @Override
    protected void onResume() {
        super.onResume();

        restoreState();
    }

    public void saveState(){

        Toast.makeText(getApplicationContext(), "saveState Called", Toast.LENGTH_SHORT).show();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        SharedPreferences.Editor editor = pref.edit();

        if(cropImageUri!=null){
            editor.putString("imageUri", cropImageUri.toString());

            editor.commit();
        }

    }

    public void restoreState(){
        Toast.makeText(getApplicationContext(), "restorestate Called", Toast.LENGTH_SHORT).show();

        SharedPreferences pref = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());

        if ((pref != null) && (pref.contains("imageUri"))) {
            String uriString = pref.getString("imageUri", "");

            imageUri = Uri.parse(uriString);

            imageViewForAdd.setImageURI(imageUri);
        }
    }



    protected void clearMyPrefs() {
        Toast.makeText(getApplicationContext(), "pref cleared", Toast.LENGTH_SHORT).show();

        SharedPreferences pref = getSharedPreferences("pref", Activity.MODE_PRIVATE);
        SharedPreferences.Editor editor = pref.edit();
        editor.clear();
        editor.commit();
    }

    public void setImgViewFromUri(ImageView imgView, Uri uri){

        Bitmap bm = null;
        try {
            bm = MediaStore.Images.Media.getBitmap(getContentResolver(), uri);
            imgView.setImageBitmap(bm);

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        imgView.setImageBitmap(bm);

    }


}






