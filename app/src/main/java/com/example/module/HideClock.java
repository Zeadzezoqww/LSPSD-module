package com.example.lsclock;

import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

import java.lang.reflect.Method;

public class HideClock extends XposedModule {

    private static final String SYSTEMUI = "com.android.systemui";

    public HideClock() {
        super();
    }

    @Override
    public void onPackageReady(@NonNull XposedModuleInterface.PackageReadyParam param) {
        if (!param.getPackageName().equals(SYSTEMUI)) return;

        ClassLoader cl = param.getClassLoader();

        // Hook DefaultClockFaceLayout - the main clock container
        try {
            Class<?> cls = cl.loadClass(
                "com.android.systemui.customization.clocks.DefaultClockFaceLayout");
            for (Method m : cls.getDeclaredMethods()) {
                hook(m).intercept(chain -> {
                    Object result = chain.proceed();
                    Object thisObj = chain.getThisObject();
                    if (thisObj instanceof View) {
                        hideViewAndChildren((View) thisObj);
                    }
                    return result;
                });
            }
        } catch (Exception ignored) {}

        // Also hook DigitalClockTextView
        try {
            Class<?> cls = cl.loadClass(
                "com.android.systemui.customization.clocks.view.DigitalClockTextView");
            for (Method m : cls.getDeclaredMethods()) {
                hook(m).intercept(chain -> {
                    Object thisObj = chain.getThisObject();
                    if (thisObj instanceof View) {
                        ((View) thisObj).setVisibility(View.GONE);
                    }
                    return null;
                });
            }
        } catch (Exception ignored) {}
    }

    static void hideViewAndChildren(View view) {
        try {
            view.setVisibility(View.GONE);
            view.setAlpha(0f);
            if (view instanceof ViewGroup) {
                ViewGroup group = (ViewGroup) view;
                for (int i = 0; i < group.getChildCount(); i++) {
                    hideViewAndChildren(group.getChildAt(i));
                }
            }
        } catch (Exception ignored) {}
    }
}
