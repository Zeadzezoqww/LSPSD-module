package com.example.lsclock;

import androidx.annotation.NonNull;

import io.github.libxposed.api.XposedInterface;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedModuleInterface;

public class HideClock extends XposedModule {

    public HideClock() {
        super();
    }

    @Override
    public void onPackageReady(@NonNull XposedModuleInterface.PackageReadyParam param) {
        if (!param.getPackageName().equals("com.android.systemui")) return;

        ClassLoader cl = param.getClassLoader();
        
        // Log all classes containing "clock" or "Clock"
        try {
            // Try common Halcyon/AOSP clock classes and log which ones exist
            String[] candidates = {
                "com.android.keyguard.KeyguardClockSwitch",
                "com.android.keyguard.AnimatableClockView",
                "com.android.keyguard.TextClock",
                "com.android.systemui.keyguard.ui.viewmodel.KeyguardClockViewModel",
                "com.android.systemui.keyguard.ui.view.KeyguardRootView",
                "com.android.systemui.clocks.ClockRegistry",
                "com.android.systemui.clocks.DefaultClockController",
            };
            
            for (String cls : candidates) {
                try {
                    cl.loadClass(cls);
                    log("FOUND CLASS: " + cls);
                } catch (ClassNotFoundException e) {
                    log("NOT FOUND: " + cls);
                }
            }
        } catch (Exception e) {
            log("Error: " + e.getMessage());
        }
    }
}
