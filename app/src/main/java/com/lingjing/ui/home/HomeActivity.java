package com.lingjing.ui.home;


import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import com.lingjing.R;
import com.lingjing.ui.NavigationFragment;
import com.lingjing.ui.home.fragment.HomeFragment;
import com.lingjing.ui.user.fragment.UserFragment;
import com.lingjing.utils.ToastUtils;


/**
 * @Author：灵静
 * @Package：com.lingjing.ui.home
 * @Project：lingjingjava
 * @name：HomeActivity
 * @Date：2024/10/26 下午6:10
 * @Filename：HomeActivity
 * @Version：1.0.0
 */
public class HomeActivity extends AppCompatActivity implements NavigationFragment.OnNavigationItemSelectedListener {

    private static final String TAG = "HomeActivity";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);


        NavigationFragment navigationFragment = new NavigationFragment();
        navigationFragment.setOnNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.navigation_container, navigationFragment).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_container, new HomeFragment()).commit();
        ToastUtils.showToast(this, "欢迎回来");
    }

    @Override
    public void onNavigationItemSelected(int itemId) {

        Fragment selectedFragment = null; // 声明用于存放选中 Fragment 的变量
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment();
        } else if (itemId == R.id.nav_user) {
            selectedFragment = new UserFragment();
        }

        // 确保选中的 Fragment 不为 null，然后替换显示的 Fragment
        if (selectedFragment != null) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_container, selectedFragment) // 替换主内容区域
                    .commit();
        }

    }
}
