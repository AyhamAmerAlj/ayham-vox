package com.ayham.vox.core

import java.net.Inet4Address
import java.net.NetworkInterface

object NetUtil {
    /** عنوان IPv4 المحلي للجهاز على الشبكة (مثل 192.168.1.20). */
    fun localIpv4(): String? {
        return try {
            val interfaces = NetworkInterface.getNetworkInterfaces() ?: return null
            interfaces.toList()
                .filter { it.isUp && !it.isLoopback }
                .flatMap { it.inetAddresses.toList() }
                .filterIsInstance<Inet4Address>()
                .firstOrNull { it.isSiteLocalAddress }
                ?.hostAddress
        } catch (e: Exception) {
            null
        }
    }
}
