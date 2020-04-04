package com.example.bitmapordrawable;

import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.util.Log;

/**
 * 为什么要继承 androidx.appcompat.widget.AppCompatEditText
 * 因为兼容性要好一些
 * 1: 增高
 * 2：重写Padding，预留空间
 * 3：绘制顶部提示文字，需要计算文字的x, y
 * 4：判断文字是否有，有则绘制
 * 5；编写动画
 * 6：1：监听什么时候出现动画:2：什么时候走消失动画，3：然后判断需不需要走动画
 * 7：最后来写动画，本质是通过一个属性动画，也就是通过设定一个动画的完成度这个属性，其他的
 * 诸如透明度、文字大小、文字的位置，就可以根据这个完成度的变化来动态修改
 * 8：注意位置，从下到上，实际上是Y值在缩小，所以要用最大的Y值减去向上的偏移量
 * 9：优化代码：通过懒加载来初始化动画
 * 10：做成一个通用的代码动画工具来使用
 * 11：做一个开关，可以关闭。（1）可以在Java代码里关闭（2）可以在XML文件里面关闭
 * 12：通过获取背景的边界值，来确定是否添加高度来使用，这样就比较安全
 * 13：TypedArray的用法
 *
 */
public class MaterialEditText extends androidx.appcompat.widget.AppCompatEditText {

    private static final float TEXT_SIZE = Utils.dp2px(12f);    // 文字大小
    private static final float TEXT_MARGIN = Utils.dp2px(8f);   // 顶部文字与EditText的间隙
    private static final float VERTICAL_OFFSET = Utils.dp2px(38f);  // 顶部文字的marginTop
    private static final float HORIZONTAL_OFFSET = Utils.dp2px(5f);  // 顶部文字的marginLeft

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private boolean isAnimatedShow; // FloatingLabel 是否显示
    private float floatLabelFraction;   // 动画完成的进度
    private static final float EXTRA_OFFSET = Utils.dp2px(16f); // 从底到顶偏移的总值
    private ObjectAnimator objectAnimator;

    private boolean isUseFloatingLabel;
    private Rect backgroundPadding = new Rect();

    public MaterialEditText(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    private void init(Context context, AttributeSet atts){

        // 获取XML里面的值
        /**
         * attrs：其实就是XML里面的那几个属性值，都被记录到了这个里面
         * */
//        for (int i = 0; i < atts.getAttributeCount(); i++) {
//            Log.d("attrs: ", "key: " + atts.getAttributeName(i) + ", value: " + atts.getAttributeValue(i));
//        }
        TypedArray typedArray = context.obtainStyledAttributes(atts, R.styleable.MaterialEditText);
        isUseFloatingLabel = typedArray.getBoolean(R.styleable.MaterialEditText_useFloatingLabel, true);
        typedArray.recycle();

        refreshPadding();
        paint.setTextSize(TEXT_SIZE);

        addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                // 没显示 而且文字不为空，则需要展示出现动画
                if (!isAnimatedShow && !TextUtils.isEmpty(s)) {
                    getObjectAnimator().start();
                    isAnimatedShow = true;
                    // 显示了 而且文字为空了 需要展示消失动画
                } else if (isAnimatedShow && TextUtils.isEmpty(s)) {
                    getObjectAnimator().reverse();
                    isAnimatedShow = false;
                }
            }
        });
    }

    private void refreshPadding() {
        getBackground().getPadding(backgroundPadding);
        if (isUseFloatingLabel) {
            setPadding(backgroundPadding.left, (int)(backgroundPadding.top + TEXT_SIZE + TEXT_MARGIN), backgroundPadding.right, backgroundPadding.bottom);
        } else {
            setPadding(backgroundPadding.left, backgroundPadding.top, backgroundPadding.right, backgroundPadding.bottom);
        }
    }

    public ObjectAnimator getObjectAnimator() {
        if (objectAnimator == null) {
            objectAnimator = ObjectAnimator.ofFloat(MaterialEditText.this,
                    "floatLabelFraction", 1);
        }
        return objectAnimator;
    }

    public float getFloatLabelFraction() {
        return floatLabelFraction;
    }

    public void setFloatLabelFraction(float floatLabelFraction) {
        this.floatLabelFraction = floatLabelFraction;
        invalidate();
    }

    public boolean isUseFloatingLabel() {
        return isUseFloatingLabel;
    }

    /**
     * 根据设置的值来刷新Padding
     * @param useFloatingLabel
     */
    public void setUseFloatingLabel(boolean useFloatingLabel) {
        if (this.isUseFloatingLabel != useFloatingLabel) {
            this.isUseFloatingLabel = useFloatingLabel;
            refreshPadding();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        if (isUseFloatingLabel) {
            // 根据动画完成度来实现透明度的处理
            paint.setAlpha((int)(floatLabelFraction * 0xff));
            // 往上升 Y值减小 所以要算出一个负值
            float extraOffset = - EXTRA_OFFSET * floatLabelFraction;
            // 通过频繁的绘制不同位置的TextView 来实现动画
            canvas.drawText(getHint().toString(), HORIZONTAL_OFFSET, VERTICAL_OFFSET + extraOffset, paint);
        }
    }
}
