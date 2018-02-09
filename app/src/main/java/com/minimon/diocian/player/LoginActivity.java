package com.minimon.diocian.player;


import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.NavUtils;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.Toast;

import com.kakao.auth.ISessionCallback;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.ApiErrorCode;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.MeResponseCallback;
import com.kakao.usermgmt.response.model.UserProfile;
import com.kakao.util.exception.KakaoException;
import com.kakao.util.helper.log.Logger;

import org.json.JSONObject;

public class LoginActivity extends AppCompatActivity {
    private String TAG = "LoginActivity";

    private MinimonUser minimonUser;
    private NaverLogin naverLogin;
    private SessionCallback callback;   // for kakao
    private LoginButton btn_kakao_login;

    private static Context mContext;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mContext = this;

        init();
    }

    public void init() {
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.mipmap.a001_top_back);

//        setImageInButton(R.mipmap.a001_social_naver, R.id.btnNaver);
//        setImageInButton(R.mipmap.a001_social_kakao, R.id.btnKakao);
//        setImageInButton(R.mipmap.a001_social_facebook, R.id.btnFacebook);
//        setImageInButton(R.mipmap.a001_social_google, R.id.btnGoogle);

        initAutoLogin();

        initMinomon();
        initNaver();
        initKakao();
    }

    private void initMinomon() {
        minimonUser = new MinimonUser();
        minimonUser.setListener(new MinimonUser.MinimonLoginListener() {
            @Override
            public void onLogined(JSONObject info) {
                Log.d("LoginActivity:", "onLogined: " + info);
            }
        });
    }

    private void initNaver() {
        naverLogin = new NaverLogin(mContext);
        naverLogin.setListener(new NaverLogin.NaverLoginListener() {
            @Override
            public void onLogined(String uid, String email) {
                newMemberSNS("naver", "naver_" + uid, email);
            }
        });

        Button loginButton = findViewById(R.id.btnNaver);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                naverLogin.forceLogin();
            }
        });
    }

    private void initKakao() {
        callback = new SessionCallback();
        Session.getCurrentSession().addCallback(callback);
//        Session.getCurrentSession().checkAndImplicitOpen();

        btn_kakao_login = findViewById(R.id.login_button_activity);
        Button loginButton = findViewById(R.id.btnKakao);
        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                btn_kakao_login.performClick();
            }
        });
    }

    private void initAutoLogin() {
        CheckBox checkBox = findViewById(R.id.cbAutoLogin);
        checkBox.setChecked(loadAutoLogin());

        checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (buttonView.getId() == R.id.cbAutoLogin) {
                    saveAutoLogin(isChecked);
                }
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            //finish();
            NavUtils.navigateUpFromSameTask(LoginActivity.this);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
            return;
        }

        super.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Session.getCurrentSession().removeCallback(callback);
    }

    private class SessionCallback implements ISessionCallback {

        @Override
        public void onSessionOpened() {
            requestMe();
        }

        @Override
        public void onSessionOpenFailed(KakaoException exception) {
            if(exception != null) {
                Logger.e(exception);
            }
        }
    }

    /**
     * 사용자의 상태를 알아 보기 위해 me API 호출을 한다.
     */
    protected void requestMe() {
        UserManagement.getInstance().requestMe(new MeResponseCallback() {
            @Override
            public void onFailure(ErrorResult errorResult) {
                String message = "failed to get user info. msg=" + errorResult;
                Log.d(TAG,message);

                int result = errorResult.getErrorCode();
                if (result == ApiErrorCode.CLIENT_ERROR_CODE) {
                    Toast.makeText(getApplicationContext(), getString(R.string.error_message_for_service_unavailable), Toast.LENGTH_SHORT).show();
                } else {
                    redirectLoginActivity();
                }
            }

            @Override
            public void onSessionClosed(ErrorResult errorResult) {
                redirectLoginActivity();
            }

            @Override
            public void onSuccess(UserProfile userProfile) {
                Log.d(TAG,"UserProfile : " + userProfile);
                newMemberSNS("kakao", "kakao_" + userProfile.getId(), userProfile.getEmail());
            }

            @Override
            public void onNotSignedUp() {
                showSignup();
            }
        });
    }

    private void redirectLoginActivity() {
        Log.d(TAG,"redirectLoginActivity");
    }

    private void redirectMainActivity() {
        Log.d(TAG,"redirectMainActivity");
    }

    private void showSignup() {
        Log.d(TAG,"showSignup");
    }

    public void setImageInButton(int drawableID, int btnID) {
        Drawable drawable = ContextCompat.getDrawable(LoginActivity.this, drawableID);
        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());

        Button btn = (Button) findViewById(btnID);
        btn.setCompoundDrawables(drawable, null, null, null);
    }

    public void onClickLogin(View view) {
        Log.d("LoginActivity:", "onClickLogin start");

        ContentValues loginInfo = new ContentValues();
        loginInfo.put("type", "basic");
        loginInfo.put("id", "hjina");
        loginInfo.put("value", "12345678");
        loginInfo.put("device_id", DeviceUuidFactory.getDeviceUuid(this.getApplicationContext()));

        minimonUser.login(loginInfo);
    }

    public void onClickFindMember(View view) {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), FindMemberActivity.class);
        startActivity(intent);
    }

    public void onClickNewMember(View view) {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), NewMemberActivity.class);
        intent.putExtra("type", "basic");
        startActivity(intent);
    }

    private void newMemberSNS(String nameSNS, String uid, String email) {
        Intent intent = new Intent(LoginActivity.this.getApplicationContext(), NewMemberActivity.class);
        intent.putExtra("type", nameSNS);
        intent.putExtra("uid", uid);
        intent.putExtra("email", email);
        startActivity(intent);
    }

    private void saveAutoLogin(boolean isAuto) {
        SharedPreferences prefs = getSharedPreferences("minimon_preference", MODE_PRIVATE);
        SharedPreferences.Editor editor = prefs.edit();
        editor.putString("AutoLogin", isAuto? "1":"0");
        editor.commit();
    }

    public boolean loadAutoLogin() {
        SharedPreferences prefs = getSharedPreferences("minimon_preference", MODE_PRIVATE);
        if (prefs.getString("AutoLogin", "0").equals("1"))
            return true;

        return false;
    }
}
