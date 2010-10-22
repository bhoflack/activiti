/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.engine.runtime;

import java.util.Date;

import org.activiti.engine.query.Query;


/**
 * Allows programmatic querying of {@link Job}s.
 * 
 * @author Joram Barrez
 */
public interface JobQuery extends Query<JobQuery, Job> {
  
  /** Only select jobs with the given id */
  JobQuery jobId(String jobId);
  
  /** Only select jobs which exist for the given process instance. **/
  JobQuery processInstanceId(String processInstanceId);
  
  /** Only select jobs which exist for the given execution */ 
  JobQuery executionId(String executionId);

  /** Only select jobs which have retries left */
  JobQuery withRetriesLeft();

  /** Only select jobs which are executable, 
   * ie. retries &gt; 0 and duedate is null or duedate is in the past **/
  JobQuery executable();

  /** Only select jobs that are timers. 
   * Cannot be used together with {@link #onlyMessages()} */
  JobQuery onlyTimers();
 
  /** Only select jobs that are messages. 
   * Cannot be used together with {@link #onlyTimers()} */
  JobQuery onlyMessages();
  
  /** Only select jobs where the duedate is lower then the given date.  */
  JobQuery duedateLowerThen(Date date);
  
  /** Only select jobs where the duedate is lower then or equals the given date.  */
  JobQuery duedateLowerThenOrEquals(Date date);
  
  /** Only select jobs where the duedate is higher then the given date. */
  JobQuery duedateHigherThen(Date date);
  
  /** Only select jobs where the duedate is higher then or equals the given date. */
  JobQuery duedateHigherThenOrEquals(Date date);
  
  //sorting //////////////////////////////////////////
  
  /** Order by job id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByJobId();
  
  /** Order by process instance id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByProcessInstanceId();
  
  /** Order by execution id (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByExecutionId();
  
  /** Order by duedate (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByDuedate();
  
  /** Order by retries (needs to be followed by {@link #asc()} or {@link #desc()}). */
  JobQuery orderByRetries();
}
