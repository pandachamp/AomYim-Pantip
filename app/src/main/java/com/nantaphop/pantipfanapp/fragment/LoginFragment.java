package com.nantaphop.pantipfanapp.fragment;

import android.app.ProgressDialog;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.loopj.android.http.AsyncHttpResponseHandler;
import com.nantaphop.pantipfanapp.R;
import com.nantaphop.pantipfanapp.event.UpdateLoginStateEvent;
import com.nantaphop.pantipfanapp.pref.UserPref_;
import com.nantaphop.pantipfanapp.utils.CircleTransform;
import com.nantaphop.pantipfanapp.utils.RESTUtils;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.assist.ImageScaleType;
import com.nostra13.universalimageloader.core.display.RoundedBitmapDisplayer;
import com.squareup.otto.Subscribe;
import com.squareup.picasso.Picasso;

import org.androidannotations.annotations.AfterViews;
import org.androidannotations.annotations.Click;
import org.androidannotations.annotations.EFragment;
import org.androidannotations.annotations.OptionsMenu;
import org.androidannotations.annotations.ViewById;
import org.androidannotations.annotations.sharedpreferences.Pref;
import org.apache.http.Header;

/**
 * Created by nantaphop on 07-Sep-14.
 */
@OptionsMenu(R.menu.menu_login)
@EFragment(R.layout.fragment_login)
public class LoginFragment extends BaseFragment {

    public static final String TAG = "loginFrag";
    @Pref
    UserPref_ userPref;
    @ViewById
    EditText username;
    @ViewById
    EditText password;
    @ViewById
    Button login;
    @ViewById
    LinearLayout loginPane;
    @ViewById
    ImageView avatar;
    @ViewById
    TextView usernameTxt;
    @ViewById
    Button logout;
    @ViewById
    LinearLayout userPane;

    private static DisplayImageOptions displayImageOptions =  new DisplayImageOptions.Builder()
            .resetViewBeforeLoading(true)
            .displayer(new RoundedBitmapDisplayer((int) 180f))
            .cacheInMemory(true)
            .cacheOnDisk(true)
            .imageScaleType(ImageScaleType.EXACTLY_STRETCHED)
            .showImageOnLoading(R.drawable.ic_image)
            .build();


    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.menu_login, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @AfterViews
    void init(){
        getAttachedActivity().getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        app.getEventBus().register(this);
    }

    @AfterViews
    void updateScreen() {
        if(userPref.username().exists()){
            loginPane.setVisibility(View.GONE);
            userPane.setVisibility(View.VISIBLE);
            usernameTxt.setText(userPref.username().get());
            Picasso.with(getAttachedActivity()).load(userPref.avatar().get()).transform(new CircleTransform()).into(avatar);
        }else{
            loginPane.setVisibility(View.VISIBLE);
            userPane.setVisibility(View.GONE);
        }
    }

    @Subscribe
    public void listenUpdateUser(UpdateLoginStateEvent e){
        updateScreen();
    }



    @Click
    void login(){
        final ProgressDialog progressDialog = ProgressDialog.show(getActivity(), app.getString(R.string.feedback_loading_login), app.getString(R.string.please_wait), false, false);
        client.login(username.getText().toString().trim(),
                password.getText().toString().trim(),
                new AsyncHttpResponseHandler() {
                    @Override
                    public void onSuccess(int i, Header[] headers, byte[] bytes) {
                        if(RESTUtils.isLogin(headers)){
                            if(RESTUtils.parseUserInfo(bytes, userPref)){
                                usernameTxt.setText(userPref.username().get());
                                Picasso.with(getAttachedActivity()).load(userPref.avatar().get()).transform(new CircleTransform()).into(avatar);
                                updateScreen();
                                app.getEventBus().post(new UpdateLoginStateEvent());
                                progressDialog.dismiss();
                            }
                        }else{
                            toastAlert(app.getString(R.string.feedback_login_failed));
                            progressDialog.dismiss();
                        }

                    }

                    @Override
                    public void onFailure(int i, Header[] headers, byte[] bytes, Throwable throwable) {
                        toastAlert(app.getString(R.string.feedback_connection_failed));
                        progressDialog.dismiss();
                    }
                });
    }

    @Click
    void logout(){
        client.logout();
        updateScreen();
        app.getEventBus().post(new UpdateLoginStateEvent());

    }
}
