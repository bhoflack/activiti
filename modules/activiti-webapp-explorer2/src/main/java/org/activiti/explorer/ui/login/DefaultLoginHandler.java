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

package org.activiti.explorer.ui.login;

import java.util.List;

import org.activiti.engine.IdentityService;
import org.activiti.engine.identity.Group;
import org.activiti.engine.identity.User;
import org.activiti.explorer.Constants;
import org.activiti.explorer.LoggedInUser;

/**
 * Default login handler, using activiti's {@link IdentityService}.
 * 
 * @author Frederik Heremans
 */
public class DefaultLoginHandler implements LoginHandler {

  private IdentityService identityService;

  public LoggedInUser authenticate(String userName, String password) {
    LoggedInUser loggedInUser = null;
    if (identityService.checkPassword(userName, password)) {
      User user = identityService.createUserQuery().userId(userName).singleResult();
      // Fetch and cache user data
      loggedInUser = new LoggedInUser(user, password);
      List<Group> groups = identityService.createGroupQuery().groupMember(user.getId()).list();
      for (Group group : groups) {
        if (Constants.SECURITY_ROLE.equals(group.getType())) {
          loggedInUser.addSecurityRoleGroup(group);
          if (Constants.SECURITY_ROLE_USER.equals(group.getId())) {
            loggedInUser.setUser(true);
          }
          if (Constants.SECURITY_ROLE_ADMIN.equals(group.getId())) {
            loggedInUser.setAdmin(true);
          }
        } else {
          loggedInUser.addGroup(group);
        }
      }
    }
    
    return loggedInUser;
  }
  public void setIdentityService(IdentityService identityService) {
    this.identityService = identityService;
  }

}
