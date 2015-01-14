package com.meg7.soas.ui.fragment;

import android.accounts.AccountManager;
import android.accounts.AccountManagerCallback;
import android.accounts.AccountManagerFuture;
import android.os.Bundle;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.ViewFlipper;

import com.meg7.soas.R;
import com.meg7.soas.accounts.SoasAccountManager;
import com.meg7.soas.accounts.User;
import com.meg7.soas.ui.AccountAuthActivity;

/**
 * A simple Fragment which shows the Account Information if created
 * or else shows Add Account Button if no Account is created.
 *
 * Here ViewFlipper contains two views:
 * 1- View for where Account has been created and displays Account Information.
 * 2- View for where Account has not been created and displays a button to create an account.
 *
 * @author Santosh Dhakal
 */
public class AccountViewFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = AccountViewFragment.class.getSimpleName();

    ViewFlipper viewFlipper;
    TextView txtAccountInfo;
    boolean isUserLoggedIn = false;
    SoasAccountManager soasAccountManager;
    AccountManager accountManager;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_account_view, container, false);
        viewFlipper = (ViewFlipper) view.findViewById(R.id.view_flipper);

        txtAccountInfo = (TextView) view.findViewById(R.id.txt_accountinfo);
        view.findViewById(R.id.btn_addAccount).setOnClickListener(this);
        view.findViewById(R.id.btn_deleteAccount).setOnClickListener(this);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        accountManager = AccountManager.get(getActivity());
        soasAccountManager = new SoasAccountManager(accountManager);
        switchViews();
    }

    /**
     * If User is logged in or has created an Account, show Account Information View
     * else show button to add account.
     */
    private void switchViews() {
        isUserLoggedIn = AccountAuthActivity.isUserLoggedIn(getActivity());
        int displayChildIndex = viewFlipper.getDisplayedChild();
        if (isUserLoggedIn) {
            if (displayChildIndex == 1) {
                viewFlipper.showPrevious();
            }

            User user = soasAccountManager.getCurrentUser();
            if (user != null) {
                StringBuilder stringBuilder = new StringBuilder();
                stringBuilder.append("UserName : ")
                        .append(user.uname)
                        .append("\n")
                        .append("Email : ")
                        .append(user.email)
                        .append("\n")
                        .append("Name : ")
                        .append(user.name)
                        .append("\n")
                        .append("AuthToken : ")
                        .append(user.authToken);
                txtAccountInfo.setText(stringBuilder.toString());
            }
        } else if (displayChildIndex == 0) {
            viewFlipper.showNext();
        }
    }

    /**
     * This method will create an Account, it will:
     * 1: Call AccountAuthenticatorService
     * 2: AccountAuthenticatorService will call AccountAuthenticator
     * 3: AccountAuthenticator addAccount method is called and on AccountAuthenticator addAccount method
     * we have launched an AuthActivity which contains sign in or sign up form
     */
    private void addAccount() {
        Bundle params = new Bundle();
        params.putBoolean(AccountAuthActivity.CALL_FROM_ADD_ACCOUNT, true);
        accountManager.addAccount(SoasAccountManager.ACCOUNT_TYPE,
                SoasAccountManager.ACCOUNT_AUTH_TYPE, null, params, getActivity(),
                new AccountManagerCallback<Bundle>() {

                    @Override
                    public void run(AccountManagerFuture<Bundle> future) {
                        try {
                            Bundle bnd = future.getResult();
                            Log.i(TAG, "AddNewAccount Bundle is " + bnd);
                            switchViews();

                        } catch (Exception e) {
                            e.printStackTrace();
                            Log.e(TAG, "Error Creating Account");
                        }

                    }
                }, null);
    }

    /**
     * Method which will delete our
     * 1: Our added Account
     * 2: Delete all Accounts
     * 3: Changes Views
     */
    private void deleteAccount() {
        soasAccountManager.removeAllAccounts();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                switchViews();

            }
        }, 400);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_addAccount:
                addAccount();
                break;
            case R.id.btn_deleteAccount:
                deleteAccount();
                break;
        }
    }
}
