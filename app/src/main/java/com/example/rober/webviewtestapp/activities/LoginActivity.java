package com.example.rober.webviewtestapp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.LoaderManager.LoaderCallbacks;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.rober.webviewtestapp.R;
import com.example.rober.webviewtestapp.tools.CustomSnackbars;
import com.example.rober.webviewtestapp.tools.FileManager;
import com.example.rober.webviewtestapp.tools.HashTool;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;


/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity implements LoaderCallbacks<Cursor> {

    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    /**
     * A dummy authentication store containing known user names and passwords.
     * TODO: remove after connecting to a real authentication system.
     */
    private static final String[] DUMMY_CREDENTIALS = new String[]{
            "foo@example.com:hello", "bar@example.com:world"
    };
    /**
     * Keep track of the login task to ensure we can cancel it if requested.
     */
    private UserLoginTask mAuthTask = null;

    // UI references.
    private AutoCompleteTextView mLoginView;
    private EditText mPasswordView;
    private View mProgressView;
    private View mLoginFormView;
    private Button mButton;
    private ImageView mImage1, mImage2;
    private TextView myTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //DELETE IN FINAL APP - NOT ASKED BY CLIENT toto je redundantne textove pole ktorym testujem ci viem zapisovat do textoveho pola ci uz pridavat nejaky text po sltaceni tlacitka alebo premenit ho za base64 text string obrazku
        myTextView = (TextView) findViewById(R.id.textViewPicture);
        myTextView.setText("This text will be changed into base64 image source text");

        //DELETE IN FINAL APP - NOT ASKED BY CLIENT listener pre tlacitko ktore zmazem ked nebude potrebne
        addListenerOnButton();

        // Set up the login form.
        mLoginView = (AutoCompleteTextView) findViewById(R.id.login);
        populateAutoComplete();

        mPasswordView = (EditText) findViewById(R.id.password);
        mPasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        //sing in button used for POST request and download the base64 image
        Button mSignInDownloadButton = (Button) findViewById(R.id.sign_in_download_button);
        mSignInDownloadButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                doWhatIWantAndDeleteMeAfterUnnecessary();

                //origos metoda na prihlasovanie treba spravit nieco vlastne maybe DELETE IN FINAL APP - NOT ASKED BY CLIENT
                attemptLogin();
            }
        });

        //MOVE INTO onClick method IN FINAL APP - ASKED BY CLIENT testovaci button na post request potom to presun do metody alebo triedy a pod spravny onclicklistener
        Button postButton = findViewById(R.id.btnPostRequest);
        postButton.setOnClickListener(new OnClickListener() {

              @Override
              public void onClick(View arg0) {
                  String login = mLoginView.getText().toString();
                  String pass = mPasswordView.getText().toString();
                  
                  //i am just lazy to write the text into fields repeatably :D lol
                  if (login.length() == 0) {
                      login = "mitosinka";
                  }
                  if (pass.length() == 0) {
                      pass = "milan";
                  }
                  
                  //calling getter for parsing the password into SHA1 hash
                  try {
                      pass = (String) HashTool.getSHA1Hash(pass);
                  } catch (NoSuchAlgorithmException e) {
                      e.printStackTrace();
                  } catch (UnsupportedEncodingException e) {
                      e.printStackTrace();
                  }

                  //calling method for post request
                  String acceptedBase64ImageStr = executeLink("https://mobility.cleverlance.com/download/bootcamp/image.php",);
                  Log.i("PostRequest", "11 message from server \n" + acceptedBase64ImageStr);
                  myTextView.setText(acceptedBase64ImageStr);

                  //calling my custom snackbar
                  String finalText = "login: " + login + " " + "pass: " + pass  + "\n" + "trying to get post requet" ;
                  CustomSnackbars.getSnackbarDismissable(LoginActivity.this, findViewById(R.id.activityLoginCoordinatorLayout), finalText, "OK");
              }
          });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    //method used for post request
    private String executeLink(String link, String urlParameters) throws Exception{

        String response = null;
        try {

            byte[] postData = urlParameters.getBytes(StandardCharsets.UTF_8);
            int postDataLength = postData.length;
            Log.i("PostRequest", "01 PostData > " + postData);
            Log.i("PostRequest", "02 PostDataLength > " + postDataLength);
            URL url = null;
            try {
                url = new URL(link);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            Log.i("PostRequest", "03 setting connection");
            connection.setDoOutput(true);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestProperty("charset", "utf-8");
            connection.setRequestProperty("Content-Length", Integer.toString(postDataLength));
            connection.setFixedLengthStreamingMode(postDataLength);
            connection.setUseCaches(false);

            Log.i("PostRequest", "04 inserting data into outputStream");
            DataOutputStream outStream = new DataOutputStream(connection.getOutputStream());
            outStream.write(postData);

            Log.i("PostRequest", "05 creating connection");
            connection.connect();

            Log.i("PostRequest", "06 checking connection status");
            if (connection.getResponseCode() == 200) {
                Log.i("PostRequest", "07.1 connection OK");
                Log.i("PostRequest", "08 flushing outputStream");
                outStream.flush();
                Log.i("PostRequest", "09 closing outputStream");
                outStream.close();
                CustomSnackbars.getSnackbarDismissable(LoginActivity.this, findViewById(R.id.activityLoginCoordinatorLayout), "POST request sent", "OK");

            } else if (connection.getResponseCode() == -1) {
                Log.i("PostRequest", "07.2 the response is not valid HTTP");
                CustomSnackbars.getSnackbarDismissable(LoginActivity.this, findViewById(R.id.activityLoginCoordinatorLayout), "the response is not valid HTTP", "OK");

            } else {
                Log.i("PostRequest", "07.3 connection Unauthorized");
                CustomSnackbars.getSnackbarDismissable(LoginActivity.this, findViewById(R.id.activityLoginCoordinatorLayout), "connection rejected", "OK");
            }

            if(connection != null) { // Make sure the connection is not null.
                Log.i("PostRequest", "10 disconnectinG");
                connection.disconnect();
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        return response;
    }

    //toto cele zmaz ked to nebude potrebne - button treti v poradi
    private void addListenerOnButton() {
        mImage1 = (ImageView) findViewById(R.id.imageView1);
        mImage2 = (ImageView) findViewById(R.id.imageView2);

        mButton = (Button) findViewById(R.id.btnChangeImage);
        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                String login = mLoginView.getText().toString();
                String pass = mPasswordView.getText().toString();
                if (login.length() == 0) {
                    login = "mitosinka";
                }
                if (pass.length() == 0) {
                    pass = "milan";
                }

                //calling getter for parsing the password into SHA1 hash
                try {
                    pass = (String) HashTool.getSHA1Hash(pass);
                } catch (NoSuchAlgorithmException e) {
                    e.printStackTrace();
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

                String finalText = "login: " + login + " \n " + "pass: " + pass + System.getProperty("line.separator") + myTextView.getText().toString();
                myTextView.setText(finalText);
            }
        });
    }

    //DELETE IN FINAL APP - NOT ASKED BY CLIENT metoda ktora mi nacita nejaky subor co mam v adresari RES/RAW a preparsuje ho do base64 formatu, nasledne vypise text do textoveho pola
    private void doWhatIWantAndDeleteMeAfterUnnecessary() {
        //calling method for reading from the file - inside res/raw
        String mBase64String = FileManager.readStringFromResource(LoginActivity.this, R.raw.earth_shadow_base64);
        //splitting the super long base64 raw formatting, cutting first part which is unnecessary for decoding
        String base64Image = mBase64String.split(",")[1];

        //decoding byte 64
        byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
        Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

        //resetting the images and text view
        mImage1.setImageResource(R.drawable.earth_shadow);
        mImage2.setImageBitmap(decodedByte);
        myTextView.setText(base64Image);
    }



    /*DELETE IN FINAL APP - clean it - NOT ASKED BY CLIENT*/



    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }
        getLoaderManager().initLoader(0, null, this);
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (checkSelfPermission(READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mLoginView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }


    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {
        if (mAuthTask != null) {
            return;
        }

        // Reset errors.
        mLoginView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mLoginView.getText().toString();
        String password = mPasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        // Check for a valid password, if the user entered one.
        if (!TextUtils.isEmpty(password) && !isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
        }

        // Check for a valid email address.
        if (TextUtils.isEmpty(email)) {
            mLoginView.setError(getString(R.string.error_field_required));
            focusView = mLoginView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mLoginView.setError(getString(R.string.error_invalid_email));
            focusView = mLoginView;
            cancel = true;
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.
            showProgress(true);
            mAuthTask = new UserLoginTask(email, password);
            mAuthTask.execute((Void) null);
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Replace this with your own logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Replace this with your own logic
        return password.length() > 4;
    }

    /**
     * Shows the progress UI and hides the login form.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
    private void showProgress(final boolean show) {
        // On Honeycomb MR2 we have the ViewPropertyAnimator APIs, which allow
        // for very easy animations. If available, use these APIs to fade-in
        // the progress spinner.
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
            int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
            mLoginFormView.animate().setDuration(shortAnimTime).alpha(
                    show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
                }
            });

            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mProgressView.animate().setDuration(shortAnimTime).alpha(
                    show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
                @Override
                public void onAnimationEnd(Animator animation) {
                    mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
                }
            });
        } else {
            // The ViewPropertyAnimator APIs are not available, so simply show
            // and hide the relevant UI components.
            mProgressView.setVisibility(show ? View.VISIBLE : View.GONE);
            mLoginFormView.setVisibility(show ? View.GONE : View.VISIBLE);
        }
    }

    @Override
    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(this,
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    @Override
    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(LoginActivity.this,
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mLoginView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }

    /**
     * Represents an asynchronous login/registration task used to authenticate
     * the user.
     */
    public class UserLoginTask extends AsyncTask<Void, Void, Boolean> {

        private final String mEmail;
        private final String mPassword;

        UserLoginTask(String email, String password) {
            mEmail = email;
            mPassword = password;
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            // TODO: attempt authentication against a network service.

            try {
                // Simulate network access.
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                return false;
            }

            for (String credential : DUMMY_CREDENTIALS) {
                String[] pieces = credential.split(":");
                if (pieces[0].equals(mEmail)) {
                    // Account exists, return true if the password matches.
                    return pieces[1].equals(mPassword);
                }
            }

            // TODO: register the new account here.
            return true;
        }

        @Override
        protected void onPostExecute(final Boolean success) {
            mAuthTask = null;
            showProgress(false);

            if (success) {
                finish();
            } else {
                mPasswordView.setError(getString(R.string.error_incorrect_password));
                mPasswordView.requestFocus();
            }
        }

        @Override
        protected void onCancelled() {
            mAuthTask = null;
            showProgress(false);
        }
    }
}

