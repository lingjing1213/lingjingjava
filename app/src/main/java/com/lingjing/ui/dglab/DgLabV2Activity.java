package com.lingjing.ui.dglab;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import com.lingjing.R;
import com.lingjing.ui.NavigationFragment;
import com.lingjing.ui.dglab.fragment.DgLabButtonsFragment;
import com.lingjing.ui.home.HomeActivity;
import com.lingjing.ui.home.HomeFragment;
import com.lingjing.ui.user.UserFragment;

/**
 * @Author：灵静
 * @Package：com.lingjing.ui.dglab
 * @Project：lingjingjava
 * @name：DgLabV2Activity
 * @Date：2024/10/29 下午11:18
 * @Filename：DgLabV2Activity
 * @Version：1.0.0
 */
public class DgLabV2Activity extends AppCompatActivity implements NavigationFragment.OnNavigationItemSelectedListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dg_lab_v2);

        NavigationFragment navigationFragment = new NavigationFragment();
        navigationFragment.setOnNavigationItemSelectedListener(this);
        getSupportFragmentManager().beginTransaction().replace(R.id.navigation_container, navigationFragment).commit();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragment_dglabv2_container, new DgLabV2Fragment()).commit();


    }

    @Override
    public void onNavigationItemSelected(int itemId) {
        Fragment selectedFragment = null; // 声明变量来存放选中 Fragment
        if (itemId == R.id.nav_home) {
            selectedFragment = new HomeFragment(); // 选择 HomeFragment
        } else if (itemId == R.id.nav_user) {
            selectedFragment = new UserFragment(); // 选择 UserFragment
        }

        // 获取当前在 Fragment 容器中显示的 Fragment
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.fragment_dglabv2_container);

        // 检查选中的 Fragment 不为空并且不同于当前显示的 Fragment
        if (selectedFragment != null && !(selectedFragment.getClass().isInstance(currentFragment))) {
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragment_dglabv2_container, selectedFragment) // 替换主要 Fragment 区域
                    .addToBackStack(null) // 可选：如果需要的话添加到返回栈
                    .commit();
        }
    }


}
