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

package io.optimism.rpc.provider;

import okhttp3.OkHttpClient;
import org.web3j.protocol.Web3j;
import org.web3j.protocol.Web3jService;
import org.web3j.protocol.http.HttpService;
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
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new RetryRateLimitInterceptor())
                .build();
        return Web3j.build(new HttpService(url, okHttpClient));
    }

    /**
     * create web3j client.
     *
     * @param url ethereum/optimism client node url
     * @return web3j client and web3j service
     */
    public static Tuple2<Web3j, Web3jService> create(String url) {
        OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addInterceptor(new RetryRateLimitInterceptor())
                .build();
        Web3jService web3jService = new HttpService(url, okHttpClient);
        return new Tuple2<>(Web3j.build(web3jService), web3jService);
    }
}
