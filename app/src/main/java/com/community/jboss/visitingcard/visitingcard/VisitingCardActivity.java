package com.community.jboss.visitingcard.visitingcard;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import com.community.jboss.visitingcard.R;
import com.community.jboss.visitingcard.SettingsActivity;
import com.community.jboss.visitingcard.about.AboutActivity;
import com.community.jboss.visitingcard.maps.MapsActivity;

import android.graphics.Bitmap;
import java.io.IOException;
import siclo.com.ezphotopicker.api.EZPhotoPick;
import siclo.com.ezphotopicker.api.EZPhotoPickStorage;
import siclo.com.ezphotopicker.api.models.EZPhotoPickConfig;
import siclo.com.ezphotopicker.api.models.PhotoSource;

public class VisitingCardActivity extends AppCompatActivity {

    private ImageButton profile_img;

    private TextView nameText;

    private TextView phoneText;
    private TextView emailText;

    private TextView linkedInText;
    private TextView twitterText;
    private TextView gitHubText;

    private static String photoDir = "visiting_card_photos";

    public static final String PREF_DARK_THEME = "dark_theme";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.visiting_card);

	      profile_img=findViewById(R.id.profile_img);
        nameText = findViewById(R.id.name_text);
        phoneText = findViewById(R.id.phone_text);
        emailText = findViewById(R.id.email_text);
        linkedInText = findViewById(R.id.linkedin_text);
        twitterText = findViewById(R.id.twitter_text);
        gitHubText = findViewById(R.id.github_text);

        populateCard();

        // TODO: Add a ImageView and a number of EditText to get his/her Visiting Card details (Currently authenticated User)



        // TODO: Align FAB to Bottom Right and replace it's icon with a SAVE icon
        // TODO: On Click on FAB should make a network call to store the entered information in the cloud using POST method(Do this in NetworkUtils class)
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Proceed to Maps Activity", Snackbar.LENGTH_LONG)
                        .setAction("Yes", new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent toVisitingCard = new Intent(VisitingCardActivity.this, MapsActivity.class);
                                startActivity(toVisitingCard);
                            }
                        }).show();
            }
        });

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode != RESULT_OK) {
            return;
        }

        if (requestCode == EZPhotoPick.PHOTO_PICK_GALLERY_REQUEST_CODE || requestCode == EZPhotoPick.PHOTO_PICK_CAMERA_REQUEST_CODE) {
            Bitmap pickedPhoto = null;
            String photoName = data.getStringExtra(EZPhotoPick.PICKED_PHOTO_NAME_KEY);
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
            SharedPreferences.Editor editor = prefs.edit();
            editor.putString("card_img", photoName);
            editor.apply();
            try {
                pickedPhoto = new EZPhotoPickStorage(this).loadLatestStoredPhotoBitmap();
            } catch (IOException e) {
                e.printStackTrace();
            }
            profile_img.setImageBitmap(cropToSquare(pickedPhoto));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_login, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.settings:
                Intent intent = new Intent(VisitingCardActivity.this, SettingsActivity.class);
                startActivity(intent);
                return true;
            case R.id.about:
                Intent aboutIntent = new Intent(VisitingCardActivity.this, AboutActivity.class);
                startActivity(aboutIntent);
                return true;
            case R.id.darktheme:
                SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
                boolean useDarkTheme = preferences.getBoolean(AboutActivity.PREF_DARK_THEME, false);
                if(!useDarkTheme)
                {
                    SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putBoolean(PREF_DARK_THEME, true);
                    editor.apply();
                }
                else {SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(this).edit();
                    editor.putBoolean(PREF_DARK_THEME, false);
                    editor.apply();}
                Intent restarter = getIntent();
                finish();
                startActivity(restarter);
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        populateCard(); //Update TextFields after change in settings
        super.onResume();
    }
    
    public void select_img(View view) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("Select image source")
                .setCancelable(false)
                .setPositiveButton("Gallery", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EZPhotoPickConfig config = new EZPhotoPickConfig();
                        config.photoSource = PhotoSource.GALLERY; // or PhotoSource.CAMERA
                        config.isAllowMultipleSelect = false; // only for GALLERY pick and API >18
                        config.storageDir = photoDir;
                        config.isGenerateUniqueName = true;
                        EZPhotoPick.startPhotoPickActivity(VisitingCardActivity.this, config);
                    }
                })
                .setNegativeButton("Camera", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int id) {
                        EZPhotoPickConfig config = new EZPhotoPickConfig();
                        config.photoSource = PhotoSource.CAMERA; // or PhotoSource.CAMERA
                        config.isAllowMultipleSelect = false; // only for GALLERY pick and API >18
                        config.storageDir = photoDir;
                        config.isGenerateUniqueName = true;
                        EZPhotoPick.startPhotoPickActivity(VisitingCardActivity.this, config);
                    }
                });
        AlertDialog alert = builder.create();
        alert.show();
    }

    private void populateCard() {
        Log.d(this.getLocalClassName(), "Populating card texts");
        SharedPreferences sharedPreferences = android.preference.PreferenceManager.getDefaultSharedPreferences(this);

        String imgName = sharedPreferences.getString("card_img","NOT_SET");
        if(!"NOT_SET".equals(imgName)){
            try {
                Bitmap bitmap = new EZPhotoPickStorage(this).loadStoredPhotoBitmap(photoDir, imgName);
                profile_img.setImageBitmap(cropToSquare(bitmap));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        nameText.setText(sharedPreferences.getString("card_name","Name"));
        phoneText.setText(sharedPreferences.getString("card_phone", "XXX-XXXX-XXX"));
        emailText.setText(sharedPreferences.getString("card_email", "name@domain.tld"));

        linkedInText.setText("www.linkedin.com/" + sharedPreferences.getString("card_linkedin","XXXXX"));
        twitterText.setText("www.twitter.com/" + sharedPreferences.getString("card_twitter","XXXXX"));
        gitHubText.setText("www.github.com/" + sharedPreferences.getString("card_github","XXXXX"));

        phoneText.setOnClickListener(view->{
            Intent intent = new Intent(Intent.ACTION_DIAL, Uri.parse("tel:" + phoneText.getText()));
            startActivity(intent);
        });

        emailText.setOnClickListener(view->{
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setType("text/plain");
            intent.putExtra(Intent.EXTRA_EMAIL, emailText.getText());
            startActivity(Intent.createChooser(intent, "Send Email"));
        });

        linkedInText.setOnClickListener(view->{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://" + linkedInText.getText().toString()));
            startActivity(i);
        });

        twitterText.setOnClickListener(view->{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://" + twitterText.getText().toString()));
            startActivity(i);
        });

        gitHubText.setOnClickListener(view->{
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse("https://" + gitHubText.getText().toString()));
            startActivity(i);
        });
    }

    private Bitmap cropToSquare(Bitmap bitmap){
      Bitmap dstBmp;
      if (bitmap.getWidth() >= bitmap.getHeight()){
        dstBmp = Bitmap.createBitmap(
            bitmap,
            bitmap.getWidth()/2 - bitmap.getHeight()/2,
            0,
            bitmap.getHeight(),
            bitmap.getHeight()
        );
      }else{
        dstBmp = Bitmap.createBitmap(
            bitmap,
            0,
            bitmap.getHeight()/2 - bitmap.getWidth()/2,
            bitmap.getWidth(),
            bitmap.getWidth()
        );
      }
      return dstBmp;
    }
}