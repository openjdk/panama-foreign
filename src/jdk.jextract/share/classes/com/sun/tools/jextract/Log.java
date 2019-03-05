/*
 * Copyright (c) 2019, Oracle and/or its affiliates. All rights reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Oracle, 500 Oracle Parkway, Redwood Shores, CA 94065 USA
 * or visit www.oracle.com if you need additional information or have any
 * questions.
 */
package com.sun.tools.jextract;

import java.io.OutputStream;
import java.io.PrintWriter;
import java.text.MessageFormat;
import java.util.Locale;
import java.util.ResourceBundle;
import java.util.function.Supplier;
import java.util.logging.Formatter;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.StreamHandler;

public class Log {
    private static final String MESSAGES_RESOURCE = "com.sun.tools.jextract.resources.Messages";

    private static final ResourceBundle MESSAGES_BUNDLE;
    static {
        MESSAGES_BUNDLE = ResourceBundle.getBundle(MESSAGES_RESOURCE, Locale.getDefault());
    }

    private final PrintWriter out;
    private final PrintWriter err;

    private final Logger logger;

    private Log(PrintWriter out, PrintWriter err, Logger logger) {
        this.out = out;
        this.err = err;
        this.logger = logger;
    }

    public static Log createDefault() {
        return Log.of(defaultOut(), defaultErr(), Level.WARNING);
    }

    public static Log of(PrintWriter out, PrintWriter err, Level logLevel) {
        Logger logger = Logger.getAnonymousLogger();
        logger.setUseParentHandlers(false);
        logger.addHandler(createHandler(err)); // err only for now
        logger.setLevel(logLevel);

        return new Log(out, err, logger);
    }

    private static Handler createHandler(PrintWriter pw) {
        return new Handler() {
            @Override
            public void publish(LogRecord record) {
                pw.println(record.getMessage());
            }

            @Override
            public void flush() {
                pw.flush();
            }

            @Override
            public void close() throws SecurityException {
                pw.close();
            }
        };
    }

    public void printError(String messageID, Object... args) {
        printMessage(Level.SEVERE, messageID, args);
    }

    public void printWarning(String messageID, Object... args) {
        printMessage(Level.WARNING, messageID, args);
    }

    public void printNote(String messageID, Object... args) {
        printMessage(Level.INFO, messageID, args);
    }

    public void printMessage(Level level, String messageID, Object... args) {
        print(level, format(messageID, args));
    }

    public void print(Level level, String message) {
        logger.log(level, message);
    }

    public void print(Level level, Supplier<String> message) {
        logger.log(level, message);
    }

    public void printStackTrace(Throwable ex) {
        if(Main.DEBUG) {
            ex.printStackTrace(err);
        }
    }

    public PrintWriter getOut() {
        return out;
    }

    public PrintWriter getErr() {
        return err;
    }

    public static String format(String msgId, Object... args) {
        return new MessageFormat(MESSAGES_BUNDLE.getString(msgId)).format(args);
    }

    public static PrintWriter defaultOut() {
        return new PrintWriter(System.out, true);
    }

    public static PrintWriter defaultErr() {
        return new PrintWriter(System.err, true);
    }

}
