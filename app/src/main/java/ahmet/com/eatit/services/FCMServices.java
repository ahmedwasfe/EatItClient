package ahmet.com.eatit.services;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.CustomTarget;
import com.bumptech.glide.request.target.CustomViewTarget;
import com.bumptech.glide.request.transition.Transition;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;

import java.util.Map;
import java.util.Random;

import ahmet.com.eatit.MainActivity;
import ahmet.com.eatit.R;
import ahmet.com.eatit.common.Common;

public class FCMServices extends FirebaseMessagingService {

    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {

        Map<String, String> dataRecv = remoteMessage.getData();
        if (dataRecv != null) {

            if (dataRecv.get(Common.KEY_IS_SEND_IMAGE) != null &&
                    dataRecv.get(Common.KEY_IS_SEND_IMAGE).equals("true")) {

                Glide.with(this)
                        .asBitmap()
                        .load(dataRecv.get(Common.KEY_NOTFI_IMAGE_LINK))
                        .into(new CustomTarget<Bitmap>() {

                            @Override
                            public void onResourceReady(@NonNull Bitmap resource, @Nullable Transition<? super Bitmap> transition) {

                                Common.showNotificationBigStyle(FCMServices.this, new Random().nextInt(),
                                        dataRecv.get(Common.KEY_NOTFI_TITLE),
                                        dataRecv.get(Common.KEY_NOTFI_CONTENT),
                                        resource,
                                        null);
                            }

                            @Override
                            public void onLoadCleared(@Nullable Drawable placeholder) {

                            }
                        });
            } else if (dataRecv.get(Common.KEY_NOTFI_TITLE).equals(getString(R.string.order_update))) {

                // Here we need call with MainActivity because we must assign value for Common.currentUser
                // So we must call with MainActivity to do that, if you direct call HomeActivity you will be crash
                // Because Common.currentUser only assign in MainActivity AFTER LOGIN
                Intent intent = new Intent(this, MainActivity.class);
                // Use extra to detect is app open from notification
                intent.putExtra(Common.IS_OPRN_ACTIVITY_NEW_ORDER, true);
                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.KEY_NOTFI_TITLE),
                        dataRecv.get(Common.KEY_NOTFI_CONTENT),
                        intent);

            } else {

                Common.showNotification(this, new Random().nextInt(),
                        dataRecv.get(Common.KEY_NOTFI_TITLE),
                        dataRecv.get(Common.KEY_NOTFI_CONTENT),
                        null);
            }
        }
    }

    @Override
    public void onNewToken(@NonNull String token) {
        super.onNewToken(token);
        Common.updateToken(this, token);
    }
}
