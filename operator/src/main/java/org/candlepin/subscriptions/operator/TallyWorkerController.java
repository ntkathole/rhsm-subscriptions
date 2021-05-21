/*
 * Copyright Red Hat, Inc.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 *
 * Red Hat trademarks are not licensed under GPLv3. No permission is
 * granted to use or replicate Red Hat trademarks that are incorporated
 * in this software or its documentation.
 */
package org.candlepin.subscriptions.operator;

import io.fabric8.kubernetes.api.model.EnvVarBuilder;
import io.fabric8.kubernetes.api.model.Service;
import io.fabric8.kubernetes.api.model.apps.Deployment;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.javaoperatorsdk.operator.api.Context;
import io.javaoperatorsdk.operator.api.Controller;
import io.javaoperatorsdk.operator.api.DeleteControl;
import io.javaoperatorsdk.operator.api.ResourceController;
import io.javaoperatorsdk.operator.api.UpdateControl;
import java.io.FileNotFoundException;
import java.io.InputStream;
import javax.inject.Inject;
import org.candlepin.subscriptions.operator.TallyWorkerStatus.Status;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Controller(namespaces = Controller.WATCH_CURRENT_NAMESPACE)
public class TallyWorkerController implements ResourceController<TallyWorker> {

  private static final Logger log = LoggerFactory.getLogger(TallyWorkerController.class);

  @Inject KubernetesClient client;

  @Override
  public DeleteControl deleteResource(TallyWorker resource, Context<TallyWorker> context) {
    return DeleteControl.DEFAULT_DELETE;
  }

  @Override
  public UpdateControl<TallyWorker> createOrUpdateResource(
      TallyWorker resource, Context<TallyWorker> context) {
    log.info("Making my resource!");
    TallyWorkerStatus status = resource.getStatus();
    if (status == null) {
      status = new TallyWorkerStatus();
    }
    TallyWorkerSpec spec = resource.getSpec();
    resource.setStatus(status);
    if (needsUpdate(spec, status)) {
      try {
        updateDeployment(spec);
        updateService(spec);
      } catch (FileNotFoundException e) {
        log.error("Error loading resources", e);
        status.setStatus(Status.ERROR);
      }
    }
    return UpdateControl.updateStatusSubResource(resource);
  }

  private void updateService(TallyWorkerSpec spec) throws FileNotFoundException {
    Service service =
        client.services().load(read("tally-worker-service.yaml")).get();

    try {
      client.services().patch(service);
    } catch (Exception e) {
      log.error(e.getClass().toString(), e);
      client.services().createOrReplace(service);
    }
  }

  private InputStream read(String filename) {
    return getClass().getClassLoader().getResourceAsStream(filename);
  }

  private void updateDeployment(TallyWorkerSpec spec) throws FileNotFoundException {
    Deployment deployment =
        client.apps().deployments().load(read("tally-worker-deployment.yaml")).get();
    deployment.getSpec().setReplicas(spec.getNumberReplicas());
    deployment
        .getSpec()
        .getTemplate()
        .getSpec()
        .getContainers()
        .forEach(
            container ->
                container
                    .getEnv()
                    .add(
                        new EnvVarBuilder()
                            .withName("KAFKA_MESSAGE_THREADS")
                            .withValue(Integer.toString(spec.getNumberThreads()))
                            .build()));
    try {
      client.apps().deployments().patch(deployment);
    } catch (Exception e) {
      log.error(e.getClass().toString(), e);
      client.apps().deployments().createOrReplace(deployment);
    }
  }

  private boolean needsUpdate(TallyWorkerSpec spec, TallyWorkerStatus status) {
    if (status == null || status.getStatus() != Status.CREATED) {
      return true;
    }
    return (status.getNumberReplicas() != spec.getNumberReplicas()
        || status.getNumberThreads() != spec.getNumberThreads());
  }
}
