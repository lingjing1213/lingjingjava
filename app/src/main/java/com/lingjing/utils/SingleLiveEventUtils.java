package com.lingjing.utils;

import androidx.annotation.MainThread;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * @Author：灵静
 * @Package：com.lingjing.utils
 * @Project：lingjingjava
 * @name：SingleLiveEventUtils
 * @Date：2024/11/9 上午2:27
 * @Filename：SingleLiveEventUtils
 * @Version：1.0.0
 */
public class SingleLiveEventUtils<T> extends MutableLiveData<T> {


    private final AtomicBoolean hasHandled = new AtomicBoolean(false);

    @MainThread
    public void observe(@NonNull LifecycleOwner owner, @NonNull final Observer<? super T> observer) {
        super.observe(owner, t -> {
            if (hasHandled.compareAndSet(false, true)) {
                observer.onChanged(t);
            }
        });
    }

    @MainThread
    public void setValue(@Nullable T value) {
        hasHandled.set(false);
        super.setValue(value);
    }

    @MainThread
    public void call() {
        setValue(null);
    }

    public void clear() {
        hasHandled.set(false);
    }

}
