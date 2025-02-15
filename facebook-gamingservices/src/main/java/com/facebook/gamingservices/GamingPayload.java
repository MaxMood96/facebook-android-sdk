/*
 * Copyright (c) 2014-present, Facebook, Inc. All rights reserved.
 *
 * You are hereby granted a non-exclusive, worldwide, royalty-free license to use,
 * copy, modify, and distribute this software in source code or binary form for use
 * in connection with the web services and APIs provided by Facebook.
 *
 * As with any software that integrates with the Facebook platform, your use of
 * this software is subject to the Facebook Developer Principles and Policies
 * [http://developers.facebook.com/policy/]. This copyright notice shall be
 * included in all copies or substantial portions of the software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY, FITNESS
 * FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER
 * IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN
 * CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */
package com.facebook.gamingservices;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import androidx.annotation.Nullable;
import java.util.HashMap;
import java.util.Map;
import org.json.JSONException;
import org.json.JSONObject;

public class GamingPayload {

  private static final String TAG = GamingPayload.class.getSimpleName();
  private static final String KEY_CONTEXT_TOKEN_ID = "context_token_id";
  private static final String KEY_GAME_REQUEST_ID = "game_request_id";
  private static final String KEY_PAYLOAD = "payload";
  private static final String KEY_APPLINK_DATA = "al_applink_data";
  private static final String KEY_EXTRAS = "extras";

  private static Map<String, String> payloadData;

  private GamingPayload() {}

  /**
   * Retrieves the Game Request ID that referred the user to the game.
   *
   * <p>When a user sends a Game Request, the recipient can launch the game directly from Facebook,
   * the resulting deeplink will provide the referring Game Request ID.
   *
   * @return GameRequestID if any.
   */
  public static @Nullable String getGameRequestID() {
    if (GamingPayload.payloadData == null) {
      return null;
    }
    return GamingPayload.payloadData.getOrDefault(KEY_GAME_REQUEST_ID, null);
  }

  /**
   * Retrieves a payload sent from Facebook to this game.
   *
   * <p>When a GameRequest contains the data field, it will be forwarded here as a payload.
   *
   * @return GameRequest payload (if any).
   */
  public static @Nullable String getPayload() {
    if (GamingPayload.payloadData == null) {
      return null;
    }
    return GamingPayload.payloadData.getOrDefault(KEY_PAYLOAD, null);
  }

  /**
   * Retireves any Gaming Payload bundled in the start arguments for a Game running on Facebook
   * Cloud. This is called automatically by the Cloud Init handler.
   *
   * @param payloadString JSON Encoded payload.
   */
  public static void loadPayloadFromCloudGame(String payloadString) {
    Map<String, String> loadedPayload = new HashMap<>();
    try {
      JSONObject payloadJSON = new JSONObject(payloadString);
      loadedPayload.put(KEY_GAME_REQUEST_ID, payloadJSON.optString(KEY_GAME_REQUEST_ID));
      loadedPayload.put(KEY_PAYLOAD, payloadJSON.getString(KEY_PAYLOAD));

      GamingPayload.payloadData = loadedPayload;
    } catch (JSONException e) {
      Log.e(TAG, e.toString(), e);
    }
  }

  /**
   * Retrieves any Gaming Payload bundled within the Intent that launched the Game.
   *
   * @param intent Intent that lanched this Game.
   */
  public static void loadPayloadFromIntent(Intent intent) {
    Map<String, String> loadedPayload = new HashMap<>();
    if (intent == null) {
      return;
    }
    Bundle extras = intent.getExtras();

    if (extras != null && extras.containsKey(KEY_APPLINK_DATA)) {
      Bundle appLinkData = extras.getBundle(KEY_APPLINK_DATA);
      Bundle appLinkExtras = appLinkData.getBundle(KEY_EXTRAS);

      if (appLinkExtras != null) {
        String gameRequestId = appLinkExtras.getString(KEY_GAME_REQUEST_ID);
        String payload = appLinkExtras.getString(KEY_PAYLOAD);
        String contextTokenId = appLinkExtras.getString(KEY_CONTEXT_TOKEN_ID);

        if (contextTokenId != null) {
          GamingContext.setCurrentGamingContext(new GamingContext(contextTokenId));
        }

        loadedPayload.put(KEY_GAME_REQUEST_ID, gameRequestId);
        loadedPayload.put(KEY_PAYLOAD, payload);
        GamingPayload.payloadData = loadedPayload;
      }
    }
  }
}
