/*
 * Copyright 2023 281165273grape@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package io.optimism.utilities.rpc;

import java.net.ConnectException;
import java.util.function.Consumer;
import okhttp3.OkHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.java_websocket.exceptions.WebsocketNotConnectedException;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
import org.web3j.protocol.websocket.WebSocketService;
import org.web3j.tuples.generated.Tuple2;

/**
 * Web3j client provider.
 *
 * @author thinkAfCod
 * @since 2023.06
 */
public class Web3jProvider {

  private Web3jProvider() {}

  /**
   * create web3j client.
   *
   * @param url ethereum/optimism client node url
   * @return web3j client
   */
  public static Web3j createClient(String url) {
    return create(url).component1();
  }

  /**
   * Create web3j client, and return Web3jService.
   * There are more custom operations that can be performed using a Web3jService instance.
   *
   * @param url ethereum/optimism client node url
   * @return web3j client and web3j service
   */
  public static Tuple2<Web3j, Web3jService> create(String url) {
    Web3jService web3Srv = null;
    if (Web3jProvider.isHttp(url)) {
      OkHttpClient okHttpClient =
          new OkHttpClient.Builder().addInterceptor(new RetryRateLimitInterceptor()).build();
      web3Srv = new HttpService(url, okHttpClient);
    } else if (Web3jProvider.isWs(url)) {
      final var web3finalSrv = new WebSocketService(url, true);
      wsConnect(web3finalSrv);
      web3Srv = web3finalSrv;
    } else {
      throw new IllegalArgumentException("not supported scheme:" + url);
    }
    return new Tuple2<>(Web3j.build(web3Srv), web3Srv);
  }

  private static void wsConnect(final WebSocketService wss) {
    final Consumer<Throwable> onError = t -> {
      if (t instanceof WebsocketNotConnectedException) {
        wsConnect(wss);
      }
    };
    try {
      wss.connect(s -> {}, onError, () -> {});
    } catch (ConnectException e) {
      throw new IllegalStateException(e);
    }
  }


  private static boolean isHttp(final String url) {
    return !StringUtils.isEmpty(url) && url.startsWith("http");
  }

  private static boolean isWs(final String url) {
    return !StringUtils.isEmpty(url) && url.startsWith("ws");
  }
}
