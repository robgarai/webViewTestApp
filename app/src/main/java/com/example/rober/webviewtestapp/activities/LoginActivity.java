package com.example.rober.webviewtestapp.activities;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.NonNull;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.app.LoaderManager.LoaderCallbacks;

import android.content.CursorLoader;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;

import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
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
import com.example.rober.webviewtestapp.tools.FileManager;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import static android.Manifest.permission.READ_CONTACTS;
import static android.content.ContentValues.TAG;

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
    private AutoCompleteTextView mEmailView;
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

        myTextView = (TextView) findViewById(R.id.textViewPicture);
        myTextView.setText("This text will be changed into base64 image source text");

        addListenerOnButton();

        // Set up the login form.
        mEmailView = (AutoCompleteTextView) findViewById(R.id.email);
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

        Button mEmailSignInButton = (Button) findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        mLoginFormView = findViewById(R.id.login_form);
        mProgressView = findViewById(R.id.login_progress);
    }

    private void addListenerOnButton() {
        mImage1 = (ImageView) findViewById(R.id.imageView1);
        mImage2 = (ImageView) findViewById(R.id.imageView2);

        mButton = (Button) findViewById(R.id.btnChangeImage);
        mButton.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View arg0) {
                //FileManager.readStringFromResource(null,R.raw.earth_shadow_base64);

/*                  // this is a chunk of code which works but i want it to have it in separate method readStringFromResource in separate class FileManager
                    // but when i make it happen the method wont work with input parameters just the R.raw.earth_shadow_base64
                    try {

                        InputStream inputStream = getResources().openRawResource(R.raw.earth_shadow_base64);
                        InputStreamReader inputStreamReader = new InputStreamReader (inputStream) ;
                        BufferedReader bufferedReader = new BufferedReader(inputStreamReader);

                        StringBuilder stringBuilder = new StringBuilder();

                        try {
                            String line = null;
                            while (( line = bufferedReader.readLine()) != null){
                                stringBuilder.append(line);
                            }
                        }
                        finally {
                            bufferedReader.close();
                        }
                        inputStream.close();
                        myTextView.setText(stringBuilder.toString());

                    }

                    catch (FileNotFoundException ex) {
                        Log.e(TAG, "Couldn't find the file " +  " " + ex);
                        ex.printStackTrace();                }

                    catch (IOException ex){
                        Log.e(TAG, "Error reading file " + " " + ex);
                        ex.printStackTrace();                }
*/

                String mBase64String = FileManager.readStringFromResource(R.raw.earth_shadow_base64);
                String base64String = "data:image/jpeg;base64,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";
                String base64Image = mBase64String.split(",")[1];

                byte[] decodedString = Base64.decode(base64Image, Base64.DEFAULT);
                Bitmap decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);


                mImage1.setImageResource(R.drawable.earth_shadow);
                // mImage2.setImageResource(R.drawable.earth_shadow);
                mImage2.setImageBitmap(decodedByte);

            }

        });
    }

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
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
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
        mEmailView.setError(null);
        mPasswordView.setError(null);

        // Store values at the time of the login attempt.
        String email = mEmailView.getText().toString();
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
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
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

        mEmailView.setAdapter(adapter);
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

