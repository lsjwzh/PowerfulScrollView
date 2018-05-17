package android.support.v4.widget;

import android.widget.OverScroller;

public final class ScrollerWrapper {
  OverScroller mScroller;

  /**
   * Package protected constructor that allows to specify if API version is newer than ICS.
   * It is useful for unit testing.
   */
  public ScrollerWrapper(OverScroller scroller) {
    mScroller = scroller;
  }

  public ScrollerWrapper(ScrollerCompat scroller) {
    mScroller = scroller.mScroller;
  }

  /**
   * Returns whether the scroller has finished scrolling.
   *
   * @return True if the scroller has finished scrolling, false otherwise.
   */
  public boolean isFinished() {
    return mScroller.isFinished();
  }

  /**
   * Returns the current X offset in the scroll.
   *
   * @return The new X offset as an absolute distance from the origin.
   */
  public int getCurrX() {
    return mScroller.getCurrX();
  }

  /**
   * Returns the current Y offset in the scroll.
   *
   * @return The new Y offset as an absolute distance from the origin.
   */
  public int getCurrY() {
    return mScroller.getCurrY();
  }

  /**
   * @return The final X position for the scroll in progress, if known.
   */
  public int getFinalX() {
    return mScroller.getFinalX();
  }

  /**
   * @return The final Y position for the scroll in progress, if known.
   */
  public int getFinalY() {
    return mScroller.getFinalY();
  }

  /**
   * Returns the current velocity on platform versions that support it.
   * <p>
   * <p>The device must support at least API level 14 (Ice Cream Sandwich).
   * On older platform versions this method will return 0. This method should
   * only be used as input for nonessential visual effects such as {@link EdgeEffectCompat}.</p>
   *
   * @return The original velocity less the deceleration. Result may be
   * negative.
   */
  public float getCurrVelocity() {
    return mScroller.getCurrVelocity();
  }

  /**
   * Call this when you want to know the new location.  If it returns true,
   * the animation is not yet finished.  loc will be altered to provide the
   * new location.
   */
  public boolean computeScrollOffset() {
    return mScroller.computeScrollOffset();
  }

  /**
   * Start scrolling by providing a starting point and the distance to travel.
   * The scroll will use the default value of 250 milliseconds for the
   * duration.
   *
   * @param startX Starting horizontal scroll offset in pixels. Positive
   *               numbers will scroll the content to the left.
   * @param startY Starting vertical scroll offset in pixels. Positive numbers
   *               will scroll the content up.
   * @param dx     Horizontal distance to travel. Positive numbers will scroll the
   *               content to the left.
   * @param dy     Vertical distance to travel. Positive numbers will scroll the
   *               content up.
   */
  public void startScroll(int startX, int startY, int dx, int dy) {
    mScroller.startScroll(startX, startY, dx, dy);
  }

  /**
   * Start scrolling by providing a starting point and the distance to travel.
   *
   * @param startX   Starting horizontal scroll offset in pixels. Positive
   *                 numbers will scroll the content to the left.
   * @param startY   Starting vertical scroll offset in pixels. Positive numbers
   *                 will scroll the content up.
   * @param dx       Horizontal distance to travel. Positive numbers will scroll the
   *                 content to the left.
   * @param dy       Vertical distance to travel. Positive numbers will scroll the
   *                 content up.
   * @param duration Duration of the scroll in milliseconds.
   */
  public void startScroll(int startX, int startY, int dx, int dy, int duration) {
    mScroller.startScroll(startX, startY, dx, dy, duration);
  }

  /**
   * Start scrolling based on a fling gesture. The distance travelled will
   * depend on the initial velocity of the fling.
   *
   * @param startX    Starting point of the scroll (X)
   * @param startY    Starting point of the scroll (Y)
   * @param velocityX Initial velocity of the fling (X) measured in pixels per
   *                  second.
   * @param velocityY Initial velocity of the fling (Y) measured in pixels per
   *                  second
   * @param minX      Minimum X value. The scroller will not scroll past this
   *                  point.
   * @param maxX      Maximum X value. The scroller will not scroll past this
   *                  point.
   * @param minY      Minimum Y value. The scroller will not scroll past this
   *                  point.
   * @param maxY      Maximum Y value. The scroller will not scroll past this
   *                  point.
   */
  public void fling(int startX, int startY, int velocityX, int velocityY,
                    int minX, int maxX, int minY, int maxY) {
    mScroller.fling(startX, startY, velocityX, velocityY, minX, maxX, minY, maxY);
  }

