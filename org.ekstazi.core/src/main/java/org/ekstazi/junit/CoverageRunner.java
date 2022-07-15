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

package org.ekstazi.junit;

import org.ekstazi.Config;
import org.ekstazi.Ekstazi;
import org.ekstazi.monitor.CoverageMonitor;
import org.junit.experimental.runners.Enclosed;
import org.junit.runner.Description;
import org.junit.runner.Runner;
import org.junit.runner.manipulation.Filter;
import org.junit.runner.manipulation.Filterable;
import org.junit.runner.manipulation.NoTestsRemainException;
import org.junit.runner.manipulation.Sortable;
import org.junit.runner.manipulation.Sorter;
import org.junit.runner.notification.RunNotifier;
import org.junit.runners.Parameterized;
import org.junit.runners.Suite;

/**
 * Support for collecting coverage for test class granularity level. (Note that
 * we also supports all runners, including non-standard runners, used with
 * RunWith annotation.)
 */
public class CoverageRunner extends Runner implements Filterable, Sortable {

    /** Test class being run */
    private final Class<?> mClz;

    /** Wrapped runner */
    private final Runner mWrappedRunner;

    /** Set of urls that is used when test classes were instantiated */
    private final String[] mURLs;

    /**
     * Constructor.
     */
    public CoverageRunner(Class<?> clz, Runner wrapped) {
        this(clz, wrapped, null);
    }

    /**
     * Constructor.
     */
    public CoverageRunner(Class<?> clz, Runner wrapped, String[] urls) {
        this.mClz = clz;
        this.mWrappedRunner = wrapped;
        this.mURLs = urls;
    }
    
    @Override
    public Description getDescription() {
        if (mWrappedRunner == null) {
            return Description.EMPTY;
        }
        return mWrappedRunner.getDescription();
    }

    @Override
    public void run(RunNotifier notifier) {
        if (isIgnoreAllTests() || mWrappedRunner == null) {
            return;
        } else if (isRunWithoutCoverage()) {
            mWrappedRunner.run(notifier);
        } else {
            Ekstazi.inst().beginClassCoverage(mClz.getName());
            JUnit4OutcomeListener outcomeListener = new JUnit4OutcomeListener();
            notifier.addListener(outcomeListener);
            try {
                mWrappedRunner.run(notifier);
            } finally {
                // Include URLs from constructors.
                if (mURLs != null) CoverageMonitor.addURLs(mURLs);
                Ekstazi.inst().endClassCoverage(mClz.getName(), outcomeListener.isFailOrError());
            }
        }
    }

    // INTERNAL

    /**
     * Check if for any reason we want to ignore all the tests. We ignore all
     * the tests if a flag is set for Tool; note however that this flag is only
     * for testing/debugging/weird purposes.
     */
    private boolean isIgnoreAllTests() {
        return Config.X_IGNORE_ALL_TESTS_V;
    }

    /**
     * This method defines when coverage should not be collected. Specifically,
     * the coverage should not be collected in case if wrapped runner
     * satisfies one of the following conditions: 1) it is our affecting runner,
     * 2) test suite (Suite.class) runner with several classes; however, we do
     * want to collect coverage for Parametrized runners (which are subclasses
     * of Suite runner).
     * 
     * @return True if coverage should not be collected, false otherwise.
     */
    private boolean isRunWithoutCoverage() {
        return (mWrappedRunner instanceof AffectingRunner) ||
                (mWrappedRunner instanceof Suite &&
                 !(mWrappedRunner instanceof Parameterized || mWrappedRunner instanceof Enclosed));
        // We do not ignore SuiteMethod (JUnit 3), as this class will run all
        // the test defined in suite() method without constructing any other
        // runner.  TODO: we can try to optimize and run each part ourselves.
        // mWrapped instanceof SuiteMethod
    }

    /**
     * IMPORTANT: We need the following method as the original Runner might
     * implement the interface and we need to propagate the call. Note that this
     * can get tricky if somebody checks for instanceof on the Runner (we would
     * be out). Implementing this method was inspired by ant and
     * "ant junit-single-test -Djunit.testcase=org.apache.tools.ant.taskdefs.optional.junit.JUnitTaskTest"
     * that actually invokes filter to run only a single method.
     */
    public void filter(Filter filter) throws NoTestsRemainException {
        if (!(mWrappedRunner instanceof Filterable)) {
            return;
        }
        Filterable filterable = (Filterable) mWrappedRunner;
        filterable.filter(filter);
    }

    /**
     * IMPORTANT: We need the following method as the original Runner might
     * implement the interface and we need to propagate the call. Note that this
     * can get tricky if somebody checks for instanceof on the Runner (we would
     * be out). Implementing this method was inspired by ant and
     * "ant junit-single-test -Djunit.testcase=org.apache.tools.ant.taskdefs.optional.junit.JUnitTaskTest"
     * that actually invokes filter to run only a single method.
     */
    public void sort(Sorter sorter) {
        if (!(mWrappedRunner instanceof Sortable)) {
            return;
        }
        Sortable sortable = (Sortable) mWrappedRunner;
        sortable.sort(sorter);
    }
}
