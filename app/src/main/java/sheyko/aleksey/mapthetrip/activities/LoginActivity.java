package sheyko.aleksey.mapthetrip.activities;

import android.app.Activity;

public class LoginActivity extends Activity {

//    private EditText usernameField;
//    private EditText passwordField;
//
//    private String mUsername;
//    private String mPassword;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_login);
//
//        usernameField = (EditText) findViewById(R.id.usernameField);
//        passwordField = (EditText) findViewById(R.id.passwordField);
//        passwordField.setTransformationMethod(new PasswordTransformationMethod());
//
//        Button loginButton = (Button) findViewById(R.id.loginButton);
//        loginButton.setOnClickListener(
//                new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mUsername = usernameField.getText().toString().trim();
//                        mPassword = passwordField.getText().toString().trim();
//
//                        if (anyBlankField()) return;
//
//                        ParseUser.logInInBackground(mUsername, mPassword, new LogInCallback() {
//                            @Override
//                            public void done(ParseUser parseUser, ParseException error) {
//                                if (error == null) {
//                                    // Hooray! Now user can go to Main Activity.
//                                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                    intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                    startActivity(intent);
//                                } else {
//                                    // Sign up didn't succeed
//                                    String rawMessage = error.getMessage();
//                                    String mMessage = rawMessage.substring(0,1).toUpperCase() + rawMessage.substring(1);
//                                    makeToast(mMessage);
//                                }
//                            }
//                        });
//                    }
//
//                    private boolean anyBlankField() {
//                        if (mUsername.equals("")) {
//                            makeToast(getString(R.string.username_missing));
//                            return true;
//                        }
//                        if (mPassword.equals("")) {
//                            makeToast(getString(R.string.password_missing));
//                            return true;
//                        }
//                        return false;
//                    }
//                }
//
//        );
//
//        TextView goToSignUpButton = (TextView) findViewById(R.id.goToSignUpButton);
//        goToSignUpButton.setOnClickListener(new OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                startActivity(new Intent(LoginActivity.this, SignUpActivity.class));
//            }
//        });
//    }
//
//    private Void makeToast(String message) {
//        Toast.makeText(LoginActivity.this, message, Toast.LENGTH_SHORT).show();
//        return null;
//    }
}
