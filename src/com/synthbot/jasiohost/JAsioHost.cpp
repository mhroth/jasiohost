/*
 *  Copyright 2009 Martin Roth (mhroth@gmail.com)
 * 
 *  This file is part of JAsioHost.
 *
 *  JVstHost is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  JVstHost is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *  
 *  You should have received a copy of the GNU Lesser General Public License
 *  along with JAsioHost.  If not, see <http://www.gnu.org/licenses/>.
 *
 */

#include <stdio.h>
#include <string.h>
#include "asiosys.h"
#include "asio.h"
#include "asiodrivers.h"
#include "iasiothiscallresolver.h"
#include "com_synthbot_jasiohost_JAsioHost.h"
#include "com_synthbot_jasiohost_AsioDriver.h"

// external reference
extern AsioDrivers* asioDrivers;
bool loadAsioDriver(char *name);

// global variable
JavaVM *jvm;

int reverseBytes(int i) {
  int j = i & 0x000000FF;
  j <<= 8; i >>= 8;
  j |= i & 0x000000FF;
  j <<= 8; i >>= 8;
  j |= i & 0x000000FF;
  j <<= 8; i >>= 8;
  j |= i & 0x000000FF;
  return j;
}

JNIEXPORT jint JNICALL JNI_OnLoad(JavaVM *localJvm, void *reserved) {  
  jvm = localJvm; // store the JVM so that it can be used to attach ASIO threads to the JVM during callbacks
  asioDrivers = new AsioDrivers(); // set the global variable
  return JNI_VERSION_1_4;
}

JNIEXPORT void JNICALL JNI_OnUnload(JavaVM *jvm, void *reserved) {
  if (asioDrivers) {
    delete asioDrivers;
  }
}

/*
 * Driver Callbacks
 */

// from ASIOv2
ASIOTime* bufferSwitchTimeInfo(ASIOTime* timeInfo, long bufferIndex, ASIOBool directProcess) {
  JNIEnv *env = NULL;
  jvm->AttachCurrentThread((void **) &env, NULL);
  if (env != NULL) {
    /*
      get primitive input java array
      copy input into it
      release java array
    
    
    
    env->CallVoidMethod(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"),
      env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireBufferSwitch", "([[I[[I)V"),
      NULL,
      NULL);
    
    
      copy contents of java output arrays into native arrays
     memcpy(bufferInfo.buffers[bufferIndex], output, bufferSize * sizeof(sample))
    
    switch ( sample type ) {
      case ASIOSTInt32LSB:
      case ASIOSTInt32LSB16:
      case ASIOSTInt32LSB18:
      case ASIOSTInt32LSB20:
      case ASIOSTInt32LSB24: {
        // reverseEndian() on all samples
      }
    }
    */
    
    // ASIOOutputReady(); // not sure if this is really necessary in windows
  }
  jvm->DetachCurrentThread();
  
  
  // calls System.out.println(message);
  env->CallObjectMethod(
      env->GetStaticObjectField(
          env->FindClass("java/lang/System"), 
          env->GetStaticFieldID(env->FindClass("java/lang/System"), "out", "Ljava/io/PrintStream;")), 
      env->GetMethodID(env->FindClass("java/io/PrintStream"), "println", "(Ljava/lang/String;)V"), 
      "boo!");
  
  return NULL; // dunno what to do with this yet...
}

// from ASIOv1
void bufferSwitch(long bufferIndex, ASIOBool directProcess) {
  ASIOTime timeInfo;
  memset(&timeInfo, 0, sizeof(timeInfo));
  ASIOError errorCode = ASIOGetSamplePosition(&timeInfo.timeInfo.samplePosition, &timeInfo.timeInfo.systemTime);
  if (errorCode == ASE_OK) {
    timeInfo.timeInfo.flags = kSystemTimeValid | kSamplePositionValid;
  }
  bufferSwitchTimeInfo (&timeInfo, bufferIndex, directProcess);
}

void sampleRateDidChange(ASIOSampleRate sampleRate) {
  JNIEnv *env = NULL;
  jvm->AttachCurrentThread((void **) &env, NULL);
  if (env != NULL) {
    env->CallVoidMethod(
      env->FindClass("com/synthbot/jasiohost/AsioDriver"),
      env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireSampleRateDidChange", "(D)V"),
      (jdouble) sampleRate);
  }
  jvm->DetachCurrentThread();
}

