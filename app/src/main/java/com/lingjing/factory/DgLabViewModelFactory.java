package com.lingjing.factory;

import android.app.Application;
import android.content.Intent;

import androidx.activity.result.ActivityResultLauncher;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

import com.lingjing.ui.dglab.DgLabViewModel;

/**
 * @Author：灵静
 * @Package：com.lingjing.factory
 * @Project：lingjingjava
 * @name：DgLabViewModelFactory
 * @Date：2024/11/2 上午1:06
 * @Filename：DgLabViewModelFactory
 * @Version：1.0.0
 */
public class DgLabViewModelFactory implements ViewModelProvider.Factory {
    private final Application application;
    private final ActivityResultLauncher<Intent> requestPermissionLauncher;

    public DgLabViewModelFactory(Application application, ActivityResultLauncher<Intent> requestPermissionLauncher) {
        this.application = application;
        this.requestPermissionLauncher = requestPermissionLauncher;
    }

    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(DgLabViewModel.class)) {
            @SuppressWarnings("unchecked")
            T viewModel = (T) new DgLabViewModel(application, requestPermissionLauncher);
            return viewModel;
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
}