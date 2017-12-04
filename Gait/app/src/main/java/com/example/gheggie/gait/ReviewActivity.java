package com.example.gheggie.gait;

import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.RatingBar;
import android.widget.TextView;

public class ReviewActivity extends AppCompatActivity implements View.OnClickListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.review_activity);

        Button submitReview = (Button)findViewById(R.id.submit_review);
        submitReview.setOnClickListener(this);
        GaitInfo gaitUser = GaitUtils.loadGait(this);
        RatingBar gaitRating = (RatingBar)findViewById(R.id.rating_bar);
        gaitRating.setMax(5);
        TextView rateTitle = (TextView)findViewById(R.id.rate_title);

        setupToolBar();

        if(gaitUser.isaGait()) {
            rateTitle.setText(R.string.rate_client);
        } else {
            rateTitle.setText(R.string.rate_your_gait);
        }

        //change statusBar color
        Window window = getWindow();
        window.setStatusBarColor(ContextCompat.getColor(this, R.color.colorPrimary));
    }

    private void setupToolBar(){
        Toolbar reviewBar = (Toolbar)findViewById(R.id.review_bar);
        reviewBar.setTitleTextColor(ContextCompat.getColor(this, R.color.colorAccent));

        setSupportActionBar(reviewBar);
    }

    @Override
    public void onClick(View v) {
        GaitMapFragment.openRequest = true;
        finish();
    }
}
