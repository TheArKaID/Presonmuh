package id.ac.umy.unires.mh;

import android.app.ProgressDialog;
import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class login extends AppCompatActivity {

    EditText emailET;
    EditText passwordET;
    Button login;

    String email;
    String password;

    Map<String, Object> dataUser;

    FirebaseFirestore db;
    ProgressDialog checkBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        emailET = findViewById(R.id.EmailET);
        passwordET = findViewById(R.id.PassET);
        login = findViewById(R.id.LoginBtn);
        db = FirebaseFirestore.getInstance();
        dataUser = new HashMap();
        checkBar = new ProgressDialog(this);

        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                LoadingBarCheck();
                email = emailET.getText().toString();
                password = passwordET.getText().toString();

                Login(email, password);
            }
        });
    }

    private void Login(String email, String password) {
        String passHashed = md5(password);
        password = passHashed;
        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(this, "Harap Masukkan email dan password anda", Toast.LENGTH_LONG).show();
        } else {
            db.collection("users").whereEqualTo("email", email).whereEqualTo("password", password)
                    .get()
                    .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                        @Override
                        public void onComplete(@NonNull Task<QuerySnapshot> task) {
                            if (task.isSuccessful()) {
                                if (!task.getResult().isEmpty()) {
                                    for (QueryDocumentSnapshot document : task.getResult()) {
                                        dataUser.putAll(document.getData());
                                    }
                                    checkBar.dismiss();

                                    final Bundle bundle = new Bundle();

                                    for(Map.Entry<String, Object> entry : dataUser.entrySet()){
                                        bundle.putString(entry.getKey(), entry.getValue().toString());
                                    }

                                    Intent mainIntent = new Intent(login.this, MainActivity.class);
                                    mainIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK|Intent.FLAG_ACTIVITY_NEW_TASK);
                                    startActivity(mainIntent);

                                } else {
                                    checkBar.dismiss();
                                    Toast.makeText(login.this, "Email dan Password anda tidak sesuai", Toast.LENGTH_LONG).show();
                                }
                            } else {
                                checkBar.dismiss();
                                Log.w("FragmentActivity", "Error getting documents.", task.getException());
                            }
                        }
                    });

        }

    }

    private void LoadingBarCheck() {
        checkBar.setTitle("Please Wait...");
        checkBar.setMessage("While We're Checking your Data");
        checkBar.show();
    }

    public static final String md5(final String toEncrypt) {
        String result = "";
        try {
            final MessageDigest digest = MessageDigest.getInstance("md5");
            digest.update(toEncrypt.getBytes());
            final byte[] bytes = digest.digest();
            final StringBuilder sb = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                sb.append(String.format("%02X", bytes[i]));
            }
            result = sb.toString().toLowerCase();
            return result;
        } catch (Exception exc) {
            return result; // Impossibru!
        }
    }
}
