package android.support.design.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.lsjwzh.widget.multirvcontainer.MultiRVScrollView;
import com.lsjwzh.widget.pulltorefresh.R;

import java.util.ArrayList;
import java.util.List;

import static android.view.MotionEvent.ACTION_CANCEL;
import static android.view.MotionEvent.ACTION_UP;

public class PullToRefreshHostScrollView extends MultiRVScrollView {
  private static final String TAG = PullToRefreshHostScrollView.class.getSimpleName();
  private int mTouchSlop;
  private int mLastEventAction;
  private List<RefreshListener> mRefreshListeners = new ArrayList<>();
  private boolean mMoveBeforeTouchRelease;
  private boolean mAddViewHacking;

  public PullToRefreshHostScrollView(Context context) {
    super(context);
    init();
  }

  public PullToRefreshHostScrollView(Context context, AttributeSet attrs) {
    super(context, attrs);
    init();
  }

  public PullToRefreshHostScrollView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    init();
  }

  private void init() {
    final ViewConfiguration configuration = ViewConfiguration.get(getContext());
    mTouchSlop = configuration.getScaledTouchSlop();
  }

  @Override
  protected LayoutParams generateDefaultLayoutParams() {
    return new LayoutParams(FrameLayout.LayoutParams.MATCH_PARENT,
        FrameLayout.LayoutParams.MATCH_PARENT);
  }

  @Override
  protected LayoutParams generateLayoutParams(ViewGroup.LayoutParams lp) {
    if (sPreserveMarginParamsInLayoutParamConversion) {
      if (lp instanceof FrameLayout.LayoutParams) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
          return new LayoutParams((FrameLayout.LayoutParams) lp);
        }
      } else if (lp instanceof MarginLayoutParams) {
        return new LayoutParams((MarginLayoutParams) lp);
      }
    }
    return new LayoutParams(lp);
  }

  @Override
  public LayoutParams generateLayoutParams(AttributeSet attrs) {
    return new LayoutParams(getContext(), attrs);
  }

  @Override
  public void scrollBy(int x, int y) {
    super.scrollBy(x, y);

  }

  @Override
  protected void onScrollChanged(int l, int t, int oldl, int oldt) {
    super.onScrollChanged(l, t, oldl, oldt);
    int childCount = getChildCount();
    if (childCount > 1) {
      for (int i = 1; i < childCount; i++) {
        View child = getChildAt(i);
        LayoutParams layoutParams = (LayoutParams) child.getLayoutParams();
        if (layoutParams.actionType == LayoutParams.ACTION_TYPE_STICKY) {
          if (layoutParams.stickyCopyView != View.NO_ID) {
            View stickyCopyView = findViewById(layoutParams.stickyCopyView);
            float copyViewRealY = 0;
            View parent = stickyCopyView;
            while (parent != this) {
              copyViewRealY += parent.getY();
              parent = (View) parent.getParent();
            }
            if (copyViewRealY >= child.getY()) {
              child.setVisibility(GONE);
              stickyCopyView.setVisibility(VISIBLE);
            } else {
              child.setVisibility(VISIBLE);
              stickyCopyView.setVisibility(INVISIBLE);
            }
          }
          child.setTranslationY(getScrollY());
        }
      }
    }
  }

  @Override
  public int getChildCount() {
    return mAddViewHacking ? 0 : super.getChildCount();
  }

  @Override
  public void addView(View child) {
    mAddViewHacking = true;
    super.addView(child);
    mAddViewHacking = false;
  }

  @Override
  public void addView(View child, int index) {
    mAddViewHacking = true;
    super.addView(child, index);
    mAddViewHacking = false;
  }

  @Override
  public void addView(View child, ViewGroup.LayoutParams params) {
    mAddViewHacking = true;
    super.addView(child, params);
    mAddViewHacking = false;
  }

  @Override
  public void addView(View child, int index, ViewGroup.LayoutParams params) {
    mAddViewHacking = true;
    super.addView(child, index, params);
    mAddViewHacking = false;
  }

  public void endRefresh() {
    getRefreshGroup().getRefreshHeader().collapse(getRefreshGroup().getRefreshTargetView(),
        new Runnable() {

          @Override
          public void run() {
            for (RefreshListener listener : mRefreshListeners) {
              listener.onRefreshAnimationEnd();
            }
          }
        });
  }

  public void startRefresh() {
    getRefreshGroup().getRefreshHeader().expand(getRefreshGroup().getRefreshTargetView(),
        new Runnable() {

          @Override
          public void run() {
            for (RefreshListener listener : mRefreshListeners) {
              listener.onRefreshing();
            }
          }
        });
  }


  @Override
  public void stopNestedScroll() {
    super.stopNestedScroll();
    Log.d(TAG, "stopNestedScroll:");
    if (mLastEventAction == ACTION_UP) {
      if (isRefreshHeaderExpanded()) {
        for (RefreshListener listener : mRefreshListeners) {
          listener.onRefreshing();
        }
      } else if (getRefreshGroup().getRefreshTargetView().getTranslationY() > 0) {
        getRefreshGroup().getRefreshHeader().collapse(getRefreshGroup().getRefreshTargetView(),
            null);
      }
    }
  }

  @Override
  public boolean dispatchTouchEvent(MotionEvent ev) {
    mLastEventAction = ev.getAction();
    if (ev.getAction() == ACTION_UP || ev.getAction() == ACTION_CANCEL) {
      mMoveBeforeTouchRelease = false;
    }
    Log.d(TAG, "dispatchTouchEvent:" + mLastEventAction);
    return super.dispatchTouchEvent(ev);
  }

  @Override
  public void onNestedPreScroll(View target, int dx, int dy, int[] consumed) {
    super.onNestedPreScroll(target, dx, dy, consumed);
    Log.d(TAG, "dy:" + dy + " consumed:" + consumed[1]);
    if (dy < -mTouchSlop && getScrollY() == 0 && consumed[1] == 0) {
      consumed[1] = mTouchSlop + 1 + dy;
    }
  }

  @Override
  public void onNestedScroll(View target, int dxConsumed, int dyConsumed, int dxUnconsumed, int
      dyUnconsumed) {
    Log.d(TAG, "dyConsumed:" + dyConsumed + " dyUnconsumed:" + dyUnconsumed);
    if (dyUnconsumed != 0 && (getScrollY() == 0 || mMoveBeforeTouchRelease)) {
      PullToRefreshGroup refreshChild = getRefreshGroup();
      if (refreshChild != null) {
        float translationY = refreshChild.getRefreshTargetView().getTranslationY();
        float mayTranslationY = translationY - dyUnconsumed;
        if ((mayTranslationY > 0 && translationY < refreshChild.getRefreshHeader().getMaxHeight())
            || mMoveBeforeTouchRelease) {
          mMoveBeforeTouchRelease = true;
          translationY = Math.min(mayTranslationY, refreshChild.getRefreshHeader()
              .getMaxHeight());
          translationY = Math.max(0, translationY);
          Log.d(TAG, "translationY:" + translationY);
          refreshChild.getRefreshHeader().cancelAnimation();
          refreshChild.getRefreshHeader().setVisibleHeight((int) translationY);
          refreshChild.getRefreshTargetView().setTranslationY(translationY);
          return;
        }
      }
    }
    super.onNestedScroll(target, dxConsumed, dyConsumed, dxUnconsumed, dyUnconsumed);
  }

  private boolean isRefreshHeaderExpanded() {
    PullToRefreshGroup refreshChild = getRefreshGroup();
    float translationY = refreshChild.getRefreshTargetView().getTranslationY();
    return translationY >= refreshChild.getRefreshHeader().getMaxHeight();
  }

  @Override
  protected void dispatchDraw(Canvas canvas) {
    super.dispatchDraw(canvas);
  }

  @Override
  protected void onDraw(Canvas canvas) {
    super.onDraw(canvas);
  }

  @Override
  protected void onOverScrolled(int scrollX, int scrollY, boolean clampedX, boolean clampedY) {
    Log.d(TAG, "onOverScrolled scrollY:" + scrollY + " clampedY:" + clampedY);
    super.onOverScrolled(scrollX, scrollY, clampedX, clampedY);
    if (clampedY) {
      if (getScrollY() == 0) {
        PullToRefreshGroup refreshGroup = getRefreshGroup();
        if (refreshGroup != null) {
          float translationY = refreshGroup.getRefreshTargetView().getTranslationY();
          if (translationY < refreshGroup.getRefreshHeader().getMaxHeight()) {
            translationY = Math.min(translationY + mTouchSlop,
                refreshGroup.getRefreshHeader().getMaxHeight());
            Log.d(TAG, "translationY:" + translationY);
            refreshGroup.getRefreshHeader().cancelAnimation();
            refreshGroup.getRefreshHeader().setVisibleHeight((int) translationY);
            refreshGroup.getRefreshTargetView().setTranslationY(translationY);
          }
        }
      }
    }
  }

  @Override
  public boolean startNestedScroll(int axes) {
    boolean b = super.startNestedScroll(axes);
    Log.d(TAG, "startNestedScroll:" + b);
    return b;
  }

  @Override
  public void onStopNestedScroll(View target) {
    Log.d(TAG, "onStopNestedScroll");
    super.onStopNestedScroll(target);
  }

  PullToRefreshGroup getRefreshGroup() {
    return (PullToRefreshGroup) getChildAt(0);
  }

  public interface RefreshListener {
    void onRefreshing();

    void onRefreshAnimationEnd();
  }

  public interface IRefreshHeader {

    void expand(View refreshTargetView, Runnable animationEndCallback);

    void collapse(View refreshTargetView, Runnable animationEndCallback);

    int getMaxHeight();

    void cancelAnimation();

    void setVisibleHeight(int targetHeight);
  }

  public static class LayoutParams extends FrameLayout.LayoutParams {
    public static final int ACTION_TYPE_NONE = 0;
    public static final int ACTION_TYPE_STICKY = 1;
    public int actionType = ACTION_TYPE_NONE;
    public int stickyCopyView = View.NO_ID;


    public LayoutParams(int width, int height) {
      super(width, height);
    }

    public LayoutParams(int width, int height, int gravity) {
      super(width, height, gravity);
    }

    public LayoutParams(@NonNull Context c, @Nullable AttributeSet attrs) {
      super(c, attrs);
      final TypedArray a
          = c.obtainStyledAttributes(attrs, R.styleable.PullToRefreshHostScrollView_Layout);
      actionType
          = a.getInt(R.styleable.PullToRefreshHostScrollView_Layout_actionType, ACTION_TYPE_NONE);
      stickyCopyView
          = a.getResourceId(R.styleable.PullToRefreshHostScrollView_Layout_stickyCopyView, View.NO_ID);
      a.recycle();
    }

    public LayoutParams(@NonNull ViewGroup.LayoutParams source) {
      super(source);
    }

    public LayoutParams(@NonNull MarginLayoutParams source) {
      super(source);
    }

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public LayoutParams(@NonNull FrameLayout.LayoutParams source) {
      super(source);
    }
  }
}
