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
package org.candlepin.subscriptions.resource;

import com.google.gson.Gson;
import javax.ws.rs.QueryParam;
import lombok.extern.slf4j.Slf4j;
import org.candlepin.subscriptions.json.TallySummary;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/bananas")
public class MarketplaceRequeueController {

  @PostMapping
  public @ResponseBody
  ResponseEntity<String> resubTallySummary(@RequestBody TallySummary tallySummary,
      @QueryParam("forceFailure") boolean forceFailure) {

    if (forceFailure) {
      throw new RuntimeException("ahhhhhhhh");
    }

    var json = new Gson().toJson(tallySummary);

    return ResponseEntity.ok(json);
  }


  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ExceptionHandler({Exception.class})
  public void handleException(Exception e) {
    log.error("Eeeep, error doing something somewhere", e);

  }
}
