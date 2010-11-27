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

package org.activiti.standalone.history;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.history.HistoricDetail;
import org.activiti.engine.history.HistoricFormProperty;
import org.activiti.engine.history.HistoricVariableUpdate;
import org.activiti.engine.impl.test.ResourceActivitiTestCase;
import org.activiti.engine.impl.util.ClockUtil;
import org.activiti.engine.repository.ProcessDefinition;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.test.Deployment;
import org.activiti.engine.test.history.SerializableVariable;


/**
 * @author Tom Baeyens
 */
public class FullHistoryTest extends ResourceActivitiTestCase {

  public FullHistoryTest() {
    super("org/activiti/standalone/history/fullhistory.activiti.cfg.xml");
  }

  @Deployment
  public void testVariableUpdates() {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("number", "one");
    variables.put("character", "a");
    variables.put("bytes", ":-(".getBytes());
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("receiveTask", variables);
    runtimeService.setVariable(processInstance.getId(), "number", "two");
    runtimeService.setVariable(processInstance.getId(), "bytes", ":-)".getBytes());
    
    List<HistoricDetail> historicDetails = historyService
      .createHistoricDetailQuery()
      .orderByVariableName().asc()
      .orderByVariableRevision().asc()
      .list();
    
    HistoricVariableUpdate historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(0);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-(", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(0, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(1);
    assertEquals("bytes", historicVariableUpdate.getVariableName());
    assertEquals(":-)", new String((byte[])historicVariableUpdate.getValue()));
    assertEquals(1, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(2);
    assertEquals("character", historicVariableUpdate.getVariableName());
    assertEquals("a", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(3);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("one", historicVariableUpdate.getValue());
    assertEquals(0, historicVariableUpdate.getRevision());
    
    historicVariableUpdate = (HistoricVariableUpdate) historicDetails.get(4);
    assertEquals("number", historicVariableUpdate.getVariableName());
    assertEquals("two", historicVariableUpdate.getValue());
    assertEquals(1, historicVariableUpdate.getRevision());
  }
 
  @Deployment
  public void testHistoricVariableUpdatesAllTypes() throws Exception {
    
    SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS");
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("aVariable", "initial value");
    
    Date startedDate = sdf.parse("01/01/2001 01:23:45 000");
    
    // In the javaDelegate, the current time is manipulated
    Date updatedDate = sdf.parse("01/01/2001 01:23:46 000");
    
    ClockUtil.setCurrentTime(startedDate);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("HistoricVariableUpdateProcess", variables);
    
    List<HistoricDetail> details = historyService.createHistoricDetailQuery()
      .variableUpdates()
      .processInstanceId(processInstance.getId())
      .orderByVariableName().asc()
      .list();
    
    // 8 variable updates should be present, one performed when starting process
    // the other 7 are set in VariableSetter serviceTask
    assertEquals(9, details.size());
    
    // Since we order by varName, first entry should be aVariable update from startTask
    HistoricVariableUpdate startVarUpdate = (HistoricVariableUpdate) details.get(0);
    assertEquals("aVariable", startVarUpdate.getVariableName());
    assertEquals("initial value", startVarUpdate.getValue());
    assertEquals(0, startVarUpdate.getRevision());
    assertEquals(processInstance.getId(), startVarUpdate.getProcessInstanceId());
    // Date should the the one set when starting
    assertEquals(startedDate, startVarUpdate.getTime());
    
    HistoricVariableUpdate updatedStringVariable = (HistoricVariableUpdate) details.get(1);
    assertEquals("aVariable", updatedStringVariable.getVariableName());
    assertEquals("updated value", updatedStringVariable.getValue());
    assertEquals(processInstance.getId(), updatedStringVariable.getProcessInstanceId());
    // Date should be the updated date
    assertEquals(updatedDate, updatedStringVariable.getTime());
    
    HistoricVariableUpdate intVariable = (HistoricVariableUpdate) details.get(2);
    assertEquals("bVariable", intVariable.getVariableName());
    assertEquals(123, intVariable.getValue());
    assertEquals(processInstance.getId(), intVariable.getProcessInstanceId());
    assertEquals(updatedDate, intVariable.getTime());
    
    HistoricVariableUpdate longVariable = (HistoricVariableUpdate) details.get(3);
    assertEquals("cVariable", longVariable.getVariableName());
    assertEquals(12345L, longVariable.getValue());
    assertEquals(processInstance.getId(), longVariable.getProcessInstanceId());
    assertEquals(updatedDate, longVariable.getTime());
    
    HistoricVariableUpdate doubleVariable = (HistoricVariableUpdate) details.get(4);
    assertEquals("dVariable", doubleVariable.getVariableName());
    assertEquals(1234.567, doubleVariable.getValue());
    assertEquals(processInstance.getId(), doubleVariable.getProcessInstanceId());
    assertEquals(updatedDate, doubleVariable.getTime());
    
    HistoricVariableUpdate shortVariable = (HistoricVariableUpdate) details.get(5);
    assertEquals("eVariable", shortVariable.getVariableName());
    assertEquals((short)12, shortVariable.getValue());
    assertEquals(processInstance.getId(), shortVariable.getProcessInstanceId());
    assertEquals(updatedDate, shortVariable.getTime());
    
    HistoricVariableUpdate dateVariable = (HistoricVariableUpdate) details.get(6);
    assertEquals("fVariable", dateVariable.getVariableName());
    assertEquals(sdf.parse("01/01/2001 01:23:45 678"), dateVariable.getValue());
    assertEquals(processInstance.getId(), dateVariable.getProcessInstanceId());
    assertEquals(updatedDate, dateVariable.getTime());
    
    HistoricVariableUpdate serializableVariable = (HistoricVariableUpdate) details.get(7);
    assertEquals("gVariable", serializableVariable.getVariableName());
    assertEquals(new SerializableVariable("hello hello"), serializableVariable.getValue());
    assertEquals(processInstance.getId(), serializableVariable.getProcessInstanceId());
    assertEquals(updatedDate, serializableVariable.getTime());
    
    HistoricVariableUpdate byteArrayVariable = (HistoricVariableUpdate) details.get(8);
    assertEquals("hVariable", byteArrayVariable.getVariableName());
    assertEquals(";-)", new String((byte[])byteArrayVariable.getValue()));
    assertEquals(processInstance.getId(), byteArrayVariable.getProcessInstanceId());
    assertEquals(updatedDate, byteArrayVariable.getTime());
    
  }
  
  @Deployment
  public void testHistoricFormProperties() throws Exception {
    Date startedDate = new SimpleDateFormat("dd/MM/yyyy hh:mm:ss SSS").parse("01/01/2001 01:23:46 000");
    
    ClockUtil.setCurrentTime(startedDate);
    
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("formProp1", "Activiti rocks");
    formProperties.put("formProp2", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("historicFormPropertiesProcess").singleResult();
    
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);
    
    // 2 historic form properties should be created
    
    List<HistoricDetail> props = historyService.createHistoricDetailQuery()
      .formProperties()
      .processInstanceId(processInstance.getId())
      .orderByFormPropertyId().asc()
      .list();
    
    HistoricFormProperty historicProperty1 = (HistoricFormProperty) props.get(0);
    assertEquals("formProp1", historicProperty1.getPropertyId());
    assertEquals("Activiti rocks", historicProperty1.getPropertyValue());
    assertEquals(startedDate, historicProperty1.getTime());
    assertEquals(processInstance.getId(), historicProperty1.getProcessInstanceId());
    assertNull(historicProperty1.getActivityInstanceId());
    
    HistoricFormProperty historicProperty2 = (HistoricFormProperty) props.get(1);
    assertEquals("formProp2", historicProperty2.getPropertyId());
    assertEquals("12345", historicProperty2.getPropertyValue());
    assertEquals(startedDate, historicProperty2.getTime());
    assertEquals(processInstance.getId(), historicProperty2.getProcessInstanceId());
    assertNull(historicProperty2.getActivityInstanceId());
  }
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQuery() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);
    
    ProcessInstance processInstance = runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
   
    // Query on activity-instance, activity instance null will return all vars set when starting process
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().activityId(null).count());
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().activityId("unexisting").count());
    
