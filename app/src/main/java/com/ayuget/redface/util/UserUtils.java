/*
 * Copyright 2015 Ayuget
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

package com.ayuget.redface.util;

import com.ayuget.redface.data.api.model.User;
import com.google.common.base.Optional;

import java.net.CookieManager;
import java.net.CookieStore;
import java.net.HttpCookie;

import okhttp3.OkHttpClient;

public class UserUtils {
    /**
     * Ugly method to fetch a user id from stored cookies
     */
    public static Optional<Integer> getLoggedInUserId(User user, CookieStore cookieStore) {
        for (HttpCookie cookie : cookieStore.getCookies()) {
            if (cookie.getName().equals("md_id")) {
                return Optional.of(Integer.valueOf(cookie.getValue()));
            }
        }

        return Optional.absent();
    }
}
