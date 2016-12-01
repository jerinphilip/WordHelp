#include <jni.h>
#include <string>

extern "C"
jstring
Java_in_ac_iiit_cvit_wordhelp_MainActivity_stringFromJNI(
        JNIEnv *env,
        jobject /* this */) {
    std::string hello = "Hello from C++";
    return env->NewStringUTF(hello.c_str());
}
