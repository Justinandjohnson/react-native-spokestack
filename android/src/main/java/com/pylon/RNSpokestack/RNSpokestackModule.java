
package com.pylon.RNSpokestack;

import com.pylon.spokestack.SpeechPipeline;
import com.pylon.spokestack.SpeechContext;
import com.pylon.spokestack.OnSpeechEventListener;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;

import com.facebook.react.modules.core.DeviceEventManagerModule;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableArray;
import com.facebook.react.bridge.Arguments;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;

import java.util.ArrayList;
import javax.annotation.Nullable;

public class RNSpokestackModule extends ReactContextBaseJavaModule implements OnSpeechEventListener {

  private final ReactApplicationContext reactContext;
  private SpeechPipeline pipeline;

  public RNSpokestackModule(ReactApplicationContext reactContext) {
    super(reactContext);
    this.reactContext = reactContext;
  }

  @Override
  public String getName() {
    return "Spokestack";
  }

  private void sendEvent(String eventName, @Nullable WritableMap params) {
      this.reactContext
          .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
          .emit(eventName, params);
  }

  @ReactMethod
  public void initialize(ReadableMap config) {
    assert config.hasKey("input") : "'input' key is required in config";
    assert config.hasKey("stages") : "'stages' key is required in config";

    final SpeechPipeline.Builder builder = new SpeechPipeline.Builder();

    builder.setInputClass(config.getString("input"));

    for (Object stage : config.getArray("stages").toArrayList()) {
      builder.addStageClass(stage.toString());
    }

    if (config.hasKey("string_properties")) {
      ReadableMap string_properties = config.getMap("string_properties");
      ReadableMapKeySetIterator string_properties_it = string_properties.keySetIterator();
      while (string_properties_it.hasNextKey()) {
        String key = string_properties_it.nextKey();
        String value = string_properties.getString(key);
        builder.setProperty(key, value);
      }
    }

    if (config.hasKey("int_properties")) {
      ReadableMap int_properties = config.getMap("int_properties");
      ReadableMapKeySetIterator int_properties_it = int_properties.keySetIterator();
      while (int_properties_it.hasNextKey()) {
        String key = int_properties_it.nextKey();
        int value = int_properties.getInt(key);
        builder.setProperty(key, value);
      }
    }

    pipeline = builder.build();
  }

  @ReactMethod
  public void start() throws Exception {
    pipeline.start();
  }

  @ReactMethod
  public void stop () {
    pipeline.stop();
  }

  public void onEvent(SpeechContext.Event event, SpeechContext context) {
    WritableMap react_event = Arguments.createMap();
    react_event.putString("event", event.name());
    react_event.putString("transcript", context.getTranscript());
    react_event.putBoolean("isActive", context.isActive());
    sendEvent("onSpeechEvent", react_event);
  }
}
