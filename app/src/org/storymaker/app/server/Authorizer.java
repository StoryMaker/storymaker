/* Copyright (c) 2010 Google Inc.
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

package org.storymaker.app.server;

import android.app.Activity;
import android.content.Context;

public interface Authorizer {

  public void fetchAccounts(AuthorizationListener<String[]> listener);

  public void addAccount(Activity activity, AuthorizationListener<String> listener);

  public void fetchAuthToken(String account, Activity activity,
      AuthorizationListener<String> listener);

  public String getAuthToken(String accountName);

  public String getFreshAuthToken(String accountName, String authToken);

  public static interface AuthorizationListener<T> {
    public void onSuccess(T result);

    public void onCanceled();

    public void onError(Exception e);
  }

  public static interface AuthorizerFactory {
    public Authorizer getAuthorizer(Context context, String type);
  }

}
