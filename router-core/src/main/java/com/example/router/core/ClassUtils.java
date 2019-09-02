package com.example.router.core;

import android.app.Application;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadPoolExecutor;

import dalvik.system.DexFile;

/**
 * Created by YXM
 * on 2019/9/2.
 */

public class ClassUtils {


    public static Set<String> getFileNameByPackageName(Application context,
                                                       final String packageName) throws PackageManager.NameNotFoundException, InterruptedException {
        final Set<String> classNames = new HashSet<>();
        List<String> paths = getSourcePaths(context);
        final CountDownLatch latch = new CountDownLatch(paths.size());
        ThreadPoolExecutor executor = DefaultPoolExecutor.newDefaultPoolExecutor(paths.size());
        for (final String path : paths) {
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    DexFile dexFile = null;
                    try {
                        dexFile = new DexFile(path);
                        Enumeration<String> entries = dexFile.entries();
                        while (entries.hasMoreElements()) {
                            String className = entries.nextElement();
                            if (className.startsWith(packageName)) {
                                classNames.add(className);
                            }
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    } finally {
                        if (null != dexFile) {
                            try {
                                dexFile.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        latch.countDown();
                    }
                }
            });
        }
        latch.await();
        return classNames;
    }

    private static List<String> getSourcePaths(Application context) throws PackageManager.NameNotFoundException {
        ApplicationInfo applicationInfo = context.getPackageManager()
                .getApplicationInfo(context.getPackageName(), 0);
        List<String> sourcePaths = new ArrayList<>();
        sourcePaths.add(applicationInfo.sourceDir);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (applicationInfo.splitPublicSourceDirs != null) {
                sourcePaths.addAll(Arrays.asList(applicationInfo.splitPublicSourceDirs));
            }
        }
        return sourcePaths;
    }
}