long asioMessage(long selector, long value, void* message, double* opt) {
  switch (selector) {
      
    case kAsioSelectorSupported: {
      switch (value) {
        case kAsioEngineVersion:
        case kAsioResetRequest:
        case kAsioResyncRequest:
        case kAsioLatenciesChanged:
        case kAsioSupportsTimeInfo:
        case kAsioSupportsTimeCode: {
          return 1L;
        }
        default: {
          // no supporting kAsioSupportsInputMonitor yet because I don't know what it is
          return 0L;
        }
      }
    }
      
    case kAsioEngineVersion: {
      return 2L;
    }
      
    case kAsioResetRequest: {
      JNIEnv *env = NULL;
      jvm->AttachCurrentThread((void **) &env, NULL);
      if (env != NULL) {
        env->CallVoidMethod(
            env->FindClass("com/synthbot/jasiohost/AsioDriver"),
            env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireResetRequest", "()V"));
      }
      jvm->DetachCurrentThread();
      return 1L;
    }
      
    case kAsioResyncRequest: {
      JNIEnv *env = NULL;
      jvm->AttachCurrentThread((void **) &env, NULL);
      if (env != NULL) {
        env->CallVoidMethod(
            env->FindClass("com/synthbot/jasiohost/AsioDriver"),
            env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireResyncRequest", "()V"));
      }
      jvm->DetachCurrentThread();
      return 1L;
    }
      
    case kAsioLatenciesChanged: {
      JNIEnv *env = NULL;
      jvm->AttachCurrentThread((void **) &env, NULL);
      if (env != NULL) {
        env->CallVoidMethod(
            env->FindClass("com/synthbot/jasiohost/AsioDriver"),
            env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireLatenciesChanged", "(II)V"),
            Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies(NULL, NULL, JNI_TRUE),
            Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies(NULL, NULL, JNI_FALSE));
      }
      jvm->DetachCurrentThread();
      return 1L;
    }
      
    case kAsioSupportsTimeInfo: {
      return 1L;
    }
    
    case kAsioSupportsTimeCode: {
      return 0L;
    }
      
    default: {
      return 0L;
    }
  }
}


/*
 * JAsioHost
 */

JNIEXPORT jboolean JNICALL Java_com_synthbot_jasiohost_JAsioHost_loadDriver
(JNIEnv *env, jclass clazz, jstring jdriverName) {

  char *driverName = (char *) env->GetStringUTFChars(jdriverName, NULL);
  bool isLoaded = loadAsioDriver(driverName);
  env->ReleaseStringUTFChars(jdriverName, driverName);
  return isLoaded ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jobject JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOInit
(JNIEnv *env, jclass clazz) {

  ASIODriverInfo asioDriverInfo = {0};
  asioDriverInfo.asioVersion = 2L;
  ASIOError errorCode = ASIOInit(&asioDriverInfo);
  
  // create new AsioDriverInfo object and return it
  return env->NewObject(
      env->FindClass("com/synthbot/jasiohost/AsioDriverInfo"),
      env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriverInfo"), "<init>", "(IILjava/lang/String;Ljava/lang/String;)V"),
      asioDriverInfo.asioVersion,
      asioDriverInfo.driverVersion,
      env->NewStringUTF(asioDriverInfo.name),
      env->NewStringUTF(asioDriverInfo.errorMessage));
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_JAsioHost_removeCurrentDriver
(JNIEnv *env, jclass clazz) {

  asioDrivers->removeCurrentDriver();
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_JAsioHost_getDriverNames
(JNIEnv *env, jclass clazz, jobjectArray jdriverNames) {

  long maxNames = (long) env->GetArrayLength(jdriverNames);
  char **names = (char **) malloc(sizeof(char *) * maxNames);
  for (int i = 0; i < maxNames; i++) {
    names[i] = (char *) malloc(sizeof(char) * 32);
  }
  
  long numNames = asioDrivers->getDriverNames(names, maxNames);
  for (int i = 0; i < numNames; i++) {
    env->SetObjectArrayElement(jdriverNames, i, env->NewStringUTF(names[i]));
  }
  
  for(int i = 0; i < maxNames; i++) {
    free(names[i]);
  }
  free(names);
  
  return (jint) numNames;
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_JAsioHost_getCurrentDriverIndex
(JNIEnv *env, jclass clazz) {

  return (jint) asioDrivers->getCurrentDriverIndex();
}

JNIEXPORT jstring JNICALL Java_com_synthbot_jasiohost_JAsioHost_getCurrentDriverName
(JNIEnv *env, jclass clazz) {

  char *name = (char *) malloc(sizeof(char) * 32);
  asioDrivers->getCurrentDriverName(name);
  jstring jname = env->NewStringUTF(name);
  free(name);
  return jname;
}


/*
 * AsioDriver
 */

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOControlPanel
(JNIEnv *env, jclass clazz) {

  ASIOControlPanel();
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetChannels
(JNIEnv *env, jclass clazz, jboolean isInput) {
  
  long numInputs;
  long numOutputs;
  ASIOError errorCode = ASIOGetChannels(&numInputs, &numOutputs);
  if (errorCode == ASE_OK) {
    return (isInput == JNI_TRUE) ? (jint) numInputs : (jint) numOutputs;
  } else {
    return -1;
  }
}

JNIEXPORT jdouble JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetSampleRate
(JNIEnv *env, jclass clazz) {
  
  ASIOSampleRate sampleRate;
  ASIOError errorCode = ASIOGetSampleRate(&sampleRate);
  if (errorCode == ASE_OK) {
    return (jdouble) sampleRate;
  } else {
    return (jdouble) -1;
  }
}

JNIEXPORT jboolean JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOCanSampleRate
(JNIEnv *env, jclass clazz, jdouble sampleRate) {
  
  ASIOError errorCode = ASIOCanSampleRate((ASIOSampleRate) sampleRate);
  return (errorCode == ASE_OK) ? JNI_TRUE : JNI_FALSE;
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetBufferSize
(JNIEnv *env, jclass clazz, jint index) {
  
  long minSize;
  long maxSize;
  long preferredSize;
  long granularity;
  ASIOError errorCode = ASIOGetBufferSize(&minSize, &maxSize, &preferredSize, &granularity);
  if (errorCode == ASE_OK) {
    switch (index) {
      case 0: {
        return (jint) minSize;
      }
      case 1: {
        return (jint) maxSize;
      }
      case 2: {
        return (jint) preferredSize;
      }
      case 3: {
        return (jint) granularity;
      }
      default: {
        return -1;
      }
    }
  } else {
    return -1;
  }
}

JNIEXPORT jint JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies
(JNIEnv *env, jclass clazz, jboolean isInput) {
  
  long inputLatency;
  long outputLatency;
  ASIOError errorCode = ASIOGetLatencies(&inputLatency, &outputLatency);
  if (errorCode == ASE_OK) {
    return (isInput == JNI_TRUE) ? (jint) inputLatency : (jint) outputLatency;
  } else {
    return -1;
  }
}

JNIEXPORT jobject JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOGetChannelInfo
(JNIEnv *env, jclass clazz, jint index, jboolean isInput) {

  ASIOChannelInfo channelInfo;
  channelInfo.channel = (long) index;
  channelInfo.isInput = (isInput == JNI_TRUE) ? ASIOTrue : ASIOFalse;
  ASIOError errorCode = ASIOGetChannelInfo(&channelInfo);
  if (errorCode == ASE_OK) {
    return env->NewObject(
        env->FindClass("com/synthbot/jasiohost/AsioChannelInfo"),
        env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioChannelInfo"), "<init>", "(IZZILcom/synthbot/jasiohost/AsioSampleType;Ljava/lang/String;)V"),
        (jint) index,
        isInput,
        (channelInfo.isActive == ASIOTrue) ? JNI_TRUE : JNI_FALSE,
        (jint) channelInfo.channelGroup,
        env->CallStaticObjectMethod(
            env->FindClass("com/synthbot/jasiohost/AsioSampleType"),
            env->GetStaticMethodID(env->FindClass("com/synthbot/jasiohost/AsioSampleType"), "getSampleType", "(I)Lcom/synthbot/jasiohost/AsioSampleType;"),
            channelInfo.type),
        env->NewStringUTF(channelInfo.name));
  } else {
    return NULL;
  }
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOCreateBuffers
(JNIEnv *env, jclass clazz, jint numInputs, jint numOutputs, jint bufferSize) {

  ASIOBufferInfo bufferInfos[(numInputs+numOutputs)];
  for (int i = 0; i < numInputs; i++) {
    bufferInfos[i].isInput = ASIOTrue;
    bufferInfos[i].channelNum = (long) i;
  }
  for (int i = 0; i < numOutputs; i++) {
    bufferInfos[i+numInputs].isInput = ASIOFalse;
    bufferInfos[i+numInputs].channelNum = (long) i;
  }
  
  ASIOCallbacks callbacks;
  callbacks.bufferSwitch = &bufferSwitch;
  callbacks.sampleRateDidChange = &sampleRateDidChange;
  callbacks.asioMessage = &asioMessage;
  callbacks.bufferSwitchTimeInfo = &bufferSwitchTimeInfo;
  
  ASIOCreateBuffers(bufferInfos, (long) (numInputs+numOutputs), (long) bufferSize, &callbacks);
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIODisposeBuffers
(JNIEnv *env, jclass clazz) {

  ASIODisposeBuffers();
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOStart
(JNIEnv *env, jclass clazz) {

  ASIOStart();
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOStop
(JNIEnv *env, jclass clazz) {

  ASIOStop();
}