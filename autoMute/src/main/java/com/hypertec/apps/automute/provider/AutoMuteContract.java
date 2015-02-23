/**
 * Copyright (c) 2014 Hypertec Corporation
 * All rights reserved. This program and the accompanying materials
 * are licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * Contributors:
 *   Ryan Chou - initial API and implementation
 */
package com.hypertec.apps.automute.provider;

import android.net.Uri;
import android.provider.BaseColumns;

/**
 * @author Ryan Chou (ryanchou0210@gmail.com)
 *
 */
public final class AutoMuteContract {
	/**
     * This authority is used for writing to or querying from the clock
     * provider.
     */
    public static final String AUTHORITY = "com.hypertec.apps.automute";
    
    /**
     * This utility class cannot be instantiated
     */
    private AutoMuteContract() {}
     
    /**
     * Constants for the Rules table, which contains the user created rules.
     */
    protected interface RulesColumns extends BaseColumns {
        /**
         * The content:// style URL for this table.
         */
        public static final Uri CONTENT_URI = Uri.parse("content://" + AUTHORITY + "/rules");

        /**
         * Title
         *
         * <p>Type: STRING</p>
         */
        public static final String TITLE = "title";

        /**
         * Year
         * <p>Type: INTEGER</p>
         */
        public static final String START_YEAR = "startyear";

        /**
         * Month in the year 1-12
         * <p>Type: INTEGER</p>
         */
        public static final String START_MONTH = "startmonth";

        /**
         * Day in the month, 1 - 28/30/31
         * <p>Type: INTEGER</p>
         */
        public static final String START_DAY = "startday";

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <p>Type: INTEGER</p>
         */
        public static final String START_HOUR = "starthour";

        /**
         * Minutes in localtime 0 - 59.
         * <p>Type: INTEGER</p>
         */
        public static final String START_MINUTE = "startminutes";
        
        /**
         * Year
         * <p>Type: INTEGER</p>
         */
        public static final String END_YEAR = "endyear";

        /**
         * Month in the year 1-12
         * <p>Type: INTEGER</p>
         */
        public static final String END_MONTH = "endmonth";

        /**
         * Day in the month, 1 - 28/30/31
         * <p>Type: INTEGER</p>
         */
        public static final String END_DAY = "endday";

        /**
         * Hour in 24-hour localtime 0 - 23.
         * <p>Type: INTEGER</p>
         */
        public static final String END_HOUR = "endhour";

        /**
         * Minutes in localtime 0 - 59.
         * <p>Type: INTEGER</p>
         */
        public static final String END_MINUTE = "endminutes";

        /**
         * Days of the week encoded as a bit set.
         * <p>Type: INTEGER</p>
         *
         * {@link DaysOfWeek}
         */
        public static final String DAYS_OF_WEEK = "daysofweek";

        /**
         * True if alarm is active.
         * <p>Type: BOOLEAN</p>
         */
        public static final String ENABLED = "enabled";
        
        /**
         * True if alarm should vibrate
         * <p>Type: BOOLEAN</p>
         */
        public static final String VIBRATE = "vibrate";        
    }
}
