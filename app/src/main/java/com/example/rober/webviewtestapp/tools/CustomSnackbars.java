package com.example.rober.webviewtestapp.tools;

import android.content.Context;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;

import com.example.rober.webviewtestapp.R;

public class CustomSnackbars extends AppCompatActivity {

    public void getSnackbarDismissable(Context ctx, String strInfo, String strBtn) {
        final Snackbar customSnackbar = Snackbar.make(findViewById(R.id.activityLoginCoordinatorLayout), strInfo, Snackbar.LENGTH_LONG);
        // set action button color
        customSnackbar.setActionTextColor(ctx.getResources().getColor(R.color.grassGreen));
        View sbView = customSnackbar.getView();
        sbView.setBackgroundColor(ctx.getResources().getColor(R.color.oceanBlue));
        TextView sbTextView = (TextView) sbView.findViewById(R.id.snackbar_text);
        sbTextView.setTextColor(ctx.getResources().getColor(R.color.yellow));

        customSnackbar.setAction(strBtn, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                customSnackbar.dismiss();
            }
        });
        customSnackbar.show();
    }
}

