/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *       http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.activiti.test.history;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.Date;

import org.activiti.engine.HistoricDataService;
import org.activiti.engine.ProcessInstance;
import org.activiti.engine.impl.ProcessEngineImpl;
import org.activiti.engine.impl.ServiceImpl;
import org.activiti.history.HistoricActivityInstance;
import org.activiti.history.HistoricProcessInstance;
import org.activiti.impl.event.ActivityEndedEvent;
import org.activiti.impl.event.ActivityStartedEvent;
import org.activiti.impl.event.DefaultProcessEventBus;
import org.activiti.impl.event.ProcessInstanceEndedEvent;
import org.activiti.impl.event.ProcessInstanceStartedEvent;
import org.activiti.impl.history.HistoricDataServiceImpl;
import org.activiti.impl.interceptor.Command;
import org.activiti.impl.interceptor.CommandContext;
import org.activiti.impl.time.Clock;
import org.activiti.pvm.Activity;
import org.activiti.pvm.event.ProcessEventBus;
import org.activiti.test.LogInitializer;
import org.activiti.test.ProcessDeployer;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

/**
 * @author Christian Stettler
 */
public class HistoricDataServiceImplTest {

  @Rule
  public LogInitializer logSetup = new LogInitializer();
  @Rule
  public ProcessDeployer deployer = new ProcessDeployer();

  private ProcessEventBus processEventBus;
  private HistoricDataService historicDataService;

  @Before
  public void setUp() {
    processEventBus = new DefaultProcessEventBus();

    historicDataService = new HistoricDataServiceImpl();
    // initialization hack :-)
    ((ServiceImpl)historicDataService).setCommandExecutor(deployer.getCommandExecutor());
    
    ((HistoricDataServiceImpl) historicDataService).registerEventConsumers(processEventBus);
  }

