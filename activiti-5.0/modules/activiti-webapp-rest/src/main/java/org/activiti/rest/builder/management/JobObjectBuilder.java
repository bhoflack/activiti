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

package org.activiti.rest.builder.management;

import java.util.Map;

import org.activiti.engine.runtime.Job;
import org.activiti.rest.builder.BaseJSONObjectBuilder;
import org.json.JSONException;
import org.json.JSONObject;


/**
 * @author Frederik Heremans
 */
public class JobObjectBuilder extends BaseJSONObjectBuilder {

  private JobJSONConverter converter = new JobJSONConverter();
  
  @Override
  public JSONObject createJsonObject(Object modelObject) throws JSONException {
    Map<String, Object> model = getModelAsMap(modelObject);
    
    Job job = (Job) model.get("job");      
    JSONObject json = converter.getJSONObject(job);
    
    // Also, add stacktrace. If null, property "stacktrace" won't be added
    json.put("stacktrace", model.get("stacktrace"));
    return json;
  }

}
