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
package org.activiti.impl.cmd;

import java.util.List;

import org.activiti.Task;
import org.activiti.TaskQuery;
import org.activiti.impl.Cmd;
import org.activiti.impl.persistence.PersistenceSession;
import org.activiti.impl.tx.TransactionContext;


/**
 * @author Joram Barrez
 * @deprecated Use the {@link TaskQuery} functionality instead.
 */
public class FindTaskByAssigneeCmd implements Cmd<List<Task>> {
  
  protected String assignee;
  
  public FindTaskByAssigneeCmd(String assignee) {
    this.assignee = assignee;
  }
  
  public List<Task> execute(TransactionContext transactionContext) {
    PersistenceSession persistenceSession = transactionContext.getTransactionalObject(PersistenceSession.class);
    return persistenceSession.findTasksByAssignee(assignee);
  }

}
