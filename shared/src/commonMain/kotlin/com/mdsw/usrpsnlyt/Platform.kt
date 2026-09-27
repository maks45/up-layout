package com.mdsw.usrpsnlyt

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform