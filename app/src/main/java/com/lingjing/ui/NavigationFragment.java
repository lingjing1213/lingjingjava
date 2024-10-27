package com.lingjing.ui;



import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationBarItemView;
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
