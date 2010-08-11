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
package org.activiti.engine.impl.cmd;

import org.activiti.engine.identity.User;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.persistence.identity.UserEntity;


/**
 * @author Joram Barrez
 */
public class SaveUserCmd implements Command<Void> {
  
  protected UserEntity user;
  
  public SaveUserCmd(User user) {
    this.user = (UserEntity) user;
  }
  
  public Void execute(CommandContext commandContext) {
    if (user.getRevision()==0) {
      commandContext
        .getIdentitySession()
        .insertUser(user);
    } else {
      UserEntity persistentUser = commandContext
        .getIdentitySession()
        .findUserById(user.getId());
      
      persistentUser.update(user);
    }
    
    return null;
  }
}
