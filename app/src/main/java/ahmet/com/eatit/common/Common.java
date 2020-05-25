package ahmet.com.eatit.common;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.text.style.StyleSpan;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.google.android.gms.maps.model.LatLng;
import com.google.firebase.database.FirebaseDatabase;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import ahmet.com.eatit.model.Category;
import ahmet.com.eatit.model.Food.Addon;
import ahmet.com.eatit.model.Food.Food;
import ahmet.com.eatit.model.Food.FoodSize;
import ahmet.com.eatit.model.ShippingOrder;
import ahmet.com.eatit.model.Token;
import ahmet.com.eatit.model.User;
import ahmet.com.eatit.R;
import ahmet.com.eatit.services.FCMServices;

public class Common {

    public static final String KEY_LOOGED = "USER_LOGGED";

    public static final int CODE_REQUEST_PHONE = 1880;
    public static final int CODE_REQUEST_PAYMENT_BRAINTREE = 1881;


    // Firebase referance
    public static final String KEY_USER_REFERANCE = "Users";
    public static final String KEY_POPULAR_REFERANCE = "MostPopular";
    public static final String KEY_BEST_DEALS_REFERANCE = "BestDeals";
    public static final String KEY_CAEGORIES_REFERANCE = "Category";
    public static final String KEY_COMMENT_REFERANCE = "Comments";
    public static final String KEY_ORDER_REFERANCE = "Orders";
    public static final String KEY_SHIPPING_ORDER_REFERANCE = "ShippingOrders";
    public static final String KEY_TOKEN_REFERANCE = "Tokens";
    public static final String KEY_FOOD_CHILD = "foods";

    // Notification
    public static final String KEY_NOTFI_TITLE = "title";
    public static final String KEY_NOTFI_CONTENT = "content";
    public static final String IS_OPRN_ACTIVITY_NEW_ORDER = "IsOpenActivityNewOrder";


    public static final int DEFAULT_COLUMN_COUNT = 0;
    public static final int FULL_WIDTH_COLUMN = 1;
    public static final String KEY_REFUND_REQUEST_REFERANCE = "RefundRequest";
    public static final String KEY_SUBSCRIBE_NEWS = "Subscribe_News";
    public static final String KEY_NEWS_TOPIC = "news";
    public static final String KEY_IS_SEND_IMAGE = "IS_SEND_IMAGE";
    public static final String KEY_NOTFI_IMAGE_LINK = "Image_Link";


    public static User currentUser;
    public static Category currentCategory;
    public static Food currentFood;
    public static ShippingOrder currentShippingOrder;

    public static String currentToken;
    public static String authorizeKey = "";

