package com.example.dev;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.firebase.ui.auth.AuthUI;
import com.google.android.gms.auth.api.Auth;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.auth.api.signin.GoogleSignInResult;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.GoogleApiClient.ConnectionCallbacks;
import com.google.android.gms.common.api.GoogleApiClient.OnConnectionFailedListener;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firestore.admin.v1beta1.Progress;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
//import com.yarolegovich.lovelydialog.LovelyInfoDialog;
//import com.yarolegovich.lovelydialog.LovelyProgressDialog;

import org.w3c.dom.NameList;

import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends AppCompatActivity{
    private EditText email;
    private EditText password;
    private ProgressBar progressBar;
    private final int SIGN_IN_REQUEST_CODE = 1;
    //RC_SIGN in是您为启动新活动而分配的请求代码, can be any number
    //Google Login Request Code
    private final int RC_SIGN_IN = 7;
    //declare mGoogleApiClient for sign in using GOOGLE
    private GoogleApiClient mGoogleApiClient;
    GoogleSignInClient mGoogleSignInClient;

    //dev nayak
    private static String TAG = "LoginActivity";
    FloatingActionButton fab;
    private final Pattern VALID_EMAIL_ADDRESS_REGEX =
            Pattern.compile("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,6}$", Pattern.CASE_INSENSITIVE);
    private EditText editTextUsername, editTextPassword;
   // private LovelyProgressDialog waitingDialog;

    private AuthUtils authUtils;
    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private FirebaseUser user;
    private boolean firstTimeAccess;

    @Override
    protected void onStart() {
        super.onStart();
        mAuth.addAuthStateListener(mAuthListener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);        //hide the bar
        requestWindowFeature(Window.FEATURE_NO_TITLE);        //hide the title bar
        getSupportActionBar().hide();        //enable full screen
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        FirebaseApp.initializeApp(this);
        // Check if user is signed in (non-null) and update UI accordingly.

        // Initialize Firebase Auth
        mAuth = FirebaseAuth.getInstance();
        FirebaseUser currentUser = mAuth.getCurrentUser();
        if (currentUser.getEmail()!=null){
            showToast(currentUser.getEmail());
        }

        setContentView(R.layout.activity_main);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        editTextUsername = (EditText) findViewById(R.id.username);
        editTextPassword = (EditText) findViewById(R.id.password);
        firstTimeAccess = true;
        initFirebase();
    }

    private void initFirebase() {
        mAuth = FirebaseAuth.getInstance();
        authUtils = new AuthUtils();
        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    // User is signed in
                    StaticConfig.UID = user.getUid();
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());
                    if (firstTimeAccess) {
                        startActivity(new Intent(MainActivity.this, MainActivity.class));
                        MainActivity.this.finish();
                    }
                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                firstTimeAccess = false;
            }
        };

        //waitingDialog = new LovelyProgressDialog(this).setCancelable(false);
    }
    @Override
    protected void onStop() {
        super.onStop();
        if (mAuthListener != null) {
            mAuth.removeAuthStateListener(mAuthListener);
        }
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == StaticConfig.REQUEST_CODE_REGISTER && resultCode == RESULT_OK) {
            authUtils.createUser(data.getStringExtra(StaticConfig.STR_EXTRA_USERNAME), data.getStringExtra(StaticConfig.STR_EXTRA_PASSWORD));
        }
    }




    //sign in with google account
    public void btn_sign_in_google(View view){
        String username = editTextUsername.getText().toString();
        String password = editTextPassword.getText().toString();
        if (validate(username, password)) {
            authUtils.signIn(username, password);
        } else {
            Toast.makeText(this, "Invalid email or empty password", Toast.LENGTH_SHORT).show();
        }
    }
    //methods for sign in with google account
    @Override
    public void onBackPressed() {
        super.onBackPressed();
        setResult(RESULT_CANCELED, null);
        finish();
    }

    private boolean validate(String emailStr, String password) {
        Matcher matcher = VALID_EMAIL_ADDRESS_REGEX.matcher(emailStr);
        return (password.length() > 0 || password.equals(";")) && matcher.find();
    }

    //sign in
    public void btn_sign_in_email(View view){
        this.email = (EditText) findViewById(R.id.email);
        this.password = (EditText) findViewById(R.id.password);
        progressBar.setVisibility(View.VISIBLE);
        //no input email
        if(this.email.getText()==null){
            showToast("Please input your email");
        }
        //no input password
        if(this.password.getText()==null){
            showToast("Please input your password");
        }
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        }else{
            // User is already signed in. Therefore, display
            // a welcome Toast
            showToast("Welcome " + FirebaseAuth.getInstance().getCurrentUser().getDisplayName());

            // Load chat room contents
            displayChatMessages();
        }
    }

    //register
    public void btn_register(View view){
        getWindow().setExitTransition(null);
        getWindow().setEnterTransition(null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            ActivityOptions options =
                    ActivityOptions.makeSceneTransitionAnimation(this, fab, fab.getTransitionName());
            startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER, options.toBundle());
        } else {
            startActivityForResult(new Intent(this, RegisterActivity.class), StaticConfig.REQUEST_CODE_REGISTER);
        }

        /*
        if(FirebaseAuth.getInstance().getCurrentUser() == null) {
            // Start sign in/sign up activity
            startActivityForResult(
                    AuthUI.getInstance()
                            .createSignInIntentBuilder()
                            .build(),
                    SIGN_IN_REQUEST_CODE
            );
        }*/
    }

    private void displayChatMessages(){
        Intent intent = new Intent(this, NameListActivity.class);
        startActivity(intent);
    }
    private void showToast(String str){
        Toast.makeText(this, str,Toast.LENGTH_SHORT);
    }
}
