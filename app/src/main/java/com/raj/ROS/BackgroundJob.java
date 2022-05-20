package com.raj.ROS;

import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A background job launched by Termux.
 */
public final class BackgroundJob {

    private static final String LOG_TAG = "ROSBackground";

    final Process mProcess;
	Runtime runtime;
	String output = "";

    public BackgroundJob(String cwd, String fileToExecute, final String[] args) {
        runtime = Runtime.getRuntime();
		String[] env = buildEnvironment(false, cwd);
        if (cwd == null) cwd = ROSService.HOME_PATH;
		String[] progArray = null;
		progArray = setupProcessArgs(fileToExecute,args);
		int i = 0;while(i != progArray.length){System.out.println("progArray-"+progArray[i]); i++;}
        final String processDescription = Arrays.toString(progArray);
        Process process;
        try {
            process = runtime.exec(progArray, env, new File(cwd));
        } catch (IOException e) {
            mProcess = null;
            // TODO: Visible error message?
            Log.e(LOG_TAG, "Failed running background job: " + processDescription, e);
            return;
        }

        mProcess = process;
        final int pid = getPid(mProcess);

        new Thread() {
            @Override
            public void run() {
                Log.i(LOG_TAG, "[" + pid + "] starting: " + processDescription);
                InputStream stdout = mProcess.getInputStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stdout, StandardCharsets.UTF_8));
                String line;
                try {
                    // FIXME: Long lines.
                    while ((line = reader.readLine()) != null) {
						output = output+"\n\r"+line;
                        Log.i(LOG_TAG, "[" + pid + "] stdout: " + line);
						MainActivity.service.getDefaultLogger().log("BJ","[" + pid + "] stdout: " + line,"INFO");
                    }
                } catch (IOException e) {
                    Log.e(LOG_TAG, "Error reading output", e);
                }

                try {
                    int exitCode = mProcess.waitFor();
                    if (exitCode == 0) {
                        Log.i(LOG_TAG, "[" + pid + "] exited normally");
                    } else {
                        Log.w(LOG_TAG, "[" + pid + "] exited with code: " + exitCode);
                    }
                } catch (InterruptedException e) {
                    // Ignore.
                }
            }
        }.start();


        new Thread() {
            @Override
            public void run() {
                InputStream stderr = mProcess.getErrorStream();
                BufferedReader reader = new BufferedReader(new InputStreamReader(stderr, StandardCharsets.UTF_8));
                String line;
                try {
                    // FIXME: Long lines.
                    while ((line = reader.readLine()) != null) {
						output=output+"\n\r"+line;
                        Log.i(LOG_TAG, "[" + pid + "] stderr: " + line);
						MainActivity.service.getDefaultLogger().log(LOG_TAG, "[" + pid + "] stderr: " + line,"INFO");
                    }
                } catch (IOException e) {
                    // Ignore.
                }
            }
        };
    }
	public String getOutput(){
		return this.output;
	}
    private static void addToEnvIfPresent(List<String> environment, String name) {
        String value = System.getenv(name);
        if (value != null) {
            environment.add(name + "=" + value);
        }
    }

    static String[] buildEnvironment(boolean failSafe, String cwd) {
        new File(ROSService.HOME_PATH).mkdirs();

        if (cwd == null) cwd = ROSService.HOME_PATH;
		new File("/data/data/com.raj.ROS/tmp/.n").mkdirs();
        List<String> environment = new ArrayList<>();
        environment.add("TERM=xterm-256color");
		environment.add("TMPDIR=/data/data/com.raj.ROS/tmp");
        environment.add("HOME=" + ROSService.HOME_PATH);
        environment.add("PREFIX=" + ROSService.PREFIX_PATH);
        environment.add("BOOTCLASSPATH" + System.getenv("BOOTCLASSPATH"));
        environment.add("ANDROID_ROOT=" + System.getenv("ANDROID_ROOT"));
        environment.add("ANDROID_DATA=" + System.getenv("ANDROID_DATA"));
		environment.add("EXTERNAL_STORAGE=" + System.getenv("EXTERNAL_STORAGE"));
		addToEnvIfPresent(environment, "ANDROID_RUNTIME_ROOT");
        addToEnvIfPresent(environment, "ANDROID_TZDATA_ROOT");
        if (failSafe) {
            // Keep the default path so that system binaries can be used in the failsafe session.
            environment.add("PATH= " + System.getenv("PATH"));
        } else {
            environment.add("LD_LIBRARY_PATH=" + ROSService.PREFIX_PATH + "/lib");
            environment.add("LANG=en_US.UTF-8");
            environment.add("PATH=" + ROSService.PREFIX_PATH + "/bin:" + ROSService.PREFIX_PATH + "/bin/applets");
            environment.add("PWD=" + cwd);
            environment.add("TMPDIR=" + ROSService.PREFIX_PATH + "/tmp");
        }

        return environment.toArray(new String[0]);
    }

    public static int getPid(Process p) {
        try {
            Field f = p.getClass().getDeclaredField("pid");
            f.setAccessible(true);
            try {
                return f.getInt(p);
            } finally {
                f.setAccessible(false);
            }
        } catch (Throwable e) {
            return -1;
        }
    }

    static String[] setupProcessArgs(String fileToExecute, String[] args) {
        // The file to execute may either be:
        // - An elf file, in which we execute it directly.
        // - A script file without shebang, which we execute with our standard shell $PREFIX/bin/sh instead of the
        //   system /system/bin/sh. The system shell may vary and may not work at all due to LD_LIBRARY_PATH.
        // - A file with shebang, which we try to handle with e.g. /bin/foo -> $PREFIX/bin/foo.
        String interpreter = null;
        try {
            File file = new File(fileToExecute);
            try (FileInputStream in = new FileInputStream(file)) {
                byte[] buffer = new byte[256];
                int bytesRead = in.read(buffer);
                if (bytesRead > 4) {
                    if (buffer[0] == 0x7F && buffer[1] == 'E' && buffer[2] == 'L' && buffer[3] == 'F') {
                        // Elf file, do nothing.
                    } else if (buffer[0] == '#' && buffer[1] == '!') {
                        // Try to parse shebang.
                        StringBuilder builder = new StringBuilder();
                        for (int i = 2; i < bytesRead; i++) {
                            char c = (char) buffer[i];
                            if (c == ' ' || c == '\n') {
                                if (builder.length() == 0) {
                                    // Skip whitespace after shebang.
                                } else {
                                    // End of shebang.
                                    String executable = builder.toString();
                                    if (executable.startsWith("/usr") || executable.startsWith("/bin")) {
                                        String[] parts = executable.split("/");
                                        String binary = parts[parts.length - 1];
                                        interpreter = ROSService.PREFIX_PATH + "/bin/" + binary;
                                    }
                                    break;
                                }
                            } else {
                                builder.append(c);
                            }
                        }
                    } else {
                        // No shebang and no ELF, use standard shell.
                        interpreter = ROSService.PREFIX_PATH + "/bin/sh";
                    }
                }
            }
        } catch (IOException e) {
            // Ignore.
        }

        List<String> result = new ArrayList<>();
        if (interpreter != null) result.add(interpreter);
        result.add(fileToExecute);
        if (args != null) Collections.addAll(result, args);
		Log.d(LOG_TAG,result.toString());
        return result.toArray(new String[0]);
    }

}
