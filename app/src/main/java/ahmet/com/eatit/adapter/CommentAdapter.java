package ahmet.com.eatit.adapter;

import android.content.Context;
import android.text.format.DateUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import ahmet.com.eatit.model.Comment;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;


public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.CommentHolder> {

    private Context mContext;
    private List<Comment> mListComment;
    private LayoutInflater inflater;

    public CommentAdapter(Context mContext, List<Comment> mListComment) {
        this.mContext = mContext;
        this.mListComment = mListComment;

        inflater = LayoutInflater.from(mContext);
    }

    @NonNull
    @Override
    public CommentHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View layoutView = inflater.inflate(R.layout.raw_comment, parent, false);
        return new CommentHolder(layoutView);
    }

    @Override
    public void onBindViewHolder(@NonNull CommentHolder holder, int position) {

        Long timeStamp = Long.valueOf(mListComment.get(position).getServerTimeStamp()
                                    .get("timeStamp").toString());
        holder.mTxtCommentDate.setText(DateUtils.getRelativeTimeSpanString(timeStamp));
        holder.mTxtComment.setText(mListComment.get(position).getComment());
        holder.mTxtUserName.setText(mListComment.get(position).getName());

       // Picasso.get().load(R.drawable.ic_avater2).into(holder.mImgCommentUser);

        holder.mRatingBar.setRating(mListComment.get(position).getRateValue());



    }

    @Override
    public int getItemCount() {
        return mListComment.size();
    }

    class CommentHolder extends RecyclerView.ViewHolder {

        @BindView(R.id.img_comment_user_image)
        ImageView mImgCommentUser;
        @BindView(R.id.txt_comment_user_name)
        TextView mTxtUserName;
        @BindView(R.id.txt_comment_time_date)
        TextView mTxtCommentDate;
        @BindView(R.id.txt_comment)
        TextView mTxtComment;
        @BindView(R.id.rating_bar_comment)
        RatingBar mRatingBar;
        @BindView(R.id.img_more_options)
        ImageView mImgMoreOption;

        public CommentHolder(@NonNull View itemView) {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }
    }
}