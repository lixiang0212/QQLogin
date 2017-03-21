package com.lx.qqlogindemo;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import com.tencent.connect.UserInfo;
import com.tencent.connect.common.Constants;
import com.tencent.connect.share.QQShare;
import com.tencent.tauth.IUiListener;
import com.tencent.tauth.Tencent;
import com.tencent.tauth.UiError;
import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends AppCompatActivity {
    private Button btn_login,btn_share,btn_avatar;
    private ImageView imageView;
    private TextView textView;
    public static Tencent mTencent;//封装了登录分享的工具类
    private String APP_ID = "222222";//创建应用时的appid和manifest一样
    private IUiListener loginListener,userInfoListener,shareListener;//在你登录或分享成功后会执行该接口
    private String SCOPE = "all";//可以获取各种API的权限
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case 0:
                    JSONObject response = (JSONObject) msg.obj;
                    if (response.has("nickname")) {
                        try {
                            textView.setText(response.getString("nickname"));
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    break;
                case 1:
                    Bitmap bitmap = (Bitmap)msg.obj;
                    imageView.setImageBitmap(bitmap);
                    break;
            }
        }
    };
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initQQLogin();
    }

    private void initView() {
        btn_login = (Button) findViewById(R.id.login);
        btn_login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mTencent.login(MainActivity.this,SCOPE,loginListener);
            }
        });
        btn_share = (Button) findViewById(R.id.share);
        btn_share.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Bundle params = new Bundle();
                params.putInt(QQShare.SHARE_TO_QQ_KEY_TYPE,QQShare.SHARE_TO_QQ_TYPE_DEFAULT);
                params.putString(QQShare.SHARE_TO_QQ_TITLE,"标题");
                params.putString(QQShare.SHARE_TO_QQ_SUMMARY,"摘要");
                params.putString(QQShare.SHARE_TO_QQ_TARGET_URL,"http://blog.csdn.net/u013451048");
                params.putString(QQShare.SHARE_TO_QQ_IMAGE_URL,"http://avatar.csdn.net/C/3/D/1_u013451048.jpg");
                params.putString(QQShare.SHARE_TO_QQ_APP_NAME, "CSDN");
                mTencent.shareToQQ(MainActivity.this, params, shareListener);
            }
        });
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.tv_name);
        btn_avatar = (Button) findViewById(R.id.set_avatar);
        btn_avatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setClass(MainActivity.this,AvatarActivity.class);
                startActivity(intent);
            }
        });
    }

    private void initQQLogin() {
        mTencent = Tencent.createInstance(APP_ID,this);
        //QQ登录回调接口
        loginListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                //登录成功
                Toast.makeText(MainActivity.this, "登录成功", Toast.LENGTH_SHORT).show();
                JSONObject object = (JSONObject) o;
                Log.i("COMPLETE:", object.toString());
                String openID;
                try {
                    openID = object.getString("openid");
                    String accessToken = object.getString("access_token");
                    String expires = object.getString("expires_in");
                    mTencent.setOpenId(openID);
                    mTencent.setAccessToken(accessToken,expires);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
            @Override
            public void onError(UiError uiError) {
                //登录失败
                Toast.makeText(MainActivity.this, "登录失败", Toast.LENGTH_SHORT).show();
                Log.e("LoginError:", uiError.toString());
            }
            @Override
            public void onCancel() {
                //登录取消
                Toast.makeText(MainActivity.this, "登录取消", Toast.LENGTH_SHORT).show();
            }
        };
        userInfoListener = new IUiListener() {
            @Override
            public void onComplete(final Object response) {
                if(response== null){return;}
                Message msg = new Message();
                msg.obj = response;
                msg.what = 0;
                handler.sendMessage(msg);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    JSONObject json = (JSONObject) response;
                    if(json.has("figureurl")){
                        Bitmap bitmap = null;
                        try {
                            bitmap = Utils.getbitmap(json.getString("figureurl_qq_2"));
                        } catch (JSONException e) {

                        }
                        Message msg = new Message();
                        msg.obj = bitmap;
                        msg.what = 1;
                       handler.sendMessage(msg);
                    }
                }
            }).start();
//                try {
////                    JSONObject jo = (JSONObject) o;
////                    Log.i("JO:",jo.toString());
////                    int ret = jo.getInt("ret");
////                    String nickName = jo.getString("nickname");
////                    String gender = jo.getString("gender");
////                    Toast.makeText(MainActivity.this, "你好，" + nickName,Toast.LENGTH_LONG).show();
//                } catch (Exception e) {
//                }
            }

            @Override
            public void onError(UiError uiError) {

            }

            @Override
            public void onCancel() {

            }
        };
        shareListener = new IUiListener() {
            @Override
            public void onComplete(Object o) {
                //分享成功后回调
                Toast.makeText(MainActivity.this, "分享成功！", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onError(UiError uiError) {

            }

            @Override
            public void onCancel() {

            }
        };
    }
    //判断用户是否已经成功的登录了QQ
    public static boolean ready(Context context) {
        if (mTencent== null) {
            return false;
        }
        boolean ready = mTencent.isSessionValid() && mTencent.getQQToken().getOpenId() != null;
        if (!ready) {
            Toast.makeText(context, "login and get openId first, please!",
                    Toast.LENGTH_SHORT).show();
        }
        return ready;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == Constants.REQUEST_LOGIN){
            if(resultCode == -1){
                Tencent.onActivityResultData(requestCode, resultCode, data, loginListener);
                Tencent.handleResultData(data, loginListener);
                //拿到用户信息
                UserInfo info = new UserInfo(this,mTencent.getQQToken());
                info.getUserInfo(userInfoListener);
            }
        }
    }
}
