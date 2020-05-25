package ahmet.com.eatit.ui.comment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.google.android.material.bottomsheet.BottomSheetDialogFragment;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

import ahmet.com.eatit.adapter.CommentAdapter;
import ahmet.com.eatit.callback.ICommentCallbackLitener;
import ahmet.com.eatit.common.Common;
import ahmet.com.eatit.model.Comment;
import ahmet.com.eatit.R;
import butterknife.BindView;
import butterknife.ButterKnife;
import io.supercharge.shimmerlayout.ShimmerLayout;

public class CommentBottomSheetFragment extends BottomSheetDialogFragment implements ICommentCallbackLitener {

    private CommentViewModel mCommentViewModel;

    @BindView(R.id.recycler_comment)
    RecyclerView mRecyclerComment;
    @BindView(R.id.shimmer_layout_comment)
    ShimmerLayout mShimmerLayout;

    private ICommentCallbackLitener commentCallbackLitener;

    private static CommentBottomSheetFragment instance;


    public static CommentBottomSheetFragment getInstance() {
        return instance == null ? new CommentBottomSheetFragment() : instance;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View sheetView = inflater.inflate(R.layout.fragment_sheet_comment, container, false);

        ButterKnife.bind(this, sheetView);

        init();

        mShimmerLayout.startShimmerAnimation();
        loadComments();

        mCommentViewModel.getmMutableComment()
                .observe(this, comments -> {
                    CommentAdapter commentAdapter = new CommentAdapter(getActivity(), comments);
                    mRecyclerComment.setAdapter(commentAdapter);
                });

        return sheetView;
    }

    private void loadComments() {

        FirebaseDatabase.getInstance().getReference()
                .child(Common.KEY_COMMENT_REFERANCE)
                .child(Common.currentFood.getId())
                .orderByChild("serverTimeStamp")
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                        List<Comment> mListComment = new ArrayList<>();
                        for (DataSnapshot snapshot : dataSnapshot.getChildren()){
                            Comment comment = snapshot.getValue(Comment.class);
                            mListComment.add(comment);
                        }

                        commentCallbackLitener.onLoadCommentSuccess(mListComment);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {
                        commentCallbackLitener.onLoadCommentFaield(databaseError.getMessage());
                    }
                });
    }

    private void init() {

        commentCallbackLitener = this;

        mCommentViewModel = ViewModelProviders.of(this).get(CommentViewModel.class);

        mRecyclerComment.setHasFixedSize(true);
        mRecyclerComment.setLayoutManager(new StaggeredGridLayoutManager(1, LinearLayout.VERTICAL));


    }

    @Override
    public void onLoadCommentSuccess(List<Comment> mListComment) {

        mCommentViewModel.setComment(mListComment);
        mShimmerLayout.stopShimmerAnimation();
        mShimmerLayout.setVisibility(View.GONE);
    }

    @Override
    public void onLoadCommentFaield(String error) {
        Log.e("COMMENT_ERROR", error);
        mShimmerLayout.stopShimmerAnimation();
        mShimmerLayout.setVisibility(View.GONE);
    }
}
