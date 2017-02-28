package com.lance.coolweather.activity;

import android.annotation.SuppressLint;
import android.app.ProgressDialog;
import android.support.v7.app.AppCompatActivity;

import com.lance.coolweather.R;

/**
 * Created by lindan on 17-2-21.
 */
@SuppressLint("Registered")
public class BaseActivity extends AppCompatActivity {
    protected ProgressDialog progressDialog;

    protected void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(this);
            progressDialog.setMessage(getString(R.string.app_common_progress_dialog_msg));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    protected void closeProgressDialog() {
        if (progressDialog != null) {
            progressDialog.dismiss();
        }
    }

//    @Override
//    protected void onCreate(@Nullable Bundle savedInstanceState) {
////        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
////            View decorView = getWindow().getDecorView();
////            decorView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
////            getWindow().setStatusBarColor(Color.TRANSPARENT);
////        }
//        super.onCreate(savedInstanceState);
//    }
}
