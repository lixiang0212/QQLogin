package com.lx.qqlogindemo;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import com.tencent.connect.avatar.QQAvatar;

public class AvatarActivity extends AppCompatActivity {
    private Button button;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_avatar);
        initView();
    }

    private void initView() {
        button = (Button) findViewById(R.id.btn_setting);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (MainActivity.ready(AvatarActivity.this)) {
                    Intent intent = new Intent();
                    // 开启Pictures画面Type设定为image
                    intent.setType("image/*");
                    // 使用Intent.ACTION_GET_CONTENT这个Action
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    // 取得相片后返回本画面
                    startActivityForResult(intent, 2);
                    // 在 onActivityResult 中调用 doSetAvatar
                }}});
    }
    private void doSetAvatar(Uri uri) {
        QQAvatar qqAvatar = new QQAvatar(MainActivity.mTencent.getQQToken());
        //qqAvatar.setAvatar(this, uri, new BaseUIListener(this), R.anim.zoomout);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode ==2 && resultCode == Activity.RESULT_OK)
            doSetAvatar(data.getData());
    }
}
