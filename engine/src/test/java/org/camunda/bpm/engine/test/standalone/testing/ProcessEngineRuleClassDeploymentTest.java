/*
 * Copyright © 2012 - 2018 camunda services GmbH and various authors (info@camunda.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.camunda.bpm.engine.test.standalone.testing;


import org.camunda.bpm.engine.test.Deployment;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.util.ProvidedProcessEngineRule;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;

@Deployment
public class ProcessEngineRuleClassDeploymentTest {

  @Rule
  public final ProcessEngineRule processEngineRule = new ProvidedProcessEngineRule();

  @Test
  public void testDeploymentOnClassLevel() {
    assertNotNull("process is not deployed",processEngineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTest").singleResult());
  }

  @Test
  @Deployment
  public void testDeploymentOnMethodOverridesClass() {
    assertNotNull("process is not deployed",processEngineRule.getRepositoryService().createProcessDefinitionQuery().processDefinitionKey("testHelperDeploymentTestOverride").singleResult());
  }

}