    // Query on process-instance
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().variableUpdates().processInstanceId("unexisting").count());
    
  }
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricFormPropertiesQuery() throws Exception {
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("stringVar", "activiti rocks!");
    formProperties.put("longVar", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);
   
    // Query on activity-instance, activity instance null will return all vars set when starting process
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().activityId(null).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().activityId("unexisting").count());
    
    // Query on process-instance
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().processInstanceId(processInstance.getId()).count());
    assertEquals(0, historyService.createHistoricDetailQuery().formProperties().processInstanceId("unexisting").count());
  }
  
  
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricVariableQuerySorting() throws Exception {
    Map<String, Object> variables = new HashMap<String, Object>();
    variables.put("stringVar", "activiti rocks!");
    variables.put("longVar", 12345L);
    
    runtimeService.startProcessInstanceByKey("oneTaskProcess", variables);
   
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().asc().list().size());
    
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByProcessInstanceId().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByTime().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableName().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableRevision().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().variableUpdates().orderByVariableType().desc().list().size());
  }
  
  @Deployment(
    resources={"org/activiti/engine/test/history/oneTaskProcess.bpmn20.xml"})
  public void testHistoricFormPropertySorting() throws Exception {
    
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("stringVar", "activiti rocks!");
    formProperties.put("longVar", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("oneTaskProcess").singleResult();
    formService.submitStartFormData(procDef.getId() , formProperties);
   
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().asc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().asc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().desc().count());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().desc().count());
    
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().asc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().asc().list().size());
    
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByProcessInstanceId().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByTime().desc().list().size());
    assertEquals(2, historyService.createHistoricDetailQuery().formProperties().orderByFormPropertyId().desc().list().size());
  }
  
  @Deployment
  public void testHistoricDetailQueryMixed() throws Exception {
    
    Map<String, String> formProperties = new HashMap<String, String>();
    formProperties.put("formProp1", "activiti rocks!");
    formProperties.put("formProp2", "12345");
    
    ProcessDefinition procDef = repositoryService.createProcessDefinitionQuery().processDefinitionKey("historicDetailMixed").singleResult();
    ProcessInstance processInstance = formService.submitStartFormData(procDef.getId() , formProperties);
   
    List<HistoricDetail> details = historyService
      .createHistoricDetailQuery()
      .processInstanceId(processInstance.getId())
      .orderByVariableName().asc()
      .list();
   
    assertEquals(4, details.size());
    
    assertTrue(details.get(0) instanceof HistoricFormProperty);
    HistoricFormProperty formProp1 = (HistoricFormProperty) details.get(0);
    assertEquals("formProp1", formProp1.getPropertyId());
    assertEquals("activiti rocks!", formProp1.getPropertyValue());
    
    assertTrue(details.get(1) instanceof HistoricFormProperty);
    HistoricFormProperty formProp2 = (HistoricFormProperty) details.get(1);
    assertEquals("formProp2", formProp2.getPropertyId());
    assertEquals("12345", formProp2.getPropertyValue());
    
    
    assertTrue(details.get(2) instanceof HistoricVariableUpdate);
    HistoricVariableUpdate varUpdate1 = (HistoricVariableUpdate) details.get(2);
    assertEquals("variable1", varUpdate1.getVariableName());
    assertEquals("activiti rocks!", varUpdate1.getValue());
    
    
    // This variable should be of type LONG since this is defined in the process-definition
    assertTrue(details.get(3) instanceof HistoricVariableUpdate);
    HistoricVariableUpdate varUpdate2 = (HistoricVariableUpdate) details.get(3);
    assertEquals("variable2", varUpdate2.getVariableName());
    assertEquals(12345L, varUpdate2.getValue());
  }
  
  
  
  public void testHistoricDetailQueryInvalidSorting() throws Exception {
    try {
      historyService.createHistoricDetailQuery().asc().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().desc().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByProcessInstanceId().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByTime().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByVariableName().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByVariableRevision().list();
      fail();
    } catch (ActivitiException e) {
      
    }
    
    try {
      historyService.createHistoricDetailQuery().orderByVariableType().list();
      fail();
    } catch (ActivitiException e) {
      
    }
  }
}
