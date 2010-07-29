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

package org.activiti.engine.test;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;

import org.activiti.engine.ActivitiException;
import org.activiti.engine.DeploymentBuilder;
import org.activiti.engine.HistoricDataService;
import org.activiti.engine.IdentityService;
import org.activiti.engine.ManagementService;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngineBuilder;
import org.activiti.engine.ProcessService;
import org.activiti.engine.RepositoryService;
import org.activiti.engine.TaskService;
import org.activiti.engine.impl.util.ClassNameUtil;
import org.activiti.impl.time.Clock;
import org.activiti.impl.util.LogUtil;
import org.activiti.impl.util.LogUtil.ThreadLogMode;
import org.activiti.test.ProcessDeployer;


/** JUnit 3 style base class that only exposes the public API services. 
 * 
 * @author Tom Baeyens
 */
public class ProcessEngineTestCase extends TestCase {

  private static final String EMPTY_LINE = "                                                                                           ";

  static {
    LogUtil.readJavaUtilLoggingConfigFromClasspath();
  }
  
  private static Logger log = Logger.getLogger(ProcessEngineTestCase.class.getName());

  private static final ThreadLogMode DEFAULT_THREAD_LOG_MODE = ThreadLogMode.INDENT;
  private static final String DEFAULT_CONFIGURATION_RESOURCE = "activiti.properties";
  private static Map<String, ProcessEngine> processEngines = new HashMap<String, ProcessEngine>(); 
  
  protected ThreadLogMode threadRenderingMode;
  protected String configurationResource;
  protected List<String> deploymentsToDeleteAfterTestMethod = new ArrayList<String>();

  protected ProcessEngine processEngine;
  protected RepositoryService repositoryService;
  protected ProcessService processService;
  protected TaskService taskService;
  protected HistoricDataService historicDataService;
  protected IdentityService identityService;
  protected ManagementService managementService;

  public ProcessEngineTestCase() {
    this(DEFAULT_CONFIGURATION_RESOURCE, DEFAULT_THREAD_LOG_MODE);
  }
  
  public ProcessEngineTestCase(String configurationResource) {
    this(configurationResource, DEFAULT_THREAD_LOG_MODE);
  }
  
  public ProcessEngineTestCase(ThreadLogMode threadRenderingMode) {
    this(DEFAULT_CONFIGURATION_RESOURCE, threadRenderingMode);
  }
  
  public ProcessEngineTestCase(String configurationResource, ThreadLogMode threadRenderingMode) {
    this.configurationResource = configurationResource;
    this.threadRenderingMode = threadRenderingMode;
  }
  
  @Override
  protected void runTest() throws Throwable {
    LogUtil.resetThreadIndents();
    ThreadLogMode oldThreadRenderingMode = LogUtil.setThreadLogMode(threadRenderingMode);
    
    if (processEngine==null) {
      processEngine = processEngines.get(configurationResource);
      if (processEngine==null) {
        log.fine("==== BUILDING PROCESS ENGINE ========================================================================");
        processEngine = new ProcessEngineBuilder()
          .configureFromPropertiesResource(configurationResource)
          .buildProcessEngine();
        log.fine("==== PROCESS ENGINE CREATED =========================================================================");
      }
      initializeServices();
    }

    log.fine(EMPTY_LINE);
    log.fine("#### START "+ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName()+" ###########################################################");

    try {
      
      processDeploymentAnnotation();
      
      super.runTest();

    }  catch (AssertionFailedError e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "ASSERTION FAILED: "+e, e);
      throw e;
    } catch (Throwable e) {
      log.severe(EMPTY_LINE);
      log.log(Level.SEVERE, "EXCEPTION: "+e, e);
      throw e;
    } finally {
      for (String deploymentId: deploymentsToDeleteAfterTestMethod) {
        repositoryService.deleteDeployment(deploymentId);
      }
      Clock.reset();
      log.fine("#### END "+ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName()+" #############################################################");
      LogUtil.setThreadLogMode(oldThreadRenderingMode);
    }
  }

  protected void processDeploymentAnnotation() {
    Method method = null;
    try {
      method = getClass().getDeclaredMethod(getName(), (Class<?>[])null);
    } catch (Exception e) {
      throw new ActivitiException("can't get method by reflection", e);
    }
    Deployment deploymentAnnotation = method.getAnnotation(Deployment.class);
    if (deploymentAnnotation != null) {
      String[] resources = deploymentAnnotation.resources();
      if (resources.length == 0) {
        String name = method.getName();
        String resource = ProcessDeployer.getBpmnProcessDefinitionResource(getClass(), name);
        resources = new String[]{resource};
      }
      
      DeploymentBuilder deploymentBuilder = repositoryService
        .createDeployment()
        .name(ClassNameUtil.getClassNameWithoutPackage(this)+"."+getName());
      
      for (String resource: resources) {
        deploymentBuilder.addClasspathResource(resource);
      }
      
      String deploymentId = deploymentBuilder.deploy().getId();
      deploymentsToDeleteAfterTestMethod.add(deploymentId);
    }
  }

  void initializeServices() {
    repositoryService = processEngine.getRepositoryService();
    processService = processEngine.getProcessService();
    taskService = processEngine.getTaskService();
    historicDataService = processEngine.getHistoricDataService();
    identityService = processEngine.getIdentityService();
    managementService = processEngine.getManagementService();
  }
}
