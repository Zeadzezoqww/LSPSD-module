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

        String[] classes = {
            "com.android.systemui.customization.clocks.DefaultClockFaceLayout",
            "com.android.systemui.customization.clocks.view.DigitalClockTextView",
            "com.android.systemui.clock.ui.viewmodel.ClockViewModel",
            "com.android.keyguard.ClockEventController",
            "com.android.systemui.keyguard.domain.interactor.KeyguardClockInteractor",
        };

        String[] methods = {
            "onFinishInflate",
            "onAttachedToWindow",
            "onMeasure",
            "onDraw",
            "setVisibility",
        };

        for (String cls : classes) {
            for (String method : methods) {
                hookMethod(cl, cls, method);
            }
        }
    }

    private void hookMethod(ClassLoader cl, String className, String methodName) {
        try {
            Class<?> cls = cl.loadClass(className);
            for (Method m : cls.getDeclaredMethods()) {
                if (m.getName().equals(methodName)) {
                    hook(m).intercept(chain -> {
                        Object thisObj = chain.getThisObject();
                        if (thisObj instanceof View) {
                            View view = (View) thisObj;
                            view.setVisibility(View.GONE);
                            view.setAlpha(0f);
                            ViewGroup parent = (ViewGroup) view.getParent();
                            if (parent != null) parent.removeView(view);
                            return null;
                        }
                        return chain.proceed();
                    });
                }
            }
        } catch (Exception ignored) {}
    }
}
