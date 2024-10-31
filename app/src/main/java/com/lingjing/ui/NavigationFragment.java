package com.lingjing.ui;



import android.graphics.Color;
import android.os.Bundle;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lingjing.R;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui
 * @Project：lingjingjava
 * @name：NavigationFragment
 * @Date：2024/10/27 下午7:25
 * @Filename：NavigationFragment
 * @Version：1.0.0
 */
public class NavigationFragment extends Fragment {
    public static final String TAG = "NavigationFragment";

    private OnNavigationItemSelectedListener listener;

    public interface OnNavigationItemSelectedListener  {

        void onNavigationItemSelected(int itemId);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_navigation, container, false);
        BottomNavigationView bottomNavigationView = view.findViewById(R.id.bottom_navigation);
        bottomNavigationView.setSelectedItemId(R.id.nav_home);
        bottomNavigationView.setItemIconTintList(null);
        // 设置每个菜单项的标题颜色
        for (int i = 0; i < bottomNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = bottomNavigationView.getMenu().getItem(i);
            SpannableString title = new SpannableString(menuItem.getTitle());
            title.setSpan(new ForegroundColorSpan(Color.parseColor("#8A5C8A")), 0, title.length(), Spannable.SPAN_INCLUSIVE_INCLUSIVE);
            menuItem.setTitle(title);
        }

        bottomNavigationView.setOnItemSelectedListener(item -> {
            if (listener != null) {
                listener.onNavigationItemSelected(item.getItemId());
            }
            return true;
        });


        return view;
    }

    public void setOnNavigationItemSelectedListener(OnNavigationItemSelectedListener listener) {
        this.listener = listener;
    }
}
