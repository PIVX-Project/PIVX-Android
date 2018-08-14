#include <jni.h>

#ifndef _Included_com_zerocoinj_JniBridge
#define _Included_com_zerocoinj_JniBridge

#include <string>
#include "version.h"
#include "uint256.h"
#include "sha256.cpp"
#include "hash.h"
#include "libzerocoin/bignum.h"
#include <iostream>
#include <sstream>
#include <exception>
#include <stdexcept>

#endif
extern "C"
JNIEXPORT jbyteArray JNICALL
Java_pivx_org_pivxwallet_AndroidJniBridge_compute1024seedNative(JNIEnv *env, jobject instance,
                                                                jbyteArray seed) {
    try{

        int len = env->GetArrayLength (seed);
        unsigned char* buf = new unsigned char[len];
        env->GetByteArrayRegion (seed, 0, len, reinterpret_cast<jbyte*>(buf));
        std::vector<unsigned char> __c_vec(buf,buf + len);

        CBigNum num(__c_vec);
        uint256 hashSeed = num.getuint256();

        CHashWriter hasher(0,0);
        hasher << hashSeed;

        vector<unsigned char> vResult;
        for (int i = 0; i < 4; i ++) {
            uint256 hash = hasher.GetHash();
            vector<unsigned char> vHash = CBigNum(hash).getvch();
            vResult.insert(vResult.end(), vHash.begin(), vHash.end());
            hasher << vResult;
        }

        //std::cout << "############# end seedTo1024 ##############" << std::endl;

        CBigNum bnResult;
        bnResult.setvch(vResult);

        std::vector<unsigned char> vch = bnResult.getvch();
        jbyteArray ret = env->NewByteArray (vch.size());
        env->SetByteArrayRegion (ret, 0, vch.size(), reinterpret_cast<jbyte*>(vch.data()));
        return ret;

    }catch (const std::exception &exc){
        std::cout << "Exception: " << exc.what() << std::endl;
        return NULL;
    }
}

extern "C"
JNIEXPORT jstring JNICALL
Java_pivx_org_pivxwallet_AndroidJniBridge_computeVSeedAndVExpandedNative(JNIEnv *env,
                                                                         jobject instance) {
    uint256 notNum = ~uint256(0);
    CBigNum notZeroBigNum(notNum);
    CBigNum randBignum = CBigNum::randBignum(notZeroBigNum);
    uint256 hashRand = randBignum.getuint256();
    uint256 hashSeed = hashRand;

    CHashWriter hasher(0,0);
    hasher << hashSeed;

    vector<unsigned char> vResult;
    for (int i = 0; i < 4; i ++) {
        uint256 hash = hasher.GetHash();
        //std::cout << "hash_" << i << ": " << hash.GetHex() << std::endl;
        vector<unsigned char> vHash = CBigNum(hash).getvch();
        //std::cout << "vHash: " << HexStr(vHash) << std::endl;
        vResult.insert(vResult.end(), vHash.begin(), vHash.end());
        //std::cout << "vResult: " << HexStr(vHash) << std::endl;
        hasher << vResult;
    }


    CBigNum bnExpanded;
    bnExpanded.setvch(vResult);

    CBigNum vSeed = CBigNum(hashRand);
    CBigNum vExpanded = bnExpanded;


    std::stringstream ret;
    ret << vSeed.GetDec();
    ret << "||";
    ret << vExpanded.GetDec();

    // Success! We're done.
    return (*env).NewStringUTF(ret.str().data());
}