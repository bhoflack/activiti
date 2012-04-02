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

package org.activiti.engine.impl.bpmn.diagram;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;


/**
 * XML {@link NamespaceContext} containing the namespaces used by BPMN 2.0 XML documents.
 *
 * Can be used in {@link XPath#setNamespaceContext(NamespaceContext)}.
 * 
 * @author Falko Menge
 */
public class Bpmn20NamespaceContext implements NamespaceContext {
  
  Map<String, String> namespaceUris;
  
  public Bpmn20NamespaceContext() {
    namespaceUris = new HashMap<String, String>();
    namespaceUris.put("bpmn", "http://www.omg.org/spec/BPMN/20100524/MODEL");
    namespaceUris.put("bpmndi", "http://www.omg.org/spec/BPMN/20100524/DI");
    namespaceUris.put("omgdi", "http://www.omg.org/spec/DD/20100524/DI");
    namespaceUris.put("omgdc", "http://www.omg.org/spec/DD/20100524/DC");
  }

  public String getNamespaceURI(String prefix) {
    return namespaceUris.get(prefix);
  }

  public String getPrefix(String namespaceURI) {
    return getKeyByValue(namespaceUris, namespaceURI);
  }

  public Iterator<String> getPrefixes(String namespaceURI) {
    return getKeysByValue(namespaceUris, namespaceURI).iterator();
  }

  private static <T, E> Set<T> getKeysByValue(Map<T, E> map, E value) {
    Set<T> keys = new HashSet<T>();
    for (Entry<T, E> entry : map.entrySet()) {
      if (value.equals(entry.getValue())) {
        keys.add(entry.getKey());
      }
    }
    return keys;
  }

  private static <T, E> T getKeyByValue(Map<T, E> map, E value) {
    for (Entry<T, E> entry : map.entrySet()) {
      if (value.equals(entry.getValue())) {
        return entry.getKey();
      }
    }
    return null;
  }

}
