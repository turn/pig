/**
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements. See the NOTICE file distributed with this
 * work for additional information regarding copyright ownership. The ASF
 * licenses this file to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package org.apache.pig.builtin;

import java.io.IOException;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

import org.apache.hadoop.mapreduce.Job;
import org.apache.pig.LoadFuncMetadataWrapper;
import org.apache.pig.LoadMetadata;
import org.apache.pig.LoadPushDown;
import org.apache.pig.impl.logicalLayer.FrontendException;
import org.apache.pig.impl.util.JarManager;

/**
 * Wrapper class which will delegate calls to parquet.pig.ParquetLoader
 */
public class ParquetLoader extends LoadFuncMetadataWrapper implements LoadPushDown {

    public ParquetLoader() throws FrontendException {
        this(null);
    }
    
    public ParquetLoader(String requestedSchemaStr) throws FrontendException {
        Throwable exception = null;
        try {
            Class parquetLoader = Class.forName("parquet.pig.ParquetLoader");
            Constructor constructor = parquetLoader.getConstructor(String.class);

            init((LoadMetadata) constructor.newInstance(requestedSchemaStr));
        }
        // if compile time dependency not found at runtime
        catch (NoClassDefFoundError e) {
            exception = e;
        } catch (ClassNotFoundException e) {
            exception = e;
        } catch (NoSuchMethodException e) {
            exception = e;
        } catch (InvocationTargetException e) {
            exception = e;
        } catch (InstantiationException e) {
            exception = e;
        } catch (IllegalAccessException e) {
            exception = e;
        }

      if(exception != null) {
          throw new FrontendException(String.format("Cannot instantiate class %s (%s)",
            getClass().getName(), "parquet.pig.ParquetLoader"), 2259, exception);
      }
    }
    
    private void init(LoadMetadata loadMetadata) {
        setLoadFunc(loadMetadata);
    }
    
    @Override
    public void setLocation(String location, Job job) throws IOException {
        try {
            JarManager.addDependencyJars(job, Class.forName("parquet.Version.class"));
        } catch (ClassNotFoundException e) {
            throw new IOException("Runtime parquet dependency not found", e);
        }
        super.setLocation(location, job);
    }

    @Override
    public List<OperatorSet> getFeatures() {
        return ((LoadPushDown)super.loadFunc()).getFeatures();
    }

    @Override
    public RequiredFieldResponse pushProjection(RequiredFieldList requiredFieldList)
            throws FrontendException {
        return ((LoadPushDown)super.loadFunc()).pushProjection(requiredFieldList);
    }
    
}