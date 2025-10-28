package com.dicoding.didapp.utils

import android.content.Context
import android.content.SharedPreferences

/**
 * Utility class untuk menyimpan pengaturan server
 */
class ServerPreferences(context: Context) {
    
    private val sharedPrefs: SharedPreferences = context.getSharedPreferences("server_preferences", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_SERVER_IP = "server_ip"
        private const val KEY_SERVER_PORT = "server_port"
        private const val DEFAULT_IP = "31.97.108.98"
        private const val DEFAULT_PORT = 8000
    }
    
    /**
     * Get server IP address
     */
    fun getServerIp(): String {
        return sharedPrefs.getString(KEY_SERVER_IP, DEFAULT_IP) ?: DEFAULT_IP
    }
    
    /**
     * Set server IP address
     */
    fun setServerIp(ip: String) {
        sharedPrefs.edit().putString(KEY_SERVER_IP, ip).apply()
    }
    
    /**
     * Get server port
     */
    fun getServerPort(): Int {
        return sharedPrefs.getInt(KEY_SERVER_PORT, DEFAULT_PORT)
    }
    
    /**
     * Set server port
     */
    fun setServerPort(port: Int) {
        sharedPrefs.edit().putInt(KEY_SERVER_PORT, port).apply()
    }
    
    /**
     * Get full server URL
     */
    fun getServerUrl(): String {
        val ip = getServerIp()
        val port = getServerPort()
        return "http://$ip:$port"
    }
    
    /**
     * Reset to default settings
     */
    fun resetToDefault() {
        setServerIp(DEFAULT_IP)
        setServerPort(DEFAULT_PORT)
    }
    
    /**
     * Check if settings are default
     */
    fun isDefaultSettings(): Boolean {
        return getServerIp() == DEFAULT_IP && getServerPort() == DEFAULT_PORT
    }
    
    /**
     * Get server info as string
     */
    fun getServerInfo(): String {
        return "${getServerIp()}:${getServerPort()}"
    }
    
    /**
     * Validate IP address format
     */
    fun isValidIp(ip: String): Boolean {
        val ipPattern = Regex("^((25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)\\.){3}(25[0-5]|2[0-4][0-9]|[01]?[0-9][0-9]?)$")
        return ipPattern.matches(ip)
    }
    
    /**
     * Validate port number
     */
    fun isValidPort(port: Int): Boolean {
        return port in 1..65535
    }
}