  /**
   * Start scrolling based on a fling gesture. The distance travelled will
   * depend on the initial velocity of the fling.
   *
   * @param startX    Starting point of the scroll (X)
   * @param startY    Starting point of the scroll (Y)
   * @param velocityX Initial velocity of the fling (X) measured in pixels per
   *                  second.
   * @param velocityY Initial velocity of the fling (Y) measured in pixels per
   *                  second
   * @param minX      Minimum X value. The scroller will not scroll past this
   *                  point.
   * @param maxX      Maximum X value. The scroller will not scroll past this
   *                  point.
   * @param minY      Minimum Y value. The scroller will not scroll past this
   *                  point.
   * @param maxY      Maximum Y value. The scroller will not scroll past this
   *                  point.
   * @param overX     Overfling range. If > 0, horizontal overfling in either
   *                  direction will be possible.
   * @param overY     Overfling range. If > 0, vertical overfling in either
   *                  direction will be possible.
   */
  public void fling(int startX, int startY, int velocityX, int velocityY,
                    int minX, int maxX, int minY, int maxY, int overX, int overY) {
    mScroller.fling(startX, startY, velocityX, velocityY,
        minX, maxX, minY, maxY, overX, overY);
  }

  /**
   * Call this when you want to 'spring back' into a valid coordinate range.
   *
   * @param startX Starting X coordinate
   * @param startY Starting Y coordinate
   * @param minX   Minimum valid X value
   * @param maxX   Maximum valid X value
   * @param minY   Minimum valid Y value
   * @param maxY   Maximum valid Y value
   * @return true if a springback was initiated, false if startX and startY were
   * already within the valid range.
   */
  public boolean springBack(int startX, int startY, int minX, int maxX, int minY, int maxY) {
    return mScroller.springBack(startX, startY, minX, maxX, minY, maxY);
  }

  /**
   * Stops the animation. Aborting the animation causes the scroller to move to the final x and y
   * position.
   */
  public void abortAnimation() {
    mScroller.abortAnimation();
  }


  /**
   * Notify the scroller that we've reached a horizontal boundary.
   * Normally the information to handle this will already be known
   * when the animation is started, such as in a call to one of the
   * fling functions. However there are cases where this cannot be known
   * in advance. This function will transition the current motion and
   * animate from startX to finalX as appropriate.
   *
   * @param startX Starting/current X position
   * @param finalX Desired final X position
   * @param overX  Magnitude of overscroll allowed. This should be the maximum
   *               desired distance from finalX. Absolute value - must be positive.
   */
  public void notifyHorizontalEdgeReached(int startX, int finalX, int overX) {
    mScroller.notifyHorizontalEdgeReached(startX, finalX, overX);
  }

  /**
   * Notify the scroller that we've reached a vertical boundary.
   * Normally the information to handle this will already be known
   * when the animation is started, such as in a call to one of the
   * fling functions. However there are cases where this cannot be known
   * in advance. This function will animate a parabolic motion from
   * startY to finalY.
   *
   * @param startY Starting/current Y position
   * @param finalY Desired final Y position
   * @param overY  Magnitude of overscroll allowed. This should be the maximum
   *               desired distance from finalY. Absolute value - must be positive.
   */
  public void notifyVerticalEdgeReached(int startY, int finalY, int overY) {
    mScroller.notifyVerticalEdgeReached(startY, finalY, overY);
  }

  /**
   * Returns whether the current Scroller is currently returning to a valid position.
   * Valid bounds were provided by the
   * {@link #fling(int, int, int, int, int, int, int, int, int, int)} method.
   * <p>
   * One should check this value before calling
   * {@link #startScroll(int, int, int, int)} as the interpolation currently in progress
   * to restore a valid position will then be stopped. The caller has to take into account
   * the fact that the started scroll will start from an overscrolled position.
   *
   * @return true when the current position is overscrolled and in the process of
   * interpolating back to a valid value.
   */
  public boolean isOverScrolled() {
    return mScroller.isOverScrolled();
  }
}
