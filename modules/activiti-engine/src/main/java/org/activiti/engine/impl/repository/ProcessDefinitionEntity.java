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
package org.activiti.engine.impl.repository;

import java.util.ArrayList;
import java.util.Map;

import org.activiti.engine.impl.db.PersistentObject;
import org.activiti.engine.impl.form.StartFormHandler;
import org.activiti.engine.impl.identity.Authentication;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.runtime.ExecutionEntity;
import org.activiti.engine.impl.runtime.VariableMap;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.pvm.impl.process.ProcessDefinitionImpl;
import org.activiti.pvm.impl.runtime.ExecutionImpl;


/**
 * @author Tom Baeyens
 */
public class ProcessDefinitionEntity extends ProcessDefinitionImpl implements ProcessDefinition, PersistentObject {

  private static final long serialVersionUID = 1L;
  
  protected String key;
  protected int version;
  protected String deploymentId;
  protected String resourceName;
  protected String formKey;
  
  protected Integer historyLevel;
  protected StartFormHandler startFormHandler;
  protected Map<String, TaskDefinition> taskDefinitions;
  
  public ProcessDefinitionEntity() {
    super(null);
  }

  public ExecutionEntity createProcessInstance() {
    ExecutionEntity processInstance = (ExecutionEntity) super.createProcessInstance();
    processInstance.setExecutions(new ArrayList<ExecutionImpl>());
    processInstance.setProcessDefinition(processDefinition);
    // Do not initialize variable map (let it happen lazily)

    // reset the process instance in order to have the db-generated process instance id available
    processInstance.setProcessInstance(processInstance);
    
    String initiatorVariableName = (String) getProperty("initiatorVariableName");
    if (initiatorVariableName!=null) {
      String authenticatedUserId = Authentication.getAuthenticatedUserId();
      processInstance.setVariable(initiatorVariableName, authenticatedUserId);
    }
    
    VariableMap variableMap = VariableMap.createNewInitialized(processInstance.getId(), processInstance.getId());
    processInstance.setVariables(variableMap);
    
    return processInstance;
  }
  
  @Override
  protected ExecutionImpl newProcessInstance() {
    ExecutionEntity processInstance = new ExecutionEntity();

    CommandContext
      .getCurrent()
      .getDbSqlSession()
      .insert(processInstance);

    return processInstance;
  }

  public String toString() {
    return "ProcessDefinitionEntity["+id+"]";
  }

  public String getName() {
    return (String) getProperty("name");
  }
  
  public void setName(String name) {
    setProperty("name", name);
  }

  // getters and setters //////////////////////////////////////////////////////
  
  public Object getPersistentState() {
    return ProcessDefinitionEntity.class;
  }
  
  public String getKey() {
    return key;
  }

  public void setKey(String key) {
    this.key = key;
  }

  public String getDeploymentId() {
    return deploymentId;
  }

  public void setDeploymentId(String deploymentId) {
    this.deploymentId = deploymentId;
  }
  
  public int getVersion() {
    return version;
  }
  
  public void setVersion(int version) {
    this.version = version;
  }

  public void setId(String id) {
    this.id = id;
  }
  
  public String getResourceName() {
    return resourceName;
  }

  public void setResourceName(String resourceName) {
    this.resourceName = resourceName;
  }

  public Integer getHistoryLevel() {
    return historyLevel;
  }

  public void setHistoryLevel(Integer historyLevel) {
    this.historyLevel = historyLevel;
  }

  public StartFormHandler getStartFormHandler() {
    return startFormHandler;
  }

  public void setStartFormHandler(StartFormHandler startFormHandler) {
    this.startFormHandler = startFormHandler;
  }

  public String getFormKey() {
    return formKey;
  }

  public void setFormKey(String formKey) {
    this.formKey = formKey;
  }

  public Map<String, TaskDefinition> getTaskDefinitions() {
    return taskDefinitions;
  }

  public void setTaskDefinitions(Map<String, TaskDefinition> taskDefinitions) {
    this.taskDefinitions = taskDefinitions;
  }
}
