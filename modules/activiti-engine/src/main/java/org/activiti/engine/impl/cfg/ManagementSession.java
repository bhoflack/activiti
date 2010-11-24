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

package org.activiti.engine.impl.cfg;

import java.util.Map;

import org.activiti.engine.impl.TablePageQueryImpl;
import org.activiti.engine.impl.db.IdBlock;
import org.activiti.engine.management.TableMetaData;
import org.activiti.engine.management.TablePage;


/**
 * @author Tom Baeyens
 */
public interface ManagementSession {

  /* Management */
  Map<String, Long> getTableCount();
  TablePage getTablePage(TablePageQueryImpl tablePageQuery, int firstResult, int maxResults);
  TableMetaData getTableMetaData(String tableName);

  IdBlock getNextIdBlock(int idBlockSize);
}
