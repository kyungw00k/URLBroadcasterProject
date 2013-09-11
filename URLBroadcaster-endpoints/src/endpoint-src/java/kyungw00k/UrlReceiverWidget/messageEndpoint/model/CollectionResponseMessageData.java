/*
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/*
 * This code was generated by https://code.google.com/p/google-apis-client-generator/
 * (build: 2013-08-26 17:13:51 UTC)
 * on 2013-09-10 at 06:32:01 UTC 
 * Modify at your own risk.
 */

package kyungw00k.UrlReceiverWidget.messageEndpoint.model;

/**
 * Model definition for CollectionResponseMessageData.
 *
 * <p> This is the Java data model class that specifies how to parse/serialize into the JSON that is
 * transmitted over HTTP when working with the messageEndpoint. For a detailed explanation see:
 * <a href="http://code.google.com/p/google-http-java-client/wiki/JSON">http://code.google.com/p/google-http-java-client/wiki/JSON</a>
 * </p>
 *
 * @author Google, Inc.
 */
@SuppressWarnings("javadoc")
public final class CollectionResponseMessageData extends com.google.api.client.json.GenericJson {

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.util.List<MessageData> items;

  static {
    // hack to force ProGuard to consider MessageData used, since otherwise it would be stripped out
    // see http://code.google.com/p/google-api-java-client/issues/detail?id=528
    com.google.api.client.util.Data.nullOf(MessageData.class);
  }

  /**
   * The value may be {@code null}.
   */
  @com.google.api.client.util.Key
  private java.lang.String nextPageToken;

  /**
   * @return value or {@code null} for none
   */
  public java.util.List<MessageData> getItems() {
    return items;
  }

  /**
   * @param items items or {@code null} for none
   */
  public CollectionResponseMessageData setItems(java.util.List<MessageData> items) {
    this.items = items;
    return this;
  }

  /**
   * @return value or {@code null} for none
   */
  public java.lang.String getNextPageToken() {
    return nextPageToken;
  }

  /**
   * @param nextPageToken nextPageToken or {@code null} for none
   */
  public CollectionResponseMessageData setNextPageToken(java.lang.String nextPageToken) {
    this.nextPageToken = nextPageToken;
    return this;
  }

  @Override
  public CollectionResponseMessageData set(String fieldName, Object value) {
    return (CollectionResponseMessageData) super.set(fieldName, value);
  }

  @Override
  public CollectionResponseMessageData clone() {
    return (CollectionResponseMessageData) super.clone();
  }

}
