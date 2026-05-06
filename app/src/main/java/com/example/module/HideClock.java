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
        hookMethod(cl, "com.android.keyguard.KeyguardClockSwitch", "onFinishInflate", true);
        hookMethod(cl, "com.android.keyguard.KeyguardStatusView", "onFinishInflate", true);
        hookMethod(cl, "com.android.keyguard.KeyguardClockSwitch", "updateClockViews", false);
    }

    private void hookMethod(ClassLoader cl, String className, String methodName, boolean runAfter) {
        try {
            Class<?> cls = cl.loadClass(className);
            for (Method m : cls.getDeclaredMethods()) {
                if (m.getName().equals(methodName)) {
                    hook(m).intercept(chain -> {
                        if (!runAfter) {
                            View view = (View) chain.getThisObject();
                            view.setVisibility(View.GONE);
                            removeFromParent(view);
                            return null;
                        }
                        Object result = chain.proceed();
                        hideClock((View) chain.getThisObject());
                        return result;
                    });
                    break;
                }
            }
        } catch (Exception ignored) {}
    }

    static void hideClock(View root) {
        String[] ids = {"keyguard_clock_container", "lockscreen_clock_view",
                "keyguard_clock", "clock_view", "keyguard_status_area"};
        for (String id : ids) {
            int resId = root.getResources().getIdentifier(id, "id", SYSTEMUI);
            if (resId != 0) {
                View child = root.findViewById(resId);
                if (child != null) {
                    child.setVisibility(View.GONE);
                    removeFromParent(child);
                    return;
                }
            }
        }
        root.setVisibility(View.GONE);
        removeFromParent(root);
    }

    static void removeFromParent(View view) {
        try {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) parent.removeView(view);
        } catch (Exception ignored) {}
    }
}
