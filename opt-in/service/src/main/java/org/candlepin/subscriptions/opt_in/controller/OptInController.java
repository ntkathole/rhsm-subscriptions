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
package org.candlepin.subscriptions.opt_in.controller;

import org.candlepin.subscriptions.opt_in.api.model.OptInConfig;
import org.candlepin.subscriptions.opt_in.api.resources.OptInApi;
import org.candlepin.subscriptions.opt_in.dao.OptInDao;
import org.candlepin.subscriptions.opt_in.db.model.OptInType;
import org.candlepin.subscriptions.resource.ResourceUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/* The @RestController annotation is a composed annotation of @Controller and @ResponseBody.
 * However, the template the API generation uses sets all our method return types to
 * ResponseEntity. This choice means that the @ResponseBody annotation is functionally useless
 * since we are going to have to be responsible for binding our model object into the response.
 * Nevertheless, I'm choosing to use @RestController since it carries semantic meaning and is
 * what people expect to see.  See also https://stackoverflow.com/a/40454751/6124862
 */
@RestController
@RequestMapping("/opt-in")
public class OptInController implements OptInApi {
  private final OptInDao optInDao;

  public OptInController(OptInDao optInDao) {
    this.optInDao = optInDao;
  }

  @Override
  public ResponseEntity<Void> deleteOptInConfig() {
    try {
      optInDao.optOut(validateAccountNumber(), validateOrgId());
      // TODO need to make sure this accords with what RestEasy returns
      return ResponseEntity.ok().build();
    }
    catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  @Override
  public ResponseEntity<OptInConfig> getOptInConfig() {
    try {
      var body = optInDao.getOptInConfig(validateAccountNumber(), validateOrgId());

      return ResponseEntity.ok(body);
    }
    catch (IllegalArgumentException e) {
      // TODO need to figure out how to make the return type flexible OR map exceptions
      return ResponseEntity.badRequest().build();
//      var error = new Error()
//        .code(ErrorCode.VALIDATION_FAILED_ERROR.getCode())
//        .title("Bad Request")
//        .detail(e.getMessage());
//      return ResponseEntity.badRequest().body()
    }
  }

  @Override
  public ResponseEntity<OptInConfig> putOptInConfig(
      Boolean enableTallySync, Boolean enableTallyReporting, Boolean enableConduitSync) {

    try {
      var body = optInDao.optIn(
          validateAccountNumber(),
          validateOrgId(),
          OptInType.API,
          trueIfNull(enableTallySync),
          trueIfNull(enableTallyReporting),
          trueIfNull(enableConduitSync));

      return ResponseEntity.ok(body);
    } catch (IllegalArgumentException e) {
      return ResponseEntity.badRequest().build();
    }
  }

  private String validateAccountNumber() {
    String accountNumber = ResourceUtils.getAccountNumber();
    if (accountNumber == null) {
      throw new IllegalArgumentException("Must specify an account number.");
    }
    return accountNumber;
  }

  private String validateOrgId() {
    String ownerId = ResourceUtils.getOwnerId();
    if (ownerId == null) {
      throw new IllegalArgumentException("Must specify an org ID.");
    }
    return ownerId;
  }

  private Boolean trueIfNull(Boolean toVerify) {
    if (toVerify == null) {
      return true;
    }
    return toVerify;
  }

}
