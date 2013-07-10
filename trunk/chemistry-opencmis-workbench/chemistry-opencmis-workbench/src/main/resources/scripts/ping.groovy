/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
import org.apache.chemistry.opencmis.commons.*
import org.apache.chemistry.opencmis.commons.data.*
import org.apache.chemistry.opencmis.commons.enums.*
import org.apache.chemistry.opencmis.client.api.*

CmisObject root = session.getRootFolder();

ping({
    root.refresh();
});




def ping(func, int sleep = 2, int turns = 1000) {
    func();
    
    int i = 0;
    long total = 0;
    long max = 0;
    long min = Long.MAX_VALUE;

    while(i <= turns) {
        i++;
        
        long start = System.currentTimeMillis();
        func();
        long end = System.currentTimeMillis();
        
        long time = end - start;
        total += time;
        if(max < time) { max = time; }
        if(min > time) { min = time; }
        
        println String.format('[%1s] %2$5d: %3$5d ms   (min: %4$5d ms, max: %5$5d ms, avg: %6$7.1f ms)', 
            (new Date(start)).format('yyyy-MM-dd hh:mm:ss'), 
            i,
            time,
            min,
            max,
            total / i);

        Thread.sleep(sleep * 1000);
    }
}