/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.pig.backend.hadoop.executionengine.mapReduceLayer;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.mapreduce.InputSplit;

import org.apache.pig.tools.counters.PigCounterHelper;
import org.apache.pig.tools.pigstats.PigStatsUtil;

/**
 * This class keeps track of errors that occur during loads.
 *
 * Note that a small fraction of bad records are tolerated. But if the rate of
 * errors crosses a threshold (default is 0%) a RuntimeException is thrown. The
 * threshold can be configured by two properties:
 * <code>pig.load.bad.split.threshold</code>: The threshold of tolerance. A
 * value of 1 skips all the bad splits, whereas a value of 0 allows no bad splits.
 * <code>pig.load.bad.split.min</code>: The minimum number of errors that will
 * be tolerated regardless of threshold.
 */
public class InputErrorTracker {

    private static final Log LOG = LogFactory.getLog(InputErrorTracker.class);

    public static final String BAD_SPLIT_THRESHOLD_CONF_KEY = "pig.load.bad.split.threshold";
    public static final String BAD_SPLIT_MIN_COUNT_CONF_KEY = "pig.load.bad.split.min";

    private long numSplits;
    private long numErrors;

    private double errorThreshold; // max fraction of errors allowed
    private long minErrors; // throw error only after this many errors

    private PigCounterHelper pigCounterHelper;

    public InputErrorTracker(Configuration conf) {
        errorThreshold = conf.getFloat(BAD_SPLIT_THRESHOLD_CONF_KEY, 0.0f);
        minErrors = conf.getLong(BAD_SPLIT_MIN_COUNT_CONF_KEY, 0);
        numSplits = 0;
        numErrors = 0;
        pigCounterHelper = new PigCounterHelper();
    }

    public void incSplits() {
        numSplits++;
        pigCounterHelper.incrCounter(PigStatsUtil.INPUT_ERROR_COUNTER_GROUP, PigStatsUtil.INPUT_SPLITS, 1L);
    }

    public void incErrors(InputSplit split, Throwable cause) {
        numErrors++;
        if (numErrors > numSplits) {
            // incorrect use of this class
            throw new RuntimeException("Forgot to invoke incSplits()?");
        }
        pigCounterHelper.incrCounter(PigStatsUtil.INPUT_ERROR_COUNTER_GROUP, PigStatsUtil.INPUT_ERRORS, 1L);

        if (cause == null) {
            cause = new Exception("Unknown error");
        }

        if (errorThreshold <= 0) { // no errors are tolerated
            throw new RuntimeException("error while reading input split: " + split, cause);
        }

        double errRate = numErrors/(double)numSplits;

        // will always excuse the first error. We can decide if single
        // error crosses threshold inside close() if we want to.
        if (numErrors > minErrors && errRate > errorThreshold) {
            throw new RuntimeException("error rate crossed threshold while reading input split: " + split, cause);
        }
    }
}
