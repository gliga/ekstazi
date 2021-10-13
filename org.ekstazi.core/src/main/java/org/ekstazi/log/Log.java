/*
 * Copyright 2014-present Milos Gligoric
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.ekstazi.log;

import org.ekstazi.Config;

import java.io.*;
import java.util.List;

/**
 * Simple logging facility.
 */
public final class Log {

    private static final String DEBUG_TEXT = "Ekstazi_Debug";
    private static final String ERROR_TEXT = "Ekstazi_Error";
    private static final String WARN_TEXT = "Ekstazi_Warn";
    private static final String CONF_TEXT = "Ekstazi_Conf";

    private static PrintWriter pwScreen;
    private static PrintWriter pwFile;

    public static void initScreen() {
        init(true, false, null);
    }

    public static void init(boolean printToScreen, boolean printToFile, String logFileName) {
        if (printToScreen) {
            pwScreen = new PrintWriter(System.err);
        }
        if (printToFile) {
            try {
                File file = new File(logFileName);
                if (!file.getParentFile().exists() && !file.getParentFile().mkdirs()) {
                    w("Was unable to create log file");
                    return;
                }
                pwFile = new PrintWriter(file);
                Runtime.getRuntime().addShutdownHook(new Thread() {
                    @Override
                    public void run() {
                        pwFile.close();
                    }
                });
            } catch (FileNotFoundException ex) {
                ex.printStackTrace();
            }
        }
    }

    /**
     * Debugging.
     */
    public static final void d(Object... messages) {
        if (Config.DEBUG_V) {
            print(DEBUG_TEXT + ": ");
            for (int i = 0; i < messages.length; i++) {
                print(messages[i]);
                if (i != messages.length - 1) {
                    print(" ");
                }
            }
            println(".");
        }
    }

    /**
     * Debugging.
     */
    public static final void d(String msg, int val) {
        if (Config.DEBUG_V) {
            d(msg, Integer.toString(val));
        }
    }

    /**
     * Error during initialization (e.g., configuration error).
     *
     * @param msg
     *            Message to be reported.
     */
    public static final void e(String msg) {
        println(ERROR_TEXT + ": " + msg);
    }

    public static final void e(String msg, Exception ex) {
        e(msg);
        if (Config.DEBUG_V) {
            ex.printStackTrace();
        }
    }

    /**
     * Something may affect performance but not correctness.
     */
    public static final void w(String msg) {
        println(WARN_TEXT + ": " + msg);
    }

    /**
     * Printing configuration options.
     */
    public static final void c(String msg) {
        if (msg.replaceAll("\\s+", "").equals("")) {
            println(CONF_TEXT);
        } else {
            println(CONF_TEXT + ": " + msg);
        }
    }

    public static final void c(Object key, Object value) {
        if (key.equals("") && value.equals("")) {
            c("");
        } else {
            c(key + " = " + value);
        }
    }

    private static void print(Object s) {
        if (pwScreen != null) {
            pwScreen.print(s);
            pwScreen.flush();
        }
        if (pwFile != null) {
            pwFile.print(s);
            pwFile.flush();
        }
    }

    public static void println(Object s) {
        if (pwScreen != null) {
            pwScreen.println(s);
            pwScreen.flush();
        }
        if (pwFile != null) {
            pwFile.println(s);
            pwFile.flush();
        }
    }

    public static void d2f (String s) {
        try {
            FileWriter fw = new FileWriter("Shuai_debug.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            bw.write(s);
            bw.newLine();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void d2f (List<String> list) {
        try {
            FileWriter fw = new FileWriter("Shuai_debug.txt", true);
            BufferedWriter bw = new BufferedWriter(fw);
            for (String s : list) {
                bw.write(s);
                bw.newLine();
            }
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void trace2f () {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        new Throwable().printStackTrace(pw);
        String sStackTrace = sw.toString(); // stack trace as a string
        d2f(sStackTrace);
    }

}
