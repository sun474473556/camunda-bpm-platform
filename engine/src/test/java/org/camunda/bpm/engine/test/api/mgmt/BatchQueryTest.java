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
package org.camunda.bpm.engine.test.api.mgmt;

import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.batchById;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.inverted;
import static org.camunda.bpm.engine.test.api.runtime.TestOrderingUtil.verifySorting;

import java.util.ArrayList;
import java.util.List;

import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.ManagementService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.batch.Batch;
import org.camunda.bpm.engine.exception.NotValidException;
import org.camunda.bpm.engine.exception.NullValueException;
import org.camunda.bpm.engine.test.ProcessEngineRule;
import org.camunda.bpm.engine.test.api.runtime.migration.MigrationTestRule;
import org.camunda.bpm.engine.test.api.runtime.migration.batch.BatchMigrationHelper;
import org.camunda.bpm.engine.test.util.CachedProcessEngineRule;
import org.hamcrest.CoreMatchers;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;

/**
 * @author Thorben Lindhauer
 *
 */
public class BatchQueryTest {

  protected ProcessEngineRule engineRule = new CachedProcessEngineRule();
  protected MigrationTestRule migrationRule = new MigrationTestRule(engineRule);
  protected BatchMigrationHelper helper = new BatchMigrationHelper(engineRule, migrationRule);

  @Rule
  public RuleChain ruleChain = RuleChain.outerRule(engineRule).around(migrationRule);

  protected RuntimeService runtimeService;
  protected ManagementService managementService;
  protected HistoryService historyService;

  @Before
  public void initServices() {
    runtimeService = engineRule.getRuntimeService();
    managementService = engineRule.getManagementService();
    historyService = engineRule.getHistoryService();
  }

  @After
  public void removeBatches() {
    for (Batch batch : managementService.createBatchQuery().list()) {
      managementService.deleteBatch(batch.getId(), true);
    }
  }

  @Test
  public void testBatchQuery() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    Batch batch2 = helper.migrateProcessInstancesAsync(1);

    // when
    List<Batch> list = managementService.createBatchQuery().list();

    // then
    Assert.assertEquals(2, list.size());

    List<String> batchIds = new ArrayList<String>();
    for (Batch resultBatch : list) {
      batchIds.add(resultBatch.getId());
    }

    Assert.assertTrue(batchIds.contains(batch1.getId()));
    Assert.assertTrue(batchIds.contains(batch2.getId()));
  }

  @Test
  public void testBatchQueryResult() {
    // given
    Batch batch = helper.migrateProcessInstancesAsync(1);

    // when
    Batch resultBatch = managementService.createBatchQuery().singleResult();

    // then
    Assert.assertNotNull(batch);

    Assert.assertEquals(batch.getId(), resultBatch.getId());
    Assert.assertEquals(batch.getBatchJobDefinitionId(), resultBatch.getBatchJobDefinitionId());
    Assert.assertEquals(batch.getMonitorJobDefinitionId(), resultBatch.getMonitorJobDefinitionId());
    Assert.assertEquals(batch.getSeedJobDefinitionId(), resultBatch.getSeedJobDefinitionId());
    Assert.assertEquals(batch.getTenantId(), resultBatch.getTenantId());
    Assert.assertEquals(batch.getType(), resultBatch.getType());
    Assert.assertEquals(batch.getBatchJobsPerSeed(), resultBatch.getBatchJobsPerSeed());
    Assert.assertEquals(batch.getInvocationsPerBatchJob(), resultBatch.getInvocationsPerBatchJob());
    Assert.assertEquals(batch.getSize(), resultBatch.getSize());
  }

  @Test
  public void testBatchQueryById() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    Batch resultBatch = managementService.createBatchQuery().batchId(batch1.getId()).singleResult();

    // then
    Assert.assertNotNull(resultBatch);
    Assert.assertEquals(batch1.getId(), resultBatch.getId());
  }

  @Test
  public void testBatchQueryByIdNull() {
    try {
      managementService.createBatchQuery().batchId(null).singleResult();
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Batch id is null"));
    }
  }

  @Test
  public void testBatchQueryByType() {
    // given
    Batch batch1 = helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    long count = managementService.createBatchQuery().type(batch1.getType()).count();

    // then
    Assert.assertEquals(2, count);
  }

  @Test
  public void testBatchQueryByNonExistingType() {
    // given
    helper.migrateProcessInstancesAsync(1);

    // when
    long count = managementService.createBatchQuery().type("foo").count();

    // then
    Assert.assertEquals(0, count);
  }

  @Test
  public void testBatchQueryByTypeNull() {
    try {
      managementService.createBatchQuery().type(null).singleResult();
      Assert.fail("exception expected");
    }
    catch (NullValueException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Type is null"));
    }
  }

  @Test
  public void testBatchQueryCount() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    long count = managementService.createBatchQuery().count();

    // then
    Assert.assertEquals(2, count);
  }

  @Test
  public void testBatchQueryOrderByIdAsc() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    List<Batch> orderedBatches = managementService.createBatchQuery().orderById().asc().list();

    // then
    verifySorting(orderedBatches, batchById());
  }

  @Test
  public void testBatchQueryOrderByIdDec() {
    // given
    helper.migrateProcessInstancesAsync(1);
    helper.migrateProcessInstancesAsync(1);

    // when
    List<Batch> orderedBatches = managementService.createBatchQuery().orderById().desc().list();

    // then
    verifySorting(orderedBatches, inverted(batchById()));
  }

  @Test
  public void testBatchQueryOrderingPropertyWithoutOrder() {
    try {
      managementService.createBatchQuery().orderById().singleResult();
      Assert.fail("exception expected");
    }
    catch (NotValidException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("Invalid query: "
          + "call asc() or desc() after using orderByXX()"));
    }
  }

  @Test
  public void testBatchQueryOrderWithoutOrderingProperty() {
    try {
      managementService.createBatchQuery().asc().singleResult();
      Assert.fail("exception expected");
    }
    catch (NotValidException e) {
      Assert.assertThat(e.getMessage(), CoreMatchers.containsString("You should call any of the orderBy methods "
          + "first before specifying a direction"));
    }
  }
}