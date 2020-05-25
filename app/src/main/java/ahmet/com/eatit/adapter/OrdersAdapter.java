package ahmet.com.eatit.adapter;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import ahmet.com.eatit.CartDatabse.Cart;
import ahmet.com.eatit.callback.IRecyclerItemClickLitener;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.Order;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class OrdersAdapter extends RecyclerView.Adapter<OrdersAdapter.OrdersHolder> {

    private Context mContext;
    private List<Order> mListOrder;
    private LayoutInflater inflater;

    private Calendar calendar;
    private SimpleDateFormat dateFormat;

    public OrdersAdapter(Context mContext, List<Order> mListOrder) {
        this.mContext = mContext;
        this.mListOrder = mListOrder;

        inflater = LayoutInflater.from(mContext);

        calendar = Calendar.getInstance();
        dateFormat = new SimpleDateFormat("dd-MM-yyyy HH:mm:ss a");
    }

    @NonNull
    @Override
    public OrdersHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_order, parent, false);
        return new OrdersHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull OrdersHolder holder, int position) {

        // Load deafult image in cart
        Picasso.get()
                .load(mListOrder.get(position).getCarts().get(0).getFoodImage())
                .into(holder.mImgOrder);

        // load date
        calendar.setTimeInMillis(mListOrder.get(position).getDate());
        Date date = new Date(mListOrder.get(position).getDate());


        Common.setSpanStringColor("Order date: ", new StringBuilder(Common.getDateOfWeek(calendar.get(Calendar.DAY_OF_WEEK)))
                        .append(" ")
                        .append(dateFormat.format(date)).toString(),
                holder.mTxtOrderDate, Color.parseColor("#333639"));

        holder.mTxtOrderNumber.setText(new StringBuilder("Order No: ")
                                        .append(mListOrder.get(position).getOrderNumber()));

        Common.setSpanStringColor("Comment: ", mListOrder.get(position).getComment(),
                holder.mTxtOrderComment, Color.parseColor("#00574B"));

        Common.setSpanStringColor("Order status: ", Common.convertStatus(mListOrder.get(position).getOrderStatus()),
                holder.mTxtOrderStatus, Color.parseColor("#00579A"));

        holder.setRecyclerItemClickLitener((view, position1) -> {

            showOrderDetailsDialog(mListOrder.get(position1).getCarts());
        });


    }

    private void showOrderDetailsDialog(List<Cart> carts) {

        View layoutView = inflater.inflate(R.layout.dialog_order_details, null);

        AlertDialog.Builder builder = new AlertDialog.Builder(mContext)
                .setView(layoutView);

        Button btnOk = layoutView.findViewById(R.id.btn_dismiss_dialog);
        RecyclerView mRecyclerOrderetails = layoutView.findViewById(R.id.recycler_order_details);
        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext);
        mRecyclerOrderetails.setHasFixedSize(true);
        mRecyclerOrderetails.setLayoutManager(layoutManager);
        mRecyclerOrderetails.addItemDecoration(new DividerItemDecoration(mContext, layoutManager.getOrientation()));

        OrderDetailsAdapter orderDetailsAdapter = new OrderDetailsAdapter(mContext, carts);
        mRecyclerOrderetails.setAdapter(orderDetailsAdapter);

        AlertDialog dialog = builder.create();
        dialog.show();

        btnOk.setOnClickListener(view -> {
            dialog.dismiss();
        });
    }

    @Override
    public int getItemCount() {
        return mListOrder.size();
    }

    public Order getItemAtPosition(int position){
        return mListOrder.get(position);
    }

    public void setItemAtPosition(int position, Order order){
        mListOrder.set(position, order);
    }


    class OrdersHolder extends RecyclerView.ViewHolder implements View.OnClickListener {

        @BindView(R.id.img_order_food)
        ImageView mImgOrder;
        @BindView(R.id.txt_order_food_date)
        TextView mTxtOrderDate;
        @BindView(R.id.txt_order_food_number)
        TextView mTxtOrderNumber;
        @BindView(R.id.txt_order_food_comment)
        TextView mTxtOrderComment;
        @BindView(R.id.txt_order_food_status)
        TextView mTxtOrderStatus;

        private IRecyclerItemClickLitener recyclerItemClickLitener;

        public OrdersHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(this);
        }

        public void setRecyclerItemClickLitener(IRecyclerItemClickLitener recyclerItemClickLitener) {
            this.recyclerItemClickLitener = recyclerItemClickLitener;
        }

        @Override
        public void onClick(View v) {
            recyclerItemClickLitener.onItemClick(v, getAdapterPosition());
        }
    }
}