// Copyright (c) 2009-2010 Satoshi Nakamoto
// Copyright (c) 2009-2014 The Bitcoin developers
// Copyright (c) 2014-2015 The Dash developers
// Copyright (c) 2015-2018 The PIVX developers
// Distributed under the MIT software license, see the accompanying
// file COPYING or http://www.opensource.org/licenses/mit-license.php.

#ifndef BITCOIN_HASH_H
#define BITCOIN_HASH_H


#include "sha256.h"
#include "serialize.h"
#include "uint256.h"
#include "version.h"

#include <iomanip>
#include <openssl/sha.h>
#include <sstream>
#include <vector>

using namespace std;

/** A hasher class for Bitcoin's 256-bit hash (double SHA-256). */
class CHash256
{

public:
    static const size_t OUTPUT_SIZE = CSHA256::OUTPUT_SIZE;
    CSHA256 sha;

    void Finalize(unsigned char hash[OUTPUT_SIZE])
    {
        unsigned char buf[CSHA256::OUTPUT_SIZE];
        sha.Finalize(buf);
        sha.Reset().Write(buf, CSHA256::OUTPUT_SIZE).Finalize(hash);
    }

    CHash256& Write(const unsigned char* data, size_t len)
    {
        sha.Write(data, len);
        return *this;
    }

    CHash256& Reset()
    {
        sha.Reset();
        return *this;
    }
};

#ifdef GLOBALDEFINED
#define GLOBAL
#else
#define GLOBAL extern
#endif

/** A writer stream (for serialization) that computes a 256-bit hash. */
class CHashWriter
{

public:
    int nType;
    int nVersion;
    CHash256 ctx;

    CHashWriter(int nTypeIn, int nVersionIn) : nType(nTypeIn), nVersion(nVersionIn) {}

    CHashWriter& write(const char* pch, size_t size)
    {
        ctx.Write((const unsigned char*)pch, size);
        return (*this);
    }

    // invalidates the object
    uint256 GetHash()
    {
        uint256 result;
        ctx.Finalize((unsigned char*)&result);
        return result;
    }

    template <typename T>
    CHashWriter& operator<<(const T& obj)
    {
        // Serialize to this stream
        ::Serialize(*this, obj, nType, nVersion);
        return (*this);
    }
};


#endif // BITCOIN_HASH_H
