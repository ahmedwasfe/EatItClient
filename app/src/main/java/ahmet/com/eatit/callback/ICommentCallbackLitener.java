package ahmet.com.eatit.callback;

import java.util.List;

import ahmet.com.eatit.model.Comment;

public interface ICommentCallbackLitener {

    void onLoadCommentSuccess(List<Comment> mListComment);
    void onLoadCommentFaield(String error);
}
