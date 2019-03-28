/*
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

package org.apache.pig.newplan.logical.relational;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.io.ByteArrayOutputStream;

import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.util.HashOutputStream;
import org.apache.pig.newplan.BaseOperatorPlan;
import org.apache.pig.newplan.Operator;
import org.apache.pig.newplan.OperatorPlan;
import org.apache.pig.newplan.logical.DotLOPrinter;
import org.apache.pig.newplan.logical.optimizer.LogicalPlanPrinter;

import com.google.common.base.Charsets;
import com.google.common.hash.HashFunction;
import com.google.common.hash.Hasher;
import com.google.common.hash.Hashing;


/**
 * LogicalPlan is the logical view of relational operations Pig will execute
 * for a given script.  Note that it contains only relational operations.
 * All expressions will be contained in LogicalExpressionPlans inside
 * each relational operator.
 */
public class LogicalPlan extends BaseOperatorPlan {

    public LogicalPlan(LogicalPlan other) {
        // shallow copy constructor
        super(other);
    }

    public LogicalPlan() {
        super();
    }

    /**
     * Equality is checked by calling equals on every leaf in the plan.  This
     * assumes that plans are always connected graphs.  It is somewhat
     * inefficient since every leaf will test equality all the way to
     * every root.  But it is only intended for use in testing, so that
     * should be ok.  Checking predecessors (as opposed to successors) was
     * chosen because splits (which have multiple successors) do not depend
     * on order of outputs for correctness, whereas joins (with multiple
     * predecessors) do.  That is, reversing the outputs of split in the
     * graph has no correctness implications, whereas reversing the inputs
     * of join can.  This method of doing equals will detect predecessors
     * in different orders but not successors in different orders.
     * It will return false if either plan has non deterministic EvalFunc.
     */
    @Override
    public boolean isEqual(OperatorPlan other) throws FrontendException {
        if (other == null || !(other instanceof LogicalPlan)) {
            return false;
        }

        return super.isEqual(other);
    }

    @Override
    public void explain(PrintStream ps, String format, boolean verbose)
    throws FrontendException {
        if (format.equals("xml")) {
            ps.println("<logicalPlan>XML Not Supported</logicalPlan>");
            return;
        }
        
        ps.println("#-----------------------------------------------");
        ps.println("# New Logical Plan:");
        ps.println("#-----------------------------------------------");

        if (this.size() == 0) { 
            ps.println("Logical plan is empty.");
        } else if (format.equals("dot")) {
            DotLOPrinter lpp = new DotLOPrinter(this, ps);
            lpp.dump();
        } else {
            LogicalPlanPrinter npp = new LogicalPlanPrinter(this, ps);
            npp.visit();
        }
    }

    public Operator findByAlias(String alias) {
    	Iterator<Operator> it = getOperators();
    	List<Operator> ops = new ArrayList<Operator>();
    	while( it.hasNext() ) {
    	    LogicalRelationalOperator op = (LogicalRelationalOperator) it.next();
    	    if(op.getAlias() == null)
    	        continue;
    	    if(op.getAlias().equals( alias ) )  {
    	        ops.add( op );
    	    }
    	}

    	if( ops.isEmpty() ) {
            return null;
    	} else {
    		return ops.get( ops.size() - 1 ); // Last one
    	}
    }

    /**
     ** Sandy:  Reverted this to Hemant's 5.5.1 version to keep guava 19 compat.
     * Returns the signature of the LogicalPlan. The signature is a unique identifier for a given
     * plan generated by a Pig script. The same script run multiple times with the same version of
     * Pig is guaranteed to produce the same signature, even if the input or output locations differ.
     *
     * @return a unique identifier for the logical plan
     * @throws FrontendException if signature can't be computed
     */
    public String getSignature() throws FrontendException {
        String logicalPlanString = getLogicalPlanString();
        return Integer.toString(logicalPlanString.hashCode());
    }
 
    public String getHash() throws FrontendException {
        HashFunction hf = Hashing.md5();
        Hasher h = hf.newHasher();
        h.putString(getLogicalPlanString(), Charsets.UTF_16LE);
        return h.hash().toString();
    }
 
    private String getLogicalPlanString() throws FrontendException {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        PrintStream ps = new PrintStream(baos);
        LogicalPlanPrinter printer = new LogicalPlanPrinter(this, ps);
        printer.visit();
        return baos.toString();
    }

}