    public static void setFragment(Fragment fragment, int frameId, FragmentManager fragmentManager){
        fragmentManager.beginTransaction()
                .replace(frameId, fragment)
                .setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out)
                .commit();
    }

    public static void setSpanString(String welcome, String name, TextView textView){

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(name);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, name.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);

    }

    public static void setSpanStringColor(String welcome, String text, TextView textView, int color) {

        SpannableStringBuilder builder = new SpannableStringBuilder();
        builder.append(welcome);
        SpannableString spannableString = new SpannableString(text);
        StyleSpan boldSpan = new StyleSpan(Typeface.BOLD);
        spannableString.setSpan(boldSpan, 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        spannableString.setSpan(new ForegroundColorSpan(color), 0, text.length(), Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        builder.append(spannableString);
        textView.setText(builder, TextView.BufferType.SPANNABLE);
    }

    public static String formatFoodPrice(double price) {
        if (price != 0){
            DecimalFormat dFormat = new DecimalFormat("#,##0.00");
            dFormat.setRoundingMode(RoundingMode.UP);
            String finalPrice = new StringBuilder(dFormat.format(price)).toString();
            return finalPrice.replace(".",",");
        }else
            return "0,00";
    }

    public static Double calcuateExtraPrice(FoodSize foodSize, List<Addon> listAddonUserSelected) {

        Double result = 0.0;
        if (foodSize == null && listAddonUserSelected == null)
            return 0.0;
        else if (foodSize == null){
            // If listAddon != null, need sum price
            for (Addon addon : listAddonUserSelected)
                result += addon.getPrice();
            return result;
        }else if (listAddonUserSelected == null)
            return foodSize.getPrice() * 1.0;
        else{
            // If both size and addon is select
            result = foodSize.getPrice() * 1.0;
            for (Addon addon : listAddonUserSelected)
                result += addon.getPrice();
            return result;
        }
    }

    public static String createOrderKey() {
        return new StringBuilder()
                // Get current time in millisecound
                .append(System.currentTimeMillis())
                // Add random number to block same order at same time
                .append(Math.abs(new Random().nextInt()))
                .toString();
    }

    public static String getDateOfWeek(int i) {
        switch (i){
            case 1:
                return "Sunday";
            case 2:
                return "Monday";
            case 3:
                return "Tuesday";
            case 4:
                return "Wednesday";
            case 5:
                return "Thursday";
            case 6:
                return "Friday";
            case 7:
                return "Saturday";
            default:
                return "Unknown";

        }
    }

    public static String convertStatus(int orderStatus) {
        switch (orderStatus){
            case 0:
                return "Placed";
            case 1:
                return "Shipping";
            case 2:
                return "Shipped";
            case -1:
                return "Cancelled";
            default:
                return "Unknown";
        }
    }

    public static void showNotification(Context mContext, int id, String title, String content, Intent intent) {

        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "ocean_for_it_eat_it";
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Eat It", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Eat It");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_restaurant_menu));
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);
    }

    public static void updateToken(Context mContext, String newToken) {

        if (Common.currentUser != null){
            FirebaseDatabase.getInstance().getReference()
                    .child(Common.KEY_TOKEN_REFERANCE)
                    .child(Common.currentUser.getUid())
                    .setValue(new Token(Common.currentUser.getPhone(), newToken))
                    .addOnCompleteListener(task -> {

                    }).addOnFailureListener(e -> {
                Log.e("GET_TOKEN_ERROR", e.getMessage());
                Toast.makeText(mContext, ""+e.getMessage(), Toast.LENGTH_SHORT).show();
            });
        }
    }

    public static String createTopicOrder() {
        return new StringBuilder("/topics/new_order").toString();
    }

    public static String buildToken(String authorizeKey) {
        return new StringBuilder("Bearer").append(" ").append(authorizeKey).toString();
    }

    public static float getBearing(LatLng start, LatLng end) {

        double lat = Math.abs(start.latitude - end.latitude);
        double lng = Math.abs(start.longitude - end.longitude);

        if (start.latitude < end.latitude && start.longitude < end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat)));
        else if (start.latitude >= end.latitude && start.longitude < end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat)))+90);
        else if (start.latitude >= end.latitude && start.longitude >= end.longitude)
            return (float) (Math.toDegrees(Math.atan(lng/lat))+180);
        else if (start.latitude < end.latitude && start.longitude >= end.longitude)
            return (float) ((90 - Math.toDegrees(Math.atan(lng/lat)))+270);

        return -1;
    }

    public static List<LatLng> decodePoly(String encoded) {

        List poly = new ArrayList();
        int index = 0, len = encoded.length();
        int lat = 0, lng = 0;

        while (index < len){
            int b, shift = 0, result = 0;
            do {
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while (b >= 0x20);
            int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lat +=dlat;

            shift = 0;
            result = 0;
            do {
                b = encoded.charAt(index++)-63;
                result |= (b & 0x1f) << shift;
                shift+=5;
            }while (b >= 0x20);
            int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
            lng +=dlng;

            LatLng p = new LatLng((((double)lat / 1E5)),
                    (((double)lng / 1E5)));
            poly.add(p);
        }

        return poly;

    }

    public static String getListAddon(List<Addon> mLIstAddon){

        StringBuilder stringBuilder = new StringBuilder();
        for (Addon addon : mLIstAddon)
            stringBuilder.append(addon.getName()).append(",");

        return stringBuilder.substring(0,stringBuilder.length()-1);
    }

    public static Food findFoodListById(Category category, String foodId) {

        if (category.getFoods() != null && category.getFoods().size() > 0){
            for (Food food : category.getFoods())
                if (food.getId().equals(foodId))
                    return food;
                return null;
        }else
            return null;
    }

    public static void showNotificationBigStyle(Context mContext, int id, String title, String content, Bitmap bitmap, Intent intent) {


        PendingIntent pendingIntent = null;
        if (intent != null)
            pendingIntent = PendingIntent.getActivity(mContext, id, intent, PendingIntent.FLAG_CANCEL_CURRENT);
        String NOTIFICATION_CHANNEL_ID = "ocean_for_it_eat_it";
        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService(mContext.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            NotificationChannel notificationChannel = new NotificationChannel(NOTIFICATION_CHANNEL_ID,
                    "Eat It", NotificationManager.IMPORTANCE_DEFAULT);
            notificationChannel.setDescription("Eat It");
            notificationChannel.enableLights(true);
            notificationChannel.setLightColor(Color.BLUE);
            notificationChannel.setVibrationPattern(new long[]{0,1000,500,1000});
            notificationChannel.enableVibration(true);

            notificationManager.createNotificationChannel(notificationChannel);
        }

        NotificationCompat.Builder builder = new NotificationCompat.Builder(mContext, NOTIFICATION_CHANNEL_ID);
        builder.setContentTitle(title)
                .setContentText(content)
                .setAutoCancel(true)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setStyle(new NotificationCompat.BigTextStyle())
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setLargeIcon(bitmap)
                .setStyle(new NotificationCompat.BigPictureStyle().bigPicture(bitmap))
                .setLargeIcon(BitmapFactory.decodeResource(mContext.getResources(), R.drawable.ic_restaurant_menu));
        if (pendingIntent != null)
            builder.setContentIntent(pendingIntent);
        Notification notification = builder.build();
        notificationManager.notify(id, notification);

    }

}
