/*
 * Copyright 2015 Ayuget
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ayuget.redface.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.ayuget.redface.R;
import com.ayuget.redface.account.RedfaceAccountManager;
import com.ayuget.redface.account.UserManager;
import com.ayuget.redface.data.api.hfr.HFRAuthenticator;
import com.ayuget.redface.data.api.model.PrivateMessage;
import com.ayuget.redface.data.api.model.User;
import com.ayuget.redface.data.rx.EndlessObserver;
import com.ayuget.redface.data.rx.SubscriptionHandler;
import com.ayuget.redface.ui.UIConstants;
import com.ayuget.redface.ui.misc.PagePosition;
import com.ayuget.redface.ui.misc.SnackbarHelper;
import com.rengwuxian.materialedittext.MaterialEditText;

import javax.inject.Inject;

import butterknife.InjectView;
import butterknife.OnClick;

public class AccountActivity extends BaseActivity {
    private static final String LOG_TAG = AccountActivity.class.getSimpleName();

    @InjectView(R.id.username)
    MaterialEditText usernameTextView;

    @InjectView(R.id.password)
    MaterialEditText passwordTextView;

    @InjectView(R.id.relogin_instructions)
    TextView reloginInstructions;

    @Inject
    HFRAuthenticator authenticator;

    @Inject
    RedfaceAccountManager accountManager;

    @Inject
    UserManager userManager;

    private SubscriptionHandler<User, Boolean> loginSubscriptionHandler = new SubscriptionHandler<>();

    private boolean isReloginMode;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_account);

        if (getIntent() != null) {
            Log.d(LOG_TAG, "Got some intent data");
            isReloginMode = getIntent().getBooleanExtra(UIConstants.ARG_RELOGIN_MODE, false);
        }

        if (isReloginMode) {
            Log.d(LOG_TAG, "Relogin mode");
            reloginInstructions.setVisibility(View.VISIBLE);
        }
    }

   @OnClick(R.id.sign_in_button)
   protected void onLoginAttempt() {
       final String username = usernameTextView.getText().toString().trim();

       if (username.equals("")) {
           usernameTextView.setError(getString(R.string.login_username_empty));
           return;
       }
       else {
           usernameTextView.setError(null);
       }

       final String password = passwordTextView.getText().toString().trim();
       if (password.equals("")) {
           passwordTextView.setError(getString(R.string.login_password_empty));
           return;
       }

       Log.d(LOG_TAG, String.format("Login attempt for user '%s'", username));

       final User user = new User(username, password);

       subscribe(loginSubscriptionHandler.load(user, authenticator.login(user), new EndlessObserver<Boolean>() {
           @Override
           public void onNext(Boolean loginWorked) {

               if (loginWorked) {
                   Log.d(LOG_TAG, "Login is successful !!");

                   // If we are logging in again (change of credentials for example), do
                   // not add a new account
                   if (! accountManager.hasAccount(user)) {
                       accountManager.addAccount(user);
                   }

                   userManager.setActiveUser(user);
                   Intent intent = new Intent(AccountActivity.this, TopicsActivity.class);
                   intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                   intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                   startActivity(intent);
                   finish();
               }
               else {
                   Log.d(LOG_TAG, "Error while logging in");
                   SnackbarHelper.make(AccountActivity.this, R.string.login_failed).show();
               }
           }

           @Override
           public void onError(Throwable throwable) {
               Log.d(LOG_TAG, "Unknow error while logging in :(", throwable);
           }
       }));
   }
}
