package am.hayber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.firebase.ui.auth.ui.phone.PhoneActivity;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseException;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;

import java.util.concurrent.TimeUnit;

public class PhoneLoginActivity extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private PhoneAuthProvider.OnVerificationStateChangedCallbacks callbacks;
    private String mVerificationId;
    private PhoneAuthProvider.ForceResendingToken mResendToken;

    private ProgressDialog loadingBar;

    private EditText phoneNumberInput;
    private EditText verifyCodeInput;
    private Button sendCodeBtn;
    private Button checkCodeBtn;

    private ImageView phoneNumberImg;
    private ImageView phoneVerifyImg;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_phone_login);

        init();
        buttonsClick();
        callback();
    }

    private void init() {
        loadingBar = new ProgressDialog(this);

        phoneNumberInput = findViewById(R.id.phone_login_phone);
        verifyCodeInput = findViewById(R.id.phone_verification);
        sendCodeBtn = findViewById(R.id.send_verification_code_btn);
        checkCodeBtn = findViewById(R.id.check_verification_code_btn);

        phoneNumberImg = findViewById(R.id.phone_img);
        phoneVerifyImg = findViewById(R.id.phone_verification_img);
    }

    private void buttonsClick() {
        sendCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String phoneNumber = phoneNumberInput.getText().toString();

                if (TextUtils.isEmpty(phoneNumber)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please enter the phone number", Toast.LENGTH_LONG).show();
                } else {
                    loadingBar.setTitle("Phone Verification");
                    loadingBar.setMessage("Please wait, while we are authenticating using your phone number (Click outside to cancel verification)");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    PhoneAuthProvider.getInstance().verifyPhoneNumber(
                            phoneNumber,        // Phone number to verify
                            60,                 // Timeout duration
                            TimeUnit.SECONDS,   // Unit of timeout
                            PhoneLoginActivity.this,               // Activity (for callback binding)
                            callbacks);        // OnVerificationStateChangedCallbacks
                }
            }
        });

        checkCodeBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String code = verifyCodeInput.getText().toString();

                if (TextUtils.isEmpty(code)) {
                    Toast.makeText(PhoneLoginActivity.this, "Please firstly enter the verification code", Toast.LENGTH_LONG).show();
                } else {
                    loadingBar.setTitle("Verification Code");
                    loadingBar.setMessage("Please wait, while we are verification code ... (Click outside to cancel verification)");
                    loadingBar.setCanceledOnTouchOutside(true);
                    loadingBar.show();

                    PhoneAuthCredential credential = PhoneAuthProvider.getCredential(mVerificationId, code);
                    signInWithPhoneAuthCredential(credential);
                }
            }
        });
    }

    private void callback() {
        callbacks = new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onVerificationCompleted(@NonNull PhoneAuthCredential phoneAuthCredential) {
                signInWithPhoneAuthCredential(phoneAuthCredential);
            }

            @Override
            public void onVerificationFailed(@NonNull FirebaseException e) {
                loadingBar.dismiss();
                Toast.makeText(PhoneLoginActivity.this, "Invalid Phone Number, Please enter the correct number with your country code ...", Toast.LENGTH_LONG).show();

                verifyCodeInput.setVisibility(View.INVISIBLE);
                phoneVerifyImg.setVisibility(View.INVISIBLE);
                checkCodeBtn.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onCodeSent(@NonNull String verificationId,
                                   @NonNull PhoneAuthProvider.ForceResendingToken token) {

                mVerificationId = verificationId;
                mResendToken = token;

                loadingBar.dismiss();

                Toast.makeText(PhoneLoginActivity.this, "Code has been sent to " + phoneNumberInput.getText().toString() + " number, please check and verify ...", Toast.LENGTH_LONG).show();

                verifyCodeInput.setVisibility(View.VISIBLE);
                phoneVerifyImg.setVisibility(View.VISIBLE);
                checkCodeBtn.setVisibility(View.VISIBLE);
            }
        };
    }

    private void signInWithPhoneAuthCredential(PhoneAuthCredential credential) {
        mAuth.setLanguageCode("am");
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            loadingBar.dismiss();
                            Toast.makeText(PhoneLoginActivity.this, "You are successfully logged", Toast.LENGTH_LONG).show();
                            sendUserToMainActivity();
                        } else {
                            loadingBar.dismiss();
                            String errMessage = task.getException().toString();
                            Toast.makeText(PhoneLoginActivity.this, "Error " + errMessage, Toast.LENGTH_LONG).show();
                        }
                    }
                });
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(PhoneLoginActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }
}