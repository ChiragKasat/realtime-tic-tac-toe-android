package androidsamples.java.tictactoe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavDirections;
import androidx.navigation.Navigation;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class LoginFragment extends Fragment {
    private FirebaseAuth mAuth;
    private DatabaseReference mDatabase;
    private EditText mEmail, mPassword;
    private View view;

    private final String TAG = "LoginActivity";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    private void makeToast(String err){
        Toast.makeText(getContext(), err, Toast.LENGTH_SHORT).show();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_login, container, false);
        mAuth = FirebaseAuth.getInstance();
        mDatabase = FirebaseDatabase.getInstance().getReference();
        mEmail = view.findViewById(R.id.edit_email);
        mPassword = view.findViewById(R.id.edit_password);

        view.findViewById(R.id.btn_log_in)
                .setOnClickListener(v -> {
                    if(mEmail.getText().toString().isEmpty()) {
                        mEmail.requestFocus();
                        makeToast("Email is required");
                        return;
                    }
                    if(mPassword.getText().toString().isEmpty()) {
                        mPassword.requestFocus();
                        makeToast("Password is required.");
                        return;
                    }

                    mAuth.createUserWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    // Sign in success, update UI with the signed-in user's information
                                    FirebaseUser user = mAuth.getCurrentUser();

                                    User currentUser = new User(user.getEmail());
                                    mDatabase.child("users").child(user.getUid()).setValue(currentUser);

                                    Log.d(TAG, user.toString());
                                    NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                                    Navigation.findNavController(view).navigate(action);
                                } else {
                                    // If sign in fails, display a message to the user.
                                    Log.d(TAG, "signInWithEmail:failure", task.getException());
                                    if(task.getException() instanceof FirebaseAuthUserCollisionException){
                                        loginUser();
                                    }
                                    else{
                                        String msg = task.getException().getMessage();
                                        makeToast(msg);
                                    }
                                }
                            });

                });

        return view;
    }

    private void loginUser(){
        mAuth.signInWithEmailAndPassword(mEmail.getText().toString(), mPassword.getText().toString())
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();

                        Log.d(TAG, user.toString());
                        NavDirections action = LoginFragmentDirections.actionLoginSuccessful();
                        Navigation.findNavController(view).navigate(action);
                    } else {
                        // If sign in fails, display a message to the user.
                        Log.d(TAG, "signInWithEmail:failure", task.getException());
                            String msg = task.getException().getMessage();
                            makeToast(msg);
                            mPassword.requestFocus();
                    }
                });
    }

    // No options menu in login fragment.
}