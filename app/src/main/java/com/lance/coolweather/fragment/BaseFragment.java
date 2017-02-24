package com.lance.coolweather.fragment;

import android.app.ProgressDialog;
import android.support.v4.app.Fragment;

import com.lance.coolweather.R;

/**
 * Created by lindan on 17-2-21.
 */

public class BaseFragment extends Fragment {
    protected ProgressDialog progressDialog;

    protected void showProgressDialog() {
        if (progressDialog == null) {
            progressDialog = new ProgressDialog(getActivity());
            progressDialog.setMessage(getString(R.string.app_common_progress_dialog_msg));
            progressDialog.setCanceledOnTouchOutside(false);
        }
        progressDialog.show();
    }

    protected void closeProgressDialog() {
        if(progressDialog != null) {
            progressDialog.dismiss();
        }
    }
}
