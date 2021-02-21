package kosmo.project3.schlineapp;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.HashMap;

public class TeamEditActivity extends AppCompatActivity {

    String TAG = "SEONGJUN";

    EditText title;
    EditText content;
    String filePath1;//파일의 절대경로
    TextView teamEditfile;
    String subject_idx;
    String board_idx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_team_edit);

        if(ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED){
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        Intent intent = getIntent();
        Bundle bundle = intent.getBundleExtra("teambundle");
        title = findViewById(R.id.teamEdittitle);
        content = findViewById(R.id.teamEditcontent);
        teamEditfile = findViewById(R.id.teamEditfile);

        Log.i(TAG, "값이 안넘어오나..?"+bundle.getString("board_title"));

        title.setText(bundle.getString("board_title"));
        content.setText(bundle.getString("board_content"));
        teamEditfile.setText(bundle.getString("board_file"));
        board_idx = bundle.getString("board_idx");
        Log.i(TAG, "게시판번호:"+board_idx);
    }

    public void btnTeamEdit(View view){
        Log.i(TAG, "수정버튼게시판번호:"+board_idx);
        HashMap<String, String> param1 = new HashMap<>();
        param1.put("user_id", StaticUserInformation.userID);
        param1.put("board_title", title.getText().toString());
        param1.put("board_content", content.getText().toString());
        param1.put("board_idx", board_idx);

        HashMap<String, String> param2 = new HashMap<>();
        param2.put("filename", filePath1);

        AsyncTeamEdit teamEdit = new AsyncTeamEdit(getApplicationContext(), param1, param2);

        teamEdit.execute();
    }

    public void btnTeamEditUpload(View view){

        Intent it = new Intent(Intent.ACTION_PICK);
        it.setType("application/*");
        it.setAction(Intent.ACTION_GET_CONTENT); startActivityForResult(it, 1);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == 1) {
            if (resultCode == RESULT_OK) {
                Uri uri = data.getData();
                showfile(uri);
            }
        }
    }

    private void showfile(Uri imageUri) {
        // 절대경로를 획득한다!!! 중요~
        filePath1 = getRealPathFromURI(imageUri);//사용자정의함수
        Log.i(TAG, "path1:" + filePath1);
        String[] filenames = filePath1.split("/");
        String filename = filenames[filenames.length-1];
        Log.i(TAG, "수정파일명:"+filename);
        teamEditfile = findViewById(R.id.teamEditfile);
        teamEditfile.setText(filename);
    }

    private String getRealPathFromURI(Uri contentUri) {

        Cursor cursor = getContentResolver().query(contentUri, null, null, null, null );
        cursor.moveToNext();
        String path = cursor.getString( cursor.getColumnIndex( "_data" ) );
        cursor.close();
        return path;
    }


    class AsyncTeamEdit extends AsyncTask<Object, Integer, JSONObject> {

        private Context mContext;
        private HashMap<String, String> param;
        private HashMap<String, String> files;

        public AsyncTeamEdit(Context context, HashMap<String, String> param, HashMap<String, String> files) {
            mContext = context;
            this.param = param;
            this.files = files;
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected JSONObject doInBackground(Object... objects) {
            JSONObject rtn = null;
            try {
                //프로젝트명이나 요청명이 변경될 수 있음
                //따라서 서비스URL은 리소스의 상수로 저장하는것이 좋다.
                String sUrl =  "http://"+ StaticInfo.my_ip +
                        "/schline/android/teamEditAction.do";
                //단말기의 사진을 서버로 업로드하기위한 객체생성 및 메소드호출
                //FileUpload 클래스는 기존내용을 그대로 가져다 쓰면 됨(수정필요없음)
                Log.i(TAG, "파일명확인:"+files.get("filename"));
                if(files.get("filename")!=null) {

                    FileUpload multipartUpload = new FileUpload(sUrl, "UTF-8");
                    rtn = multipartUpload.upload(param, files);
                    //서버에서 반환받은 결과데이터를 로그로 출력
                    Log.i(TAG, rtn.toString());
                }
                else{
                    FileUpload multipartUpload = new FileUpload(sUrl, "UTF-8");
                    rtn = multipartUpload.upload(param);
                    //서버에서 반환받은 결과데이터를 로그로 출력
                    Log.i(TAG, rtn.toString());

                }
            }
            catch (IOException e) {
                e.printStackTrace();
            }
            return rtn;
        }

        @Override
        protected void onPostExecute(JSONObject jsonObject) {
            super.onPostExecute(jsonObject);


            if (jsonObject != null) {
                //결과데이터를 텍스트뷰에 출력
                try {
                    if (jsonObject.getInt("success") == 1) {
                        Toast.makeText(mContext, "수정완료",
                                Toast.LENGTH_LONG).show();
                        TeamViewActivity ta = (TeamViewActivity)TeamViewActivity.teamviewcontext;
                        ta.finish();
                        Intent intent = new Intent(getApplicationContext(), TeamViewActivity.class);
                        intent.putExtra("board_idx", board_idx);
                        finish();
                        startActivity(intent);

                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            else {
                Toast.makeText(mContext, "수정실패",
                        Toast.LENGTH_LONG).show();
            }
        }
    }
}
