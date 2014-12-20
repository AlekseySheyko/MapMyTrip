package sheyko.aleksey.mapmytrip.ui;

import android.app.Activity;

public class SignUpActivity extends Activity {

//    private EditText emailField;
//    private EditText usernameField;
//    private EditText passwordField;
//
//    private String mEmail;
//    private String mUsername;
//    private String mPassword;
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_sign_up);
//
//        emailField = (EditText) findViewById(R.id.emailField);
//        usernameField = (EditText) findViewById(R.id.usernameField);
//        passwordField = (EditText) findViewById(R.id.passwordField);
//        passwordField.setTransformationMethod(new PasswordTransformationMethod());
//
//        Button signUpButton = (Button) findViewById(R.id.signUpButton);
//        signUpButton.setOnClickListener(
//                new OnClickListener() {
//                    @Override
//                    public void onClick(View v) {
//                        mEmail = emailField.getText().toString().trim();
//                        mUsername = usernameField.getText().toString().trim();
//                        mPassword = passwordField.getText().toString().trim();
//
//                        if (anyBlankField()) return;
//
//                        ParseUser user = new ParseUser();
//                        user.setUsername(mUsername);
//                        user.setPassword(mPassword);
//                        user.setEmail(mEmail);
//
//                        user.signUpInBackground(
//                                new SignUpCallback() {
//                                    public void done(ParseException error) {
//                                        if (error == null) {
//                                            // Hooray! Now user can go to Main Activity.
//                                            Intent intent = new Intent(SignUpActivity.this, MainActivity.class);
//                                            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//                                            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
//                                            startActivity(intent);
//                                        } else {
//                                            // Sign up didn't succeed
//                                            String rawMessage = error.getMessage();
//                                            String mMessage = rawMessage.substring(0,1).toUpperCase() + rawMessage.substring(1);
//                                            makeToast(mMessage);
//                                        }
//                                    }
//                                }
//
//                        );
//                    }
//
//                    private boolean anyBlankField() {
//                        if (mEmail.equals("")) {
//                            makeToast(getString(R.string.email_missing));
//                            return true;
//                        }
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
//    }
//
//    private Void makeToast(String message) {
//        Toast.makeText(SignUpActivity.this, message, Toast.LENGTH_SHORT).show();
//        return null;
//    }
}
