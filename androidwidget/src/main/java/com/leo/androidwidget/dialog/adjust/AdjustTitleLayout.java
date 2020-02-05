package com.leo.androidwidget.dialog.adjust;

import android.text.TextUtils;
import android.view.View;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.leo.androidwidget.R;
import com.leo.androidwidget.Utils;
import com.leo.androidwidget.dialog.SmartDialog;


public class AdjustTitleLayout implements AdjustStyle {

  @Override
  public void apply(@NonNull SmartDialog dialog) {
    View popupView = dialog.getPopupView();
    TextView titleView = popupView.findViewById(R.id.title);
    if (titleView != null) {
      if (TextUtils.isEmpty(dialog.getBuilder().getContentText())) {
        titleView.setPadding(titleView.getPaddingLeft(), titleView.getPaddingTop(),
            titleView.getPaddingRight(), 0);
      } else if (titleView.getLineCount() > 1) {
        titleView.setPadding(titleView.getPaddingLeft(), titleView.getPaddingTop(),
            titleView.getPaddingRight(),
            Utils.INSTANCE.getDimensionPixelSize(R.dimen.dialog_title_multi_line_padding_bottom));
      }
    }
  }
}
