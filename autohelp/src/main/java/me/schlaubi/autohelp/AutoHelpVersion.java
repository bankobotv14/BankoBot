/*
 * MIT License
 *
 * Copyright (c) 2021 BankoBot Contributors
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *
 */

package me.schlaubi.autohelp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Properties;

/**
 * The version of this AutoHelp artifact.
 */
public class AutoHelpVersion {

    private static final Logger LOG = LoggerFactory.getLogger(AutoHelpVersion.class);
    private static final Properties PROPERTIES = new Properties();

    static {
        try {
            PROPERTIES.load(AutoHelpVersion.class.getResourceAsStream("AutoHelpVersion.properties"));
        } catch (IOException e) {
            LOG.warn("Could not load project meta", e);
        }
    }

    /**
     * The release name of this release.
     */
    public static final String VERSION = PROPERTIES.getProperty("version");

    /**
     * The commit hash in the <a href="https://github.com/bankobotv14/BankoBot/tree/main/autohelp">AutoHelp repo</a>
     */
    public static final String COMMIT_HASH = PROPERTIES.getProperty("commit_hash");

}
