package ahmet.com.eatit.ui.comment;

import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import java.util.List;

import ahmet.com.eatit.model.Comment;

public class CommentViewModel extends ViewModel {

    private MutableLiveData<List<Comment>> mMutableComment;

    public CommentViewModel() {
        mMutableComment = new MutableLiveData<>();
    }

    public MutableLiveData<List<Comment>> getmMutableComment() {
        return mMutableComment;
    }

    public void setComment(List<Comment> mListComment) {
        mMutableComment.setValue(mListComment);
    }
}
