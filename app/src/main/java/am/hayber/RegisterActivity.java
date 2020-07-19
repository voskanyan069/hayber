package am.hayber;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class RegisterActivity extends AppCompatActivity {

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private final FirebaseDatabase root = FirebaseDatabase.getInstance();
    private final DatabaseReference usersRef = root.getReference().child("Users");

    private Button createAccountBtn;
    private EditText userEmail;
    private EditText userPass;
    private TextView alreadyHaveAnAccount;

    private ProgressDialog loadingBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        init();
        buttonClick();
    }

    private void init() {
        createAccountBtn = findViewById(R.id.reg_btn);
        userEmail = findViewById(R.id.reg_email);
        userPass = findViewById(R.id.reg_pass);
        alreadyHaveAnAccount = findViewById(R.id.already_have_an_account_link);

        loadingBar = new ProgressDialog(this);
    }

    private void buttonClick() {
        createAccountBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                createNewAccount();
            }
        });

        alreadyHaveAnAccount.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendUserToLoginActivity();
            }
        });
    }

    private void createNewAccount() {
        String email = userEmail.getText().toString();
        String pass = userPass.getText().toString();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, "Please enter email ...", Toast.LENGTH_SHORT).show();
        }
        if (TextUtils.isEmpty(pass)) {
            Toast.makeText(this, "Please enter password ...", Toast.LENGTH_SHORT).show();
        } else {
            loadingBar.setTitle("Creating new Account");
            loadingBar.setMessage("Please wait ... (Click outside to cancel account creating)");
            loadingBar.setCanceledOnTouchOutside(true);
            loadingBar.show();

            mAuth.createUserWithEmailAndPassword(email, pass)
                    .addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                String currentUserID = mAuth.getCurrentUser().getUid();
                                usersRef.child(currentUserID).setValue("");

                                sendUserToMainActivity();
                                Toast.makeText(RegisterActivity.this, "Account created successfully", Toast.LENGTH_SHORT).show();
                                loadingBar.dismiss();
                            } else {
                                String errMessage = task.getException().toString();
                                Toast.makeText(RegisterActivity.this, "Error " + errMessage, Toast.LENGTH_LONG).show();
                                loadingBar.dismiss();
                            }
                        }
                    });
        }
    }

    private void sendUserToMainActivity() {
        Intent mainIntent = new Intent(RegisterActivity.this, MainActivity.class);
        mainIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(mainIntent);
        finish();
    }

    private void sendUserToLoginActivity() {
        Intent loginIntent = new Intent(RegisterActivity.this, LoginActivity.class);
        startActivity(loginIntent);
    }
}