  @Test
  public void testCreateAndCompleteHistoricProcessInstance() {
    try {
      final ProcessInstance processInstance = mock(ProcessInstance.class);
      when(processInstance.getId()).thenReturn("processInstanceId");
      when(processInstance.getProcessDefinitionId()).thenReturn("processDefinitionId");

      Date startTime = new Date();
      Clock.setCurrentTime(startTime);

      fireProcessInstanceStartedEvent(processInstance);

      HistoricProcessInstance historicProcessInstance = historicDataService.findHistoricProcessInstance(processInstance.getId());

      assertNotNull(historicProcessInstance);
      assertEquals("processInstanceId", historicProcessInstance.getProcessInstanceId());
      assertEquals("processDefinitionId", historicProcessInstance.getProcessDefinitionId());
      assertEquals(startTime, historicProcessInstance.getStartTime());
      assertNull(historicProcessInstance.getEndTime());
      assertNull(historicProcessInstance.getDurationInMillis());
      assertNull(historicProcessInstance.getEndStateName());

      Date endTime = new Date(startTime.getTime() + 1000);
      Clock.setCurrentTime(endTime);

      fireProcessInstanceEndedEvent(processInstance);

      historicProcessInstance = historicDataService.findHistoricProcessInstance("processInstanceId");

      assertEquals("processInstanceId", historicProcessInstance.getProcessInstanceId());
      assertEquals("processDefinitionId", historicProcessInstance.getProcessDefinitionId());
      assertEquals(startTime, historicProcessInstance.getStartTime());
      assertEquals(endTime, historicProcessInstance.getEndTime());
      assertEquals(Long.valueOf(1000L), historicProcessInstance.getDurationInMillis());
      assertEquals("endStateName", historicProcessInstance.getEndStateName());
    } finally {
      Clock.reset();
      cleanHistoricProcessInstancesFromDatabase("processInstanceId");
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMarkHistoricProcessInstanceEndedFailsForNonExistingHistoricProcessInstance() {
    ProcessInstance processInstance = mock(ProcessInstance.class);
    when(processInstance.getId()).thenReturn("nonExistingProcessInstanceId");

    fireProcessInstanceEndedEvent(processInstance);
  }

  @Test
  public void testUniqueConstraintsOnHistoricProcessInstance() {
    try {
      ProcessInstance processInstance = mock(ProcessInstance.class);
      when(processInstance.getProcessDefinitionId()).thenReturn("processDefinitionId");

      when(processInstance.getId()).thenReturn("processInstanceIdOne");
      fireProcessInstanceStartedEvent(processInstance);

      try {
        fireProcessInstanceStartedEvent(processInstance);
        fail("unique key constraint violation expected");
      } catch (Exception expected) {
      }

      when(processInstance.getId()).thenReturn("processInstanceIdTwo");
      fireProcessInstanceStartedEvent(processInstance);
    } finally {
      cleanHistoricProcessInstancesFromDatabase("processInstanceIdOne");
      cleanHistoricProcessInstancesFromDatabase("processInstanceIdTwo");
    }
  }

  @Test
  public void testCreateAndCompleteHistoricActivtiyInstance() {
    try {
      Activity activity = mock(Activity.class);
      when(activity.getId()).thenReturn("activityId");
      when(activity.getName()).thenReturn("activityName");
      when(activity.getType()).thenReturn("activityType");

      ProcessInstance processInstance = mock(ProcessInstance.class);
      when(processInstance.getId()).thenReturn("processInstanceId");
      when(processInstance.getProcessDefinitionId()).thenReturn("processDefinitionId");

      HistoricDataService historicDataService = new HistoricDataServiceImpl();
      // initialization hack :-)
      ((ServiceImpl)historicDataService).setCommandExecutor(deployer.getCommandExecutor());

      Date startTime = new Date();
      Clock.setCurrentTime(startTime);

      fireActivityStartedEvent(processInstance, activity);

      HistoricActivityInstance historicActivityInstance = historicDataService.findHistoricActivityInstance("activityId", "processInstanceId");

      assertNotNull(historicActivityInstance);
      assertEquals("activityId", historicActivityInstance.getActivityId());
      assertEquals("activityName", historicActivityInstance.getActivityName());
      assertEquals("activityType", historicActivityInstance.getActivityType());
      assertEquals("processInstanceId", historicActivityInstance.getProcessInstanceId());
      assertEquals("processDefinitionId", historicActivityInstance.getProcessDefinitionId());
      assertEquals(startTime, historicActivityInstance.getStartTime());
      assertNull(historicActivityInstance.getEndTime());
      assertNull(historicActivityInstance.getDurationInMillis());

      Date endTime = new Date(startTime.getTime() + 1000);
      Clock.setCurrentTime(endTime);

      fireActivityEndedEvent(processInstance, activity);

      historicActivityInstance = historicDataService.findHistoricActivityInstance("activityId", "processInstanceId");

      assertEquals("activityId", historicActivityInstance.getActivityId());
      assertEquals("activityName", historicActivityInstance.getActivityName());
      assertEquals("activityType", historicActivityInstance.getActivityType());
      assertEquals("processInstanceId", historicActivityInstance.getProcessInstanceId());
      assertEquals("processDefinitionId", historicActivityInstance.getProcessDefinitionId());
      assertEquals(startTime, historicActivityInstance.getStartTime());
      assertEquals(endTime, historicActivityInstance.getEndTime());
      assertEquals(Long.valueOf(1000L), historicActivityInstance.getDurationInMillis());
    } finally {
      Clock.reset();
      cleanHistoricActivityInstancesFromDatabase("activityId", "processInstanceId");
    }
  }

  @Test(expected = IllegalArgumentException.class)
  public void testMarkHistoricActivityInstanceEndedFailsForNonExistingHistoricActivityInstance() {
    Activity activity = mock(Activity.class);
    when(activity.getId()).thenReturn("activityId");

    ProcessInstance processInstance = mock(ProcessInstance.class);
    when(processInstance.getId()).thenReturn("processInstanceId");

    fireActivityEndedEvent(processInstance, activity);
  }

  @Test
  public void testUnqiueConstraintsOnHistoricActivityInstance() {
    try {
      Activity activity = mock(Activity.class);
      when(activity.getName()).thenReturn("activityName");
      when(activity.getType()).thenReturn("activityType");

      ProcessInstance processInstance = mock(ProcessInstance.class);
      when(processInstance.getProcessDefinitionId()).thenReturn("processDefinitionId");

      when(activity.getId()).thenReturn("activityIdOne");
      when(processInstance.getId()).thenReturn("processInstanceIdOne");
      fireActivityStartedEvent(processInstance, activity);

      try {
        fireActivityStartedEvent(processInstance, activity);
        fail("unique key constraint violation expected");
      } catch (Exception expected) {
      }

      when(activity.getId()).thenReturn("activityIdTwo");
      when(processInstance.getId()).thenReturn("processInstanceIdOne");
      fireActivityStartedEvent(processInstance, activity);

      when(activity.getId()).thenReturn("activityIdOne");
      when(processInstance.getId()).thenReturn("processInstanceIdTwo");
      fireActivityStartedEvent(processInstance, activity);

      when(activity.getId()).thenReturn("activityIdTwo");
      when(processInstance.getId()).thenReturn("processInstanceIdTwo");
      fireActivityStartedEvent(processInstance, activity);
    } finally {
      cleanHistoricActivityInstancesFromDatabase("activityIdOne", "processInstanceIdOne");
      cleanHistoricActivityInstancesFromDatabase("activityIdTwo", "processInstanceIdOne");
      cleanHistoricActivityInstancesFromDatabase("activityIdOne", "processInstanceIdTwo");
      cleanHistoricActivityInstancesFromDatabase("activityIdTwo", "processInstanceIdTwo");
    }
  }

  private void fireProcessInstanceEndedEvent(final ProcessInstance processInstance) {
    deployer.getCommandExecutor().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        processEventBus.postEvent(new ProcessInstanceEndedEvent(processInstance));
        return null;
      }
    });
  }

  private void fireProcessInstanceStartedEvent(final ProcessInstance processInstance) {
    deployer.getCommandExecutor().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        processEventBus.postEvent(new ProcessInstanceStartedEvent(processInstance));
        return null;
      }
    });
  }

  private void fireActivityStartedEvent(final ProcessInstance processInstance, final Activity activity) {
    deployer.getCommandExecutor().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        processEventBus.postEvent(new ActivityStartedEvent(processInstance, activity));
        return null;
      }
    });
  }

  private void fireActivityEndedEvent(final ProcessInstance processInstance, final Activity activity) {
    deployer.getCommandExecutor().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        processEventBus.postEvent(new ActivityEndedEvent(processInstance, activity));
        return null;
      }
    });
  }

  private void cleanHistoricProcessInstancesFromDatabase(final String processInstanceId) {
    deployer.getCommandExecutor().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getPersistenceSession().deleteHistoricProcessInstance(processInstanceId);

        return null;
      }
    });
  }

  private void cleanHistoricActivityInstancesFromDatabase(final String activityId, final String processInstanceId) {
    deployer.getCommandExecutor().execute(new Command<Object>() {
      public Object execute(CommandContext commandContext) {
        commandContext.getPersistenceSession().deleteHistoricActivityInstance(activityId, processInstanceId);

        return null;
      }
    });
  }

}
