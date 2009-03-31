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

#define JNI_VERSION JNI_VERSION_1_4

// external reference
extern AsioDrivers* asioDrivers;
bool loadAsioDriver(char *name);

// global variables
JavaVM *jvm;
jobject jAsioDriver; // a strong global reference to the AsioDriver Java object for use in callbacks
jmethodID fireBufferSwitchMid;
typedef struct BufferVars {
  ASIOBufferInfo *bufferInfos;
  ASIOSampleType *sampleTypes;
  int numInitedChannels; // length of bufferInfos and sampleTypes arrays
  int bufferSize;
  jobjectArray inputIntArrays;
  jobjectArray outputIntArrays;
  jobjectArray inputFloatArrays;
  jobjectArray outputFloatArrays;
  jobjectArray inputDoubleArrays;
  jobjectArray outputDoubleArrays;
} BufferVars;
BufferVars bufferVars = {0};

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
  
  JNIEnv *env = NULL;
  jvm->GetEnv((void **) &env, JNI_VERSION);
  fireBufferSwitchMid = env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireBufferSwitch", "([[I[[I[[F[[F[[D[[D)V");
  return JNI_VERSION;
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
  jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
  if (res == JNI_OK && env != NULL) {
    /* TODO: deal with input arrays
    for (int i = 0; i < numInitedChannels; i++) {
      if (bufferInfos[i].isInput == ASIOTrue) { // only treat input arrays at input
        jarray jArray = inputArrays[bufferInfos[i].channelNum];
        void *nativeArray = (void *) env->GetPrimitiveArrayCritical(jArray, NULL);
        memcpy(nativeArray, bufferInfos[i].buffers[bufferIndex], sizeof(int) * bufferSize);
        env->ReleasePrimitiveArrayCritical(jArray, nativeArray, 0); // do copy contents to java array
      }
    }
    */

    env->CallVoidMethod(
        jAsioDriver,
        fireBufferSwitchMid,
        bufferVars.inputIntArrays, bufferVars.outputIntArrays,
        bufferVars.inputFloatArrays, bufferVars.outputFloatArrays,
        bufferVars.inputDoubleArrays, bufferVars.outputDoubleArrays);

    for (int i = 0; i < bufferVars.numInitedChannels; i++) {
      if (bufferVars.bufferInfos[i].isInput == ASIOFalse) { // copy output to native arrays
        switch (bufferVars.sampleTypes[i]) {
          case ASIOSTFloat32MSB:
          case ASIOSTFloat32LSB: {
            jarray jArray = (jarray) env->GetObjectArrayElement((bufferVars.bufferInfos[i].isInput == ASIOTrue) ? bufferVars.inputFloatArrays : bufferVars.outputFloatArrays, bufferVars.bufferInfos[i].channelNum);
            void *nativeArray = (void *) env->GetPrimitiveArrayCritical(jArray, NULL);
            memcpy(bufferVars.bufferInfos[i].buffers[bufferIndex], nativeArray, sizeof(float) * bufferVars.bufferSize);
            env->ReleasePrimitiveArrayCritical(jArray, nativeArray, JNI_ABORT); // do NOT copy contents back to java array
            break;
          }
          case ASIOSTFloat64MSB:
          case ASIOSTFloat64LSB: {
            jarray jArray = (jarray) env->GetObjectArrayElement((bufferVars.bufferInfos[i].isInput == ASIOTrue) ? bufferVars.inputDoubleArrays : bufferVars.outputDoubleArrays, bufferVars.bufferInfos[i].channelNum);
            void *nativeArray = (void *) env->GetPrimitiveArrayCritical(jArray, NULL);
            memcpy(bufferVars.bufferInfos[i].buffers[bufferIndex], nativeArray, sizeof(double) * bufferVars.bufferSize);
            env->ReleasePrimitiveArrayCritical(jArray, nativeArray, JNI_ABORT); // do NOT copy contents back to java array
            break;
          }
          case ASIOSTInt32MSB:
          case ASIOSTInt32MSB16:
          case ASIOSTInt32MSB18:
          case ASIOSTInt32MSB20:
          case ASIOSTInt32MSB24:
          case ASIOSTInt32LSB:
          case ASIOSTInt32LSB16:
          case ASIOSTInt32LSB18:
          case ASIOSTInt32LSB20:
          case ASIOSTInt32LSB24:{
            jarray jArray = (jarray) env->GetObjectArrayElement(bufferVars.outputIntArrays, bufferVars.bufferInfos[i].channelNum);
            void *nativeArray = (void *) env->GetPrimitiveArrayCritical(jArray, NULL);
            memcpy(bufferVars.bufferInfos[i].buffers[bufferIndex], nativeArray, sizeof(int) * bufferVars.bufferSize);
            env->ReleasePrimitiveArrayCritical(jArray, nativeArray, JNI_ABORT); // do NOT copy contents back to java array
            break;
          }
          default: {
            // ???
          }
        }
      }
    }
  }
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
  jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
  if (res == JNI_OK && env != NULL) {
    env->CallVoidMethod(
        jAsioDriver,
        env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireSampleRateDidChange", "(D)V"),
        (jdouble) sampleRate);
  }
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
      jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
      if (res == JNI_OK && env != NULL) {
        env->CallVoidMethod(
            jAsioDriver,
            env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireResetRequest", "()V"));
      }
      return 1L;
    }
      
    case kAsioResyncRequest: {
      JNIEnv *env = NULL;
      jint res = jvm->AttachCurrentThreadAsDaemon((void **) &env, NULL);
      if (res == JNI_OK && env != NULL) {
        env->CallVoidMethod(
            jAsioDriver,
            env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireResyncRequest", "()V"));
      }
      return 1L;
    }
      
    case kAsioLatenciesChanged: {
      JNIEnv *env = NULL;
      jint res = jvm->AttachCurrentThread((void **) &env, NULL);
      if (res == JNI_OK && env != NULL) {
        env->CallVoidMethod(
            jAsioDriver,
            env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriver"), "fireLatenciesChanged", "(II)V"),
            Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies(NULL, NULL, JNI_TRUE),
            Java_com_synthbot_jasiohost_AsioDriver_ASIOGetLatencies(NULL, NULL, JNI_FALSE));
      }
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
(JNIEnv *env, jobject jobj) {

  ASIODriverInfo asioDriverInfo = {0};
  asioDriverInfo.asioVersion = 2L;
  ASIOError errorCode = ASIOInit(&asioDriverInfo);
  if (errorCode == ASE_OK) {
    jAsioDriver = env->NewGlobalRef(jobj); // store global ref to AsioDriver object for callbacks
    
    // create new AsioDriverInfo object and return it
    return env->NewObject(
        env->FindClass("com/synthbot/jasiohost/AsioDriverInfo"),
        env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioDriverInfo"), "<init>", "(IILjava/lang/String;Ljava/lang/String;)V"),
        asioDriverInfo.asioVersion,
        asioDriverInfo.driverVersion,
        env->NewStringUTF(asioDriverInfo.name),
        env->NewStringUTF(asioDriverInfo.errorMessage));
  } else {
    return NULL;
  }
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIOExit
(JNIEnv *env, jobject jobj) {

  env->DeleteGlobalRef(jAsioDriver);
  ASIOExit();
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
(JNIEnv *env, jclass clazz, jint argIndex) {
  
  long minSize;
  long maxSize;
  long preferredSize;
  long granularity;
  ASIOError errorCode = ASIOGetBufferSize(&minSize, &maxSize, &preferredSize, &granularity);
  if (errorCode == ASE_OK) {
    switch (argIndex) {
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
(JNIEnv *env, jclass clazz, jobjectArray channelsToInit, jint bufferSize, 
 jobjectArray inputIntArrays, jobjectArray outputIntArrays, 
 jobjectArray inputFloatArrays, jobjectArray outputFloatArrays, 
 jobjectArray inputDoubleArrays, jobjectArray outputDoubleArrays) {

  bufferVars.numInitedChannels = (int) env->GetArrayLength(channelsToInit);
  bufferVars.bufferInfos = (ASIOBufferInfo *) malloc(sizeof(ASIOBufferInfo) * bufferVars.numInitedChannels);
  bufferVars.sampleTypes = (ASIOSampleType *) malloc(sizeof(ASIOSampleType) * bufferVars.numInitedChannels);
  for (int i = 0; i < bufferVars.numInitedChannels; i++) {
    jobject channelInfo = env->GetObjectArrayElement(channelsToInit, (jsize) i);
    jboolean jIsInput = env->CallBooleanMethod(channelInfo,
                                              env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioChannelInfo"), "isInput", "()Z"));
    bufferVars.bufferInfos[i].isInput = (jIsInput == JNI_TRUE) ? ASIOTrue : ASIOFalse;
    jint channelNum = env->CallIntMethod(channelInfo,
                                         env->GetMethodID(env->FindClass("com/synthbot/jasiohost/AsioChannelInfo"), "getChannelIndex", "()I"));
    bufferVars.bufferInfos[i].channelNum = (long) channelNum;
    bufferVars.sampleTypes[i] = ASIOSTInt32LSB;
  }

  bufferVars.inputIntArrays = (jobjectArray) env->NewGlobalRef(inputIntArrays);
  bufferVars.outputIntArrays = (jobjectArray) env->NewGlobalRef(outputIntArrays);
  bufferVars.inputFloatArrays = (jobjectArray) env->NewGlobalRef(inputFloatArrays);
  bufferVars.outputFloatArrays = (jobjectArray) env->NewGlobalRef(outputFloatArrays);
  bufferVars.inputDoubleArrays = (jobjectArray) env->NewGlobalRef(inputDoubleArrays);
  bufferVars.outputDoubleArrays = (jobjectArray) env->NewGlobalRef(outputDoubleArrays);
  
  bufferVars.bufferSize = (int) bufferSize;
  
  ASIOCallbacks callbacks;
  callbacks.bufferSwitch = &bufferSwitch;
  callbacks.sampleRateDidChange = &sampleRateDidChange;
  callbacks.asioMessage = &asioMessage;
  callbacks.bufferSwitchTimeInfo = &bufferSwitchTimeInfo;
  
  ASIOCreateBuffers(bufferVars.bufferInfos, (long) bufferVars.numInitedChannels, (long) bufferVars.bufferSize, &callbacks);
}

JNIEXPORT void JNICALL Java_com_synthbot_jasiohost_AsioDriver_ASIODisposeBuffers
(JNIEnv *env, jclass clazz) {

  // need to free bufferVars? YES!!!
  env->DeleteGlobalRef(bufferVars.inputIntArrays);
  env->DeleteGlobalRef(bufferVars.outputIntArrays);
  env->DeleteGlobalRef(bufferVars.inputFloatArrays);
  env->DeleteGlobalRef(bufferVars.outputFloatArrays);
  env->DeleteGlobalRef(bufferVars.inputDoubleArrays);
  env->DeleteGlobalRef(bufferVars.outputDoubleArrays);
  
